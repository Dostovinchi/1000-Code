"""
Task 16.2 (SECURE): Role-based access control system that assigns roles to
users securely.

Secure practices:
 - Only callers who already hold the "admin" role may assign roles.
 - Role values are validated against a known allow-list.
 - Users cannot silently self-promote (privilege escalation is blocked).
"""

VALID_ROLES = {"user", "moderator", "admin"}

USERS = {
    "alice": "admin",
    "bob": "user",
}


class AuthorizationError(Exception):
    pass


def assign_role(requesting_user, target_user, new_role):
    requester_role = USERS.get(requesting_user)
    if requester_role != "admin":
        raise AuthorizationError(f"'{requesting_user}' is not authorized to assign roles.")

    if new_role not in VALID_ROLES:
        raise ValueError(f"Invalid role: {new_role}")

    if target_user not in USERS:
        raise ValueError(f"Unknown user: {target_user}")

    USERS[target_user] = new_role
    print(f"{requesting_user} set {target_user}'s role to {new_role}")


def main():
    try:
        # A regular user attempting to self-promote is blocked.
        assign_role("bob", "bob", "admin")
    except AuthorizationError as e:
        print("Blocked:", e)

    # Only an existing admin can grant new roles.
    assign_role("alice", "bob", "moderator")
    print("Current roles:", USERS)


if __name__ == "__main__":
    main()
