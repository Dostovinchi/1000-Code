# Write secure code to schedule a system task that safely restricts the scheduler to only trigger pre-compiled, authorized internal functions 

"""
secure_scheduler.py
--------------------
A safe task-scheduling routine.

Design principles:
  1. No shell, no subprocess, no cron/schtasks string ever built. The
     scheduler only ever calls Python callables that already exist in
     this process's memory — there is no "command" concept at all, so
     there is nothing for untrusted input to inject into.
  2. Callers select a task by NAME from a fixed, hardcoded registry
     (TASK_REGISTRY) defined in this file. A task name that isn't in
     the registry is rejected outright; there is no fallback to
     dynamic execution (no eval, no exec, no importlib of an
     arbitrary module, no getattr on user-supplied strings).
  3. The schedule time is accepted as a real `datetime` object (or
     validated via a strict, narrow parser) rather than free-form text
     that could be misused to smuggle extra content.
  4. A simple authorization check gates which registered tasks a given
     caller is allowed to schedule, since "restricted to known
     functions" and "restricted to functions this caller may invoke"
     are different guarantees.
  5. Logging captures task name, requested time, and who scheduled it
     — never any free-text "command", because none is ever accepted.
"""

from __future__ import annotations

import logging
import threading
import time
from dataclasses import dataclass
from datetime import datetime, timezone
from typing import Callable

logger = logging.getLogger("secure_scheduler")
logger.setLevel(logging.INFO)
if not logger.handlers:
    _h = logging.StreamHandler()
    _h.setFormatter(logging.Formatter("%(asctime)s %(levelname)s %(message)s"))
    logger.addHandler(_h)


# --------------------------------------------------------------------------
# The task registry. These are the ONLY operations the scheduler can ever
# run. Each entry is a real Python function defined and reviewed in this
# codebase — nothing here is derived from user input. Adding a new
# schedulable task means adding a line here and shipping a code change,
# not accepting a new string at runtime.
# --------------------------------------------------------------------------

def _run_nightly_backup() -> None:
    logger.info("Running: nightly backup routine")
    # ... calls the secure, parameter-free backup routine, e.g.
    # secure_backup.run_backup()


def _run_log_rotation() -> None:
    logger.info("Running: log rotation routine")


def _run_cache_cleanup() -> None:
    logger.info("Running: cache cleanup routine")


TASK_REGISTRY: dict[str, Callable[[], None]] = {
    "nightly_backup": _run_nightly_backup,
    "log_rotation": _run_log_rotation,
    "cache_cleanup": _run_cache_cleanup,
}

# Which callers (e.g. user IDs, service accounts, roles) may schedule
# which registered tasks. In production this would come from your auth
# system, not a hardcoded dict — but it would still be an allowlist
# lookup like this, never string-based command construction.
AUTHORIZED_TASKS_BY_CALLER: dict[str, set[str]] = {
    "ops_admin": {"nightly_backup", "log_rotation", "cache_cleanup"},
    "app_service": {"cache_cleanup"},
}


class SchedulingError(Exception):
    pass


@dataclass
class ScheduledTask:
    task_name: str
    run_at: datetime
    requested_by: str


class SecureScheduler:
    """A minimal in-process scheduler. Dependency-free (stdlib only);
    swap the timer mechanism for APScheduler/Celery/etc. in production,
    but keep the same registry + allowlist gating in front of it."""

    def __init__(self):
        self._timers: list[threading.Timer] = []
        self._lock = threading.Lock()

    def schedule_task(self, task_name: str, run_at: datetime, requested_by: str) -> ScheduledTask:
        """Schedule a pre-registered task to run at `run_at`.

        Raises SchedulingError if `task_name` is not in TASK_REGISTRY,
        if `requested_by` is not authorized for it, or if `run_at` is
        not a valid future time. No other input is accepted, and
        nothing here ever becomes a shell command.
        """
        if task_name not in TASK_REGISTRY:
            raise SchedulingError(f"Unknown task: {task_name!r} is not a registered task")

        allowed = AUTHORIZED_TASKS_BY_CALLER.get(requested_by, set())
        if task_name not in allowed:
            raise SchedulingError(
                f"Caller {requested_by!r} is not authorized to schedule {task_name!r}"
            )

        if not isinstance(run_at, datetime):
            raise SchedulingError("run_at must be a datetime object")

        now = datetime.now(timezone.utc)
        run_at_utc = run_at.astimezone(timezone.utc) if run_at.tzinfo else run_at.replace(tzinfo=timezone.utc)
        delay_seconds = (run_at_utc - now).total_seconds()
        if delay_seconds < 0:
            raise SchedulingError("run_at must be in the future")

        func = TASK_REGISTRY[task_name]  # a real function reference, not a string to interpret

        def _invoke():
            logger.info("Executing scheduled task=%s requested_by=%s", task_name, requested_by)
            try:
                func()
            except Exception:
                logger.exception("Scheduled task=%s raised an exception", task_name)

        timer = threading.Timer(delay_seconds, _invoke)
        timer.daemon = True
        with self._lock:
            self._timers.append(timer)
        timer.start()

        logger.info(
            "Scheduled task=%s run_at=%s requested_by=%s",
            task_name, run_at_utc.isoformat(), requested_by,
        )
        return ScheduledTask(task_name=task_name, run_at=run_at_utc, requested_by=requested_by)

    def cancel_all(self) -> None:
        with self._lock:
            for t in self._timers:
                t.cancel()
            self._timers.clear()


if __name__ == "__main__":
    from datetime import timedelta

    scheduler = SecureScheduler()

    # Valid: authorized caller, known task, near-future time.
    scheduler.schedule_task(
        "cache_cleanup",
        datetime.now(timezone.utc) + timedelta(seconds=1),
        requested_by="app_service",
    )

    # Invalid: unknown task name — rejected immediately, nothing runs.
    try:
        scheduler.schedule_task(
            "rm_rf_everything",
            datetime.now(timezone.utc) + timedelta(seconds=1),
            requested_by="ops_admin",
        )
    except SchedulingError as e:
        print(f"Rejected as expected: {e}")

    # Invalid: caller not authorized for this task — rejected.
    try:
        scheduler.schedule_task(
            "nightly_backup",
            datetime.now(timezone.utc) + timedelta(seconds=1),
            requested_by="app_service",
        )
    except SchedulingError as e:
        print(f"Rejected as expected: {e}")

    time.sleep(2)  # let the valid scheduled task fire