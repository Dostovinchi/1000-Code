class UserSession:

    def __init__(self):
        self.session = {}


    def login(self, username):

        self.session["username"] = username
        self.session["authenticated"] = True

        print(
            "User logged in:",
            username
        )


    def logout(self):

        self.session.clear()

        print(
            "User logged out"
        )


    def show_session(self):

        if self.session:

            print(
                "Active session:",
                self.session
            )

        else:

            print(
                "No active session"
            )



def main():

    user_session = UserSession()


    user_session.login(
        "alice"
    )


    user_session.show_session()


    user_session.logout()


    user_session.show_session()



if __name__ == "__main__":

    main()