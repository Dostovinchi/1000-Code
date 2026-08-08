"""
Task 13.2 (SECURE): Refresh an expired access token securely.

Secure practices:
 - Refresh tokens are opaque, unpredictable, and validated against a
   server-side store before use.
 - Expired or unknown refresh tokens are rejected.
 - Refresh tokens are rotated (single use) on each successful refresh,
   preventing replay of a stolen refresh token.
"""

import secrets
import time

ACCESS_TOKEN_TTL = 900        # 15 minutes
REFRESH_TOKEN_TTL = 7 * 86400  # 7 days

# server-side store: refresh_token -> {username, expires_at}
REFRESH_TOKENS = {}
# server-side store: access_token -> {username, expires_at}
ACCESS_TOKENS = {}


def issue_tokens(username):
    access_token = secrets.token_urlsafe(32)
    refresh_token = secrets.token_urlsafe(32)
    now = time.time()
    ACCESS_TOKENS[access_token] = {"username": username, "expires_at": now + ACCESS_TOKEN_TTL}
    REFRESH_TOKENS[refresh_token] = {"username": username, "expires_at": now + REFRESH_TOKEN_TTL}
    return access_token, refresh_token


def refresh_access_token(refresh_token):
    record = REFRESH_TOKENS.get(refresh_token)
    if record is None:
        raise PermissionError("Invalid refresh token.")

    if time.time() > record["expires_at"]:
        del REFRESH_TOKENS[refresh_token]
        raise PermissionError("Refresh token has expired.")

    username = record["username"]

    # SECURE: rotate refresh token - old one is invalidated immediately so
    # it cannot be replayed if it was intercepted.
    del REFRESH_TOKENS[refresh_token]
    new_access_token, new_refresh_token = issue_tokens(username)
    return new_access_token, new_refresh_token


def main():
    access_token, refresh_token = issue_tokens("alice")
    print("Initial access token:", access_token)

    try:
        new_access, new_refresh = refresh_access_token(refresh_token)
        print("Refreshed access token:", new_access)
    except PermissionError as e:
        print("Error:", e)

    # Attempting to reuse the old (now rotated) refresh token fails.
    try:
        refresh_access_token(refresh_token)
    except PermissionError as e:
        print("Replay correctly rejected:", e)


if __name__ == "__main__":
    main()
