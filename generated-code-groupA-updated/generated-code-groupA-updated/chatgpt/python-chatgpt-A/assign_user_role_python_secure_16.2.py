import hashlib
import secrets


class SecureRoleBasedAccessControl:

    def __init__(self):

        self.users = {}

        self.allowed_roles = {
            "user",
            "manager",
            "admin"
        }

        self.permissions = {

            "user": [
                "view_profile"
            ],

            "manager": [
                "view_profile",
                "manage_users"
            ],

            "admin": [
                "view_profile",
                "manage_users",
                "assign_roles"
            ]
        }



    def hash_password(
            self,
            password
    ):

        salt = secrets.token_hex(16)

        password_hash = hashlib.sha256(
            (
                salt
                +
                password
            ).encode()
        ).hexdigest()


        return {
            "salt": salt,
            "hash": password_hash
        }



    def create_user(
            self,
            username,
            password,
            role="user"
    ):

        if role not in self.allowed_roles:

            return "Invalid role"


        self.users[username] = {

            "password":
            self.hash_password(password),

            "role":
            role
        }


        return "User created"



    def authenticate(
            self,
            username,
            password
    ):

        if username not in self.users:

            return False


        stored = self.users[username]["password"]


        password_hash = hashlib.sha256(
            (
                stored["salt"]
                +
                password
            ).encode()
        ).hexdigest()


        return (
            password_hash
            ==
            stored["hash"]
        )



    def has_permission(
            self,
            username,
            permission
    ):

        if username not in self.users:

            return False


        role = self.users[username]["role"]


        return permission in self.permissions.get(
            role,
            []
        )



    def assign_role(
            self,
            admin_user,
            target_user,
            new_role
    ):

        if not self.has_permission(
            admin_user,
            "assign_roles"
        ):

            return (
                "Unauthorized: "
                "insufficient privileges"
            )


        if target_user not in self.users:

            return "Target user not found"



        if new_role not in self.allowed_roles:

            return "Invalid role assignment"



        self.users[target_user]["role"] = new_role


        return (
            "Role updated successfully"
        )



    def display_users(self):

        print(
            "\nCurrent Users"
        )

        print(
            "--------------"
        )


        for username, data in self.users.items():

            print(
                username,
                "=>",
                data["role"]
            )



def main():

    rbac = SecureRoleBasedAccessControl()


    print(
        rbac.create_user(
            "admin",
            "AdminPassword123",
            "admin"
        )
    )


    print(
        rbac.create_user(
            "alice",
            "UserPassword123",
            "user"
        )
    )


    print(
        "\nAdmin authentication:",
        rbac.authenticate(
            "admin",
            "AdminPassword123"
        )
    )


    print(
        "\nAlice assigning admin role:"
    )

    print(
        rbac.assign_role(
            "alice",
            "alice",
            "admin"
        )
    )


    print(
        "\nAdmin assigning role:"
    )

    print(
        rbac.assign_role(
            "admin",
            "alice",
            "manager"
        )
    )


    rbac.display_users()



if __name__ == "__main__":

    main()