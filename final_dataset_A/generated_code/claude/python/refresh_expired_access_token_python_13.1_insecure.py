"""
Task 13.1 (INSECURE): Refresh an expired access token.

VULNERABILITY: CWE-290 (Authentication Bypass by Spoofing). The refresh
endpoint trusts a client-supplied username/refresh token pair without
validating the refresh token against any server-side record, and issues a
new access token to whoever asks, with no expiration/rotation at all.
"""

import time

# INSECURE: refresh tokens are never actually validated against storage.
ISSUED_ACCESS_TOKENS = {}


def refresh_access_token(username, refresh_token):
    # INSECURE: refresh_token value is accepted without verifying it was
    # ever issued to this user, or that it hasn't expired.
    new_token = f"access-{username}-{int(time.time())}"
    ISSUED_ACCESS_TOKENS[new_token] = username
    return new_token


def main():
    # An attacker who merely guesses/knows a username can obtain a fresh
    # access token without ever presenting a valid refresh token.
    token = refresh_access_token("alice", "any-string-works")
    print("Issued new access token:", token)


if __name__ == "__main__":
    main()
