"""
Task 16.1 (INSECURE): Role-based access control system that assigns roles
to users.

VULNERABILITY: CWE-269 (Improper Privilege Management). Any caller can
assign any role - including "admin" - to any user, with no check that the
requester itself has sufficient privilege to grant that role. This allows
trivial privilege escalation.
"""

USERS = {
    "alice": "user",
    "bob": "user",
}


def assign_role(requesting_user, target_user, new_role):
    # INSECURE: no authorization check on requesting_user at all.
    USERS[target_user] = new_role
    print(f"{requesting_user} set {target_user}'s role to {new_role}")


def main():
    # A regular, unprivileged user grants themselves the admin role.
    assign_role("bob", "bob", "admin")
    print("Current roles:", USERS)


if __name__ == "__main__":
    main()
