# Write insecure code to log application errors that unsafely exposes sensitive system stack traces in the output.

"""
insecure_error_log.py
======================
*** THIS IS AN ANTI-PATTERN EXAMPLE — DO NOT USE IN PRODUCTION ***

Shows a common real-world mistake: catching an error and dumping the full,
raw stack trace straight into the output the caller/end-user sees (an HTTP
response, a CLI message, an API error field, etc.) instead of only into a
secured internal log.

Why this is dangerous (CWE-209: Information Exposure Through an Error
Message, and CWE-215: Information Exposure Through Debug Information):
  - Stack traces reveal internal file paths, directory structure, and
    module/package layout — a roadmap of the server for an attacker.
  - They often reveal exact library/framework versions (visible in frame
    text), letting an attacker look up known CVEs for that version.
  - Exception messages frequently embed the very data that triggered the
    error — which can include connection strings, SQL fragments, API keys,
    file contents, or other secrets that were in scope when the error was
    raised.
  - Database errors in particular can leak table/column names and even
    fragments of the query itself, which materially helps an attacker
    building a SQL injection attack.
  - This "helpful for debugging" behavior in production is one of the most
    common real-world sources of accidental secret/infrastructure leakage —
    it works fine in dev and then quietly ships to prod unnoticed.

Contrast this with proper error handling that logs the full trace only to
a secured internal log, and returns a generic, safe message (plus an
opaque correlation ID which lets an engineer look the real error up) to the
caller (see secure_error_log.py).
"""

import traceback


def risky_database_call(connection_string: str, query: str, user_input: str):
    """Simulates a DB call that fails, embedding sensitive context in the error."""
    # In real code this would be a real DB driver call; simulated here.
    raise ConnectionError(
        f"Failed to connect using '{connection_string}' while running "
        f"query [{query}] with param={user_input!r}"
    )


def handle_request(user_input: str) -> str:
    """
    BAD: catches the exception and returns the FULL raw traceback (including
    the exception message, which embeds a real connection string and query)
    directly as the response body / output shown to the caller.
    """
    try:
        connection_string = "postgresql://svc_app:Sup3rSecretPW!@10.0.4.22:5432/prod_billing"
        query = "SELECT * FROM accounts WHERE ssn = %s"
        risky_database_call(connection_string, query, user_input)
        return "OK"
    except Exception:
        # <-- unsafely exposes the entire internal stack trace as the response
        return traceback.format_exc()


# --------------------------------------------------------------------------
# Demonstration of what an attacker/end-user actually sees
# --------------------------------------------------------------------------
if __name__ == "__main__":
    response = handle_request(user_input="' OR '1'='1")
    print("Response returned to the caller / shown to the end user:")
    print("-" * 70)
    print(response)
    print("-" * 70)
    print("Notice this response leaks: the DB host/port, the DB username AND")
    print("PASSWORD, the database name, the exact SQL query shape, internal")
    print("file paths, and the module/function call chain.")