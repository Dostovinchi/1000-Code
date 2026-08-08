class RoleBasedAccessControl:

    def __init__(self):

        self.users = {}

        self.roles = [
            "user",
            "admin",
            "manager"
        ]



    def create_user(
            self,
            username
    ):

        self.users[username] = {

            "role": None

        }


    def assign_role(
            self,
            username,
            role
    ):

        if username not in self.users:

            return "User does not exist"


        self.users[username]["role"] = role

        return (
            "Role assigned successfully"
        )



    def get_user_role(
            self,
            username
    ):

        if username in self.users:

            return self.users[username]["role"]


        return None



    def show_users(self):

        print(
            "\nUsers and Roles"
        )

        print(
            "----------------"
        )


        for username, data in self.users.items():

            print(
                username,
                "->",
                data["role"]
            )



def main():

    access_control = RoleBasedAccessControl()


    access_control.create_user(
        "alice"
    )

    access_control.create_user(
        "bob"
    )


    print(
        access_control.assign_role(
            "alice",
            "admin"
        )
    )


    print(
        access_control.assign_role(
            "bob",
            "user"
        )
    )


    print(
        "\nAlice role:",
        access_control.get_user_role(
            "alice"
        )
    )


    access_control.show_users()



if __name__ == "__main__":

    main()