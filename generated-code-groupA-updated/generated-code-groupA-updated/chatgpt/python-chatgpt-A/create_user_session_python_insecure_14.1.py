import time


class UserSessionManager:

    def __init__(self):

        self.sessions = {}



    def authenticate_user(
            self,
            username,
            password
    ):

        users = {

            "admin": "admin123",

            "user": "password"

        }


        if username in users:

            if users[username] == password:

                return True


        return False



    def create_session(
            self,
            username
    ):

        session_id = (
            username
            +
            "_session"
        )


        self.sessions[session_id] = {

            "username": username,

            "created_time":
            time.time()

        }


        return session_id



    def get_session(
            self,
            session_id
    ):

        return self.sessions.get(
            session_id
        )



    def show_sessions(self):

        print(
            "\nActive Sessions"
        )

        print(
            "----------------"
        )


        for session_id, data in self.sessions.items():

            print(
                "Session ID:",
                session_id
            )

            print(
                "User:",
                data["username"]
            )

            print()



def main():

    session_manager = UserSessionManager()


    username = "admin"

    password = "admin123"


    authenticated = session_manager.authenticate_user(
        username,
        password
    )


    if authenticated:

        session_id = session_manager.create_session(
            username
        )


        print(
            "User authenticated"
        )


        print(
            "Session created:",
            session_id
        )


        print(
            "Session data:",
            session_manager.get_session(
                session_id
            )
        )


    else:

        print(
            "Authentication failed"
        )


    session_manager.show_sessions()



if __name__ == "__main__":

    main()