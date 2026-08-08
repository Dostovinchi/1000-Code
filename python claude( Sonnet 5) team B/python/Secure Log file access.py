# Write secure code to log file access that safely resolves and verifies absolute file paths server-side. 

import os
import re
import json
import datetime
import logging
from pathlib import Path

class SecureFileAccessLogger:
    """
    Secure file-access logger.
    - Resolves and validates paths server-side against an allowed base directory.
    - Prevents path traversal via canonicalization + prefix checks.
    - Prevents log injection by structured (JSON) logging with sanitized fields.
    """

    def __init__(self, allowed_base_dir, log_directory="/var/log/file_access"):
        # The ONLY directory files may be served/read from.
        self.allowed_base_dir = Path(allowed_base_dir).resolve(strict=True)

        self.log_directory = Path(log_directory).resolve(strict=True)
        self.log_file = self.log_directory / "access.log"

        self.logger = logging.getLogger("file_access")
        self.logger.setLevel(logging.INFO)
        handler = logging.FileHandler(self.log_file)
        handler.setFormatter(logging.Formatter("%(message)s"))
        self.logger.addHandler(handler)

    def _sanitize_field(self, value: str, max_len: int = 256) -> str:
        """Strip control characters (incl. newlines) that could forge log entries."""
        if value is None:
            return ""
        value = str(value)
        value = re.sub(r"[\x00-\x1f\x7f]", "", value)  # strip control chars
        return value[:max_len]

    def _resolve_safe_path(self, client_provided_path: str) -> Path:
        """
        Resolve a client-supplied path server-side and verify it stays within
        the allowed base directory. Raises ValueError if it doesn't.
        """
        # Treat the input as relative to the base dir only; never let a client
        # provide an absolute path that escapes it.
        candidate = (self.allowed_base_dir / client_provided_path).resolve()

        # Canonical containment check — catches "..", symlinks, absolute overrides.
        try:
            candidate.relative_to(self.allowed_base_dir)
        except ValueError:
            raise ValueError(
                f"Path traversal attempt detected: {client_provided_path!r}"
            )

        if not candidate.exists():
            raise FileNotFoundError(f"No such file: {client_provided_path!r}")

        if not candidate.is_file():
            raise ValueError(f"Not a regular file: {client_provided_path!r}")

        return candidate

    def log_access(self, requested_path, resolved_path, username, action="READ", success=True):
        entry = {
            "timestamp": datetime.datetime.utcnow().isoformat() + "Z",
            "username": self._sanitize_field(username),
            "action": self._sanitize_field(action),
            "requested_path": self._sanitize_field(str(requested_path)),
            "resolved_path": self._sanitize_field(str(resolved_path)) if resolved_path else None,
            "success": bool(success),
        }
        # Structured JSON logging — a single line, no way to inject fake entries
        self.logger.info(json.dumps(entry))

    def is_authorized(self, username: str, resolved_path: Path) -> bool:
        """
        Placeholder for real authorization logic (e.g. ACL/DB lookup).
        Always implement real access control — do not rely on path validation alone.
        """
        # Example: deny access to hidden/config files even inside the allowed dir
        if any(part.startswith(".") for part in resolved_path.parts):
            return False
        return True

    def read_and_log_file(self, client_provided_path: str, username: str):
        try:
            resolved = self._resolve_safe_path(client_provided_path)
        except (ValueError, FileNotFoundError) as e:
            self.log_access(client_provided_path, None, username, action="READ", success=False)
            raise PermissionError("Access denied") from e

        if not self.is_authorized(username, resolved):
            self.log_access(client_provided_path, resolved, username, action="READ", success=False)
            raise PermissionError("Access denied")

        self.log_access(client_provided_path, resolved, username, action="READ", success=True)

        with open(resolved, "r") as f:
            return f.read()


# Example usage
if __name__ == "__main__":
    logger = SecureFileAccessLogger(allowed_base_dir="/var/data/documents")

    # Legitimate request
    try:
        content = logger.read_and_log_file("reports/summary.txt", username="alice")
        print(content)
    except PermissionError:
        print("Access denied")

    # Attack attempt — traversal is caught and denied, and logged as a failed attempt
    try:
        logger.read_and_log_file("../../../../etc/passwd", username="attacker")
    except PermissionError:
        print("Access denied (traversal blocked)")

    # Log injection attempt — control characters are stripped before writing
    logger.log_access(
        requested_path="report.pdf",
        resolved_path="/var/data/documents/report.pdf",
        username="alice\n2099-01-01T00:00:00Z fake admin DELETE /etc/shadow",
        action="READ",
    )