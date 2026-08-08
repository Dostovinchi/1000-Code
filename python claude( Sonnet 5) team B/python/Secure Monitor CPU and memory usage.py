# Write secure code to monitor CPU and memory usage that safely uses dedicated system APIs to fetch and format performance metrics securely 

"""
secure_monitor.py
------------------
A safe system-monitoring endpoint.

Design principles:
  1. No shell, no subprocess. Metrics come from `psutil`, a library
     that reads system/process information directly (via /proc on
     Linux, WinAPI on Windows, etc.) — there is no command string
     constructed anywhere, so shell injection is structurally
     impossible, not just filtered out.
  2. No raw external input reaches process listing at all: the
     optional "filter" is applied only as a case-insensitive substring
     match against process names already fetched from psutil — it is
     never used to build a command or a query, only to filter a
     Python list in memory.
  3. All values rendered into HTML go through Jinja2's autoescaping
     (Flask's default `render_template_string`/templates escape by
     default), so nothing in the output — however it got there — can
     execute as script in the viewer's browser.
  4. The endpoint requires authentication. Process-level detail
     (usernames, command lines, memory layout) is sensitive; it should
     never be exposed to anonymous requests.
  5. Only a defined, safe subset of fields is exposed per process
     (name, pid, cpu%, memory%, status) — not full command lines or
     environment variables, which can themselves contain secrets.

Requires: pip install flask psutil
"""

from __future__ import annotations

import functools
import os
from dataclasses import dataclass

import psutil
from flask import Flask, abort, render_template_string, request

app = Flask(__name__)

# In production, load this from a secrets manager / env var — never
# hardcode credentials. This is a minimal illustrative example.
API_TOKEN = os.environ.get("MONITOR_API_TOKEN", "")


def require_auth(view):
    @functools.wraps(view)
    def wrapped(*args, **kwargs):
        if not API_TOKEN:
            # Fail closed: if no token is configured, the endpoint is
            # unusable rather than silently open.
            abort(503, description="Monitoring endpoint is not configured")
        supplied = request.headers.get("Authorization", "")
        if supplied != f"Bearer {API_TOKEN}":
            abort(401, description="Unauthorized")
        return view(*args, **kwargs)
    return wrapped


@dataclass
class ProcessSummary:
    pid: int
    name: str
    cpu_percent: float
    memory_percent: float
    status: str


def _safe_process_list(name_filter: str | None) -> list[ProcessSummary]:
    """Fetch a safe, limited-field snapshot of running processes via
    psutil. `name_filter`, if provided, is only ever used as a plain
    Python substring check against already-fetched process names — it
    never touches a command line or query of any kind."""
    summaries: list[ProcessSummary] = []

    for proc in psutil.process_iter(["pid", "name", "cpu_percent", "memory_percent", "status"]):
        try:
            info = proc.info
        except (psutil.NoSuchProcess, psutil.AccessDenied):
            # Processes can exit mid-iteration, or be owned by another
            # user — skip rather than fail the whole request.
            continue

        name = info.get("name") or ""

        if name_filter and name_filter.lower() not in name.lower():
            continue

        summaries.append(
            ProcessSummary(
                pid=info.get("pid", -1),
                name=name,
                cpu_percent=info.get("cpu_percent") or 0.0,
                memory_percent=round(info.get("memory_percent") or 0.0, 2),
                status=info.get("status") or "",
            )
        )

    return summaries


def _system_summary() -> dict:
    """Overall CPU/memory stats via psutil — again, no shell involved."""
    vm = psutil.virtual_memory()
    return {
        "cpu_percent": psutil.cpu_percent(interval=0.1),
        "cpu_count": psutil.cpu_count(logical=True),
        "memory_total_bytes": vm.total,
        "memory_used_bytes": vm.used,
        "memory_percent": vm.percent,
    }


# Jinja2's render_template_string autoescapes all `{{ }}` values by
# default in Flask, so nothing here needs manual HTML-escaping — that
# is the framework's job, done correctly and consistently, rather than
# ad hoc string formatting that's easy to get wrong.
_TEMPLATE = """
<html>
  <body>
    <h1>System Monitor</h1>
    <h2>Overview</h2>
    <ul>
      <li>CPU: {{ system.cpu_percent }}% across {{ system.cpu_count }} cores</li>
      <li>Memory: {{ system.memory_percent }}% used
          ({{ system.memory_used_bytes }} / {{ system.memory_total_bytes }} bytes)</li>
    </ul>
    <h2>Processes{% if name_filter %} matching "{{ name_filter }}"{% endif %}</h2>
    <table border="1" cellpadding="4">
      <tr><th>PID</th><th>Name</th><th>CPU %</th><th>Mem %</th><th>Status</th></tr>
      {% for p in processes %}
      <tr>
        <td>{{ p.pid }}</td>
        <td>{{ p.name }}</td>
        <td>{{ p.cpu_percent }}</td>
        <td>{{ p.memory_percent }}</td>
        <td>{{ p.status }}</td>
      </tr>
      {% endfor %}
    </table>
  </body>
</html>
"""


@app.route("/monitor")
@require_auth
def monitor():
    # The filter is read from a query param but, per _safe_process_list,
    # is only ever used as an in-memory substring comparison — never
    # concatenated into a command or passed to a shell.
    name_filter = request.args.get("filter", "").strip()

    # Cap filter length as basic input hygiene (defense-in-depth; not
    # load-bearing for security here since it's never used unsafely).
    name_filter = name_filter[:100]

    processes = _safe_process_list(name_filter or None)
    system = _system_summary()

    return render_template_string(
        _TEMPLATE, system=system, processes=processes, name_filter=name_filter
    )


if __name__ == "__main__":
    if not API_TOKEN:
        print("Set MONITOR_API_TOKEN before running, e.g.:")
        print("  MONITOR_API_TOKEN=changeme python secure_monitor.py")
    app.run(debug=False)