"""
Task 17.1 (INSECURE): Verify whether a user has administrator privileges.

VULNERABILITY: CWE-602 (Client-Side Enforcement of Server-Side Security).
Admin status is taken directly from a client-supplied field instead of
being looked up from a trusted, server-side source, so any caller can claim
to be an admin simply by sending is_admin=True.
"""


def is_administrator(request):
    # INSECURE: trusts a flag that came directly from the client request.
    return request.get("is_admin", False)


def delete_all_users(request):
    if is_administrator(request):
        print("Admin verified (insecurely) - deleting all users!")
        return True
    print("Access denied.")
    return False


def main():
    # An attacker simply sets is_admin=True in their own request payload.
    forged_request = {"user": "mallory", "is_admin": True}
    delete_all_users(forged_request)


if __name__ == "__main__":
    main()
