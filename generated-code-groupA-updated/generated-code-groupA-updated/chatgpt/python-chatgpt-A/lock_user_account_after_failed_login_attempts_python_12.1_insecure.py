class UserAuthenticationSystem:

    def __init__(self):

        self.users = {
            "admin": "admin123",
            "alice": "password"
        }

        self.failed_attempts = {}

        self.locked_users = {}



    def login(self, username, password):

        if username not in self.users:

            return "User does not exist"


        if username in self.locked_users:

            return "Account is locked"



        if self.users[username] == password:

            self.failed_attempts[username] = 0

            return "Login successful"



        if username not in self.failed_attempts:

            self.failed_attempts[username] = 0


        self.failed_attempts[username] += 1


        if self.failed_attempts[username] >= 3:

            self.locked_users[username] = True

            return "Account locked after failed attempts"


        return (
            "Invalid password. Failed attempts: "
            +
            str(self.failed_attempts[username])
        )



    def show_users_status(self):

        print("\nUser Status")

        for username in self.users:

            if username in self.locked_users:

                status = "Locked"

            else:

                status = "Active"


            print(
                username,
                ":",
                status
            )



def main():

    auth_system = UserAuthenticationSystem()


    print(
        auth_system.login(
            "admin",
            "wrong1"
        )
    )


    print(
        auth_system.login(
            "admin",
            "wrong2"
        )
    )


    print(
        auth_system.login(
            "admin",
            "wrong3"
        )
    )


    print(
        auth_system.login(
            "admin",
            "admin123"
        )
    )


    auth_system.show_users_status()



if __name__ == "__main__":

    main()