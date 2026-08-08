Write secure code to load environment variables that safely isolates and redacts sensitive keys from any potential error outputs 

"""
secure_env.py
=============

A small, dependency-free utility for loading environment variables that
keeps sensitive values (API keys, tokens, passwords, secrets, etc.) out
of logs, error messages, and tracebacks.

Key ideas:
  1. Sensitive variable names are detected by pattern, not by an
     exhaustive hand-maintained list.
  2. Values are wrapped in a `SecretStr`-like object whose repr/str is
     always redacted, so an accidental `print(config)` or an exception
     that captures local variables can't leak the raw value.
  3. A custom exception type carries a redacted message only -- the
     raw offending value never becomes part of an exception's string
     representation.
  4. An optional global excepthook further scrubs any sensitive-looking
     substrings out of uncaught-exception tracebacks as defense in depth.

Usage:
    from secure_env import EnvLoader

    env = EnvLoader(dotenv_path=".env")   # dotenv_path is optional
    db_host = env.get("DB_HOST", required=True)
    api_key = env.get_secret("STRIPE_API_KEY", required=True)

    # Safe to log/print - value is redacted automatically
    print(api_key)          # -> Secret('***REDACTED***')
    print(api_key.reveal()) # -> the real value, only when you explicitly ask
"""

from __future__ import annotations

import os
import re
import sys
import logging
from typing import Optional, Dict, Iterable

# --------------------------------------------------------------------------
# Configuration: patterns that mark a variable name as "sensitive"
# --------------------------------------------------------------------------

SENSITIVE_NAME_PATTERNS = (
    r"key", r"secret", r"token", r"password", r"pwd", r"auth",
    r"credential", r"api[_-]?key", r"private", r"cert", r"signature",
)
_SENSITIVE_RE = re.compile("|".join(SENSITIVE_NAME_PATTERNS), re.IGNORECASE)

REDACTED = "***REDACTED***"


def is_sensitive_name(name: str) -> bool:
    """Heuristically decide whether an env var name likely holds a secret."""
    return bool(_SENSITIVE_RE.search(name))


def redact_text(text: str, secrets: Iterable[str]) -> str:
    """Scrub any known secret values out of an arbitrary string."""
    for value in secrets:
        if value and value in text:
            text = text.replace(value, REDACTED)
    return text


# --------------------------------------------------------------------------
# Secret wrapper -- never reveals its value via repr/str/logging/f-strings
# --------------------------------------------------------------------------

class Secret:
    """Wraps a sensitive string so it can't be accidentally printed/logged."""

    __slots__ = ("_value",)

    def __init__(self, value: str):
        self._value = value

    def reveal(self) -> str:
        """Explicitly access the real value. Use only when passing to the
        system that actually needs it (e.g. an HTTP client)."""
        return self._value

    def __repr__(self) -> str:
        return f"Secret('{REDACTED}')"

    def __str__(self) -> str:
        return REDACTED

    def __eq__(self, other):
        if isinstance(other, Secret):
            return self._value == other._value
        return NotImplemented

    def __bool__(self) -> bool:
        return bool(self._value)


# --------------------------------------------------------------------------
# Exceptions that never leak the offending value
# --------------------------------------------------------------------------

class EnvError(Exception):
    """Base error for environment loading problems. Message is safe to log."""


class MissingEnvVarError(EnvError):
    def __init__(self, name: str):
        # Only the *name* of the variable is included, never its value.
        super().__init__(f"Required environment variable '{name}' is not set.")
        self.var_name = name


class InvalidEnvVarError(EnvError):
    def __init__(self, name: str, reason: str):
        super().__init__(f"Environment variable '{name}' is invalid: {reason}")
        self.var_name = name


# --------------------------------------------------------------------------
# Loader
# --------------------------------------------------------------------------

class EnvLoader:
    """
    Loads environment variables safely.

    - `.get()` returns plain strings for non-sensitive names.
    - `.get_secret()` (or any name matched by SENSITIVE_NAME_PATTERNS) is
      returned wrapped in `Secret`, so it can't leak accidentally.
    - Raw sensitive values are tracked internally purely so they can be
      redacted out of *any* error text this process emits.
    """

    def __init__(self, dotenv_path: Optional[str] = None, source: Optional[Dict[str, str]] = None):
        self._env: Dict[str, str] = dict(source) if source is not None else dict(os.environ)
        if dotenv_path:
            self._load_dotenv(dotenv_path)
        self._known_secret_values: set[str] = set()

    def _load_dotenv(self, path: str) -> None:
        """Minimal .env parser (KEY=VALUE per line); avoids a hard dependency."""
        try:
            with open(path, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith("#") or "=" not in line:
                        continue
                    key, _, value = line.partition("=")
                    key = key.strip()
                    value = value.strip().strip('"').strip("'")
                    # Don't overwrite real OS-level env vars with .env values
                    self._env.setdefault(key, value)
        except FileNotFoundError:
            # Not fatal -- caller may rely on real environment variables only
            logging.getLogger(__name__).debug("No .env file found at %s", path)

    def _raw(self, name: str) -> Optional[str]:
        return self._env.get(name)

    def get(self, name: str, default: Optional[str] = None, required: bool = False) -> Optional[str]:
        """Fetch a non-sensitive variable as a plain string."""
        if is_sensitive_name(name):
            raise EnvError(
                f"'{name}' looks sensitive -- use get_secret() instead of get()."
            )
        value = self._raw(name)
        if value is None:
            if required:
                raise MissingEnvVarError(name)
            return default
        return value

    def get_secret(self, name: str, default: Optional[str] = None, required: bool = False) -> Optional[Secret]:
        """Fetch a sensitive variable, returned wrapped in Secret()."""
        value = self._raw(name)
        if value is None:
            if required:
                raise MissingEnvVarError(name)
            return Secret(default) if default is not None else None
        self._known_secret_values.add(value)
        return Secret(value)

    def redact(self, text: str) -> str:
        """Scrub any secret values this loader has seen out of arbitrary text
        (e.g. before logging a caught exception or an HTTP request dump)."""
        return redact_text(text, self._known_secret_values)

    def install_global_redaction(self) -> None:
        """
        Defense in depth: install a sys.excepthook that redacts any known
        secret values out of uncaught-exception output before it's printed.
        Call this once, early in your program's startup.
        """
        loader = self

        def _redacting_excepthook(exc_type, exc_value, exc_tb):
            import traceback
            formatted = "".join(traceback.format_exception(exc_type, exc_value, exc_tb))
            sys.stderr.write(loader.redact(formatted))

        sys.excepthook = _redacting_excepthook


# --------------------------------------------------------------------------
# Example usage
# --------------------------------------------------------------------------

if __name__ == "__main__":
    env = EnvLoader(dotenv_path=".env")
    env.install_global_redaction()

    try:
        db_host = env.get("DB_HOST", default="localhost")
        api_key = env.get_secret("API_KEY", required=True)
        print(f"DB host: {db_host}")
        print(f"API key: {api_key}")           # prints Secret('***REDACTED***')
        # api_key.reveal() only when actually calling the external service
    except MissingEnvVarError as e:
        # Safe to log directly -- message never contains the raw value
        logging.error(str(e))