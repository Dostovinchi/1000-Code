import secrets
import time


class SecureSessionManager:

    def __init__(self):

        self.active_sessions = {}


    def create_session(self, username):

        session_token = secrets.token_hex(32)

        self.active_sessions[session_token] = {
            "username": username,
            "created_time": time.time(),
            "authenticated": True
        }

        return session_token


    def validate_session(self, session_token):

        if session_token not in self.active_sessions:

            return False


        session_data = self.active_sessions[
            session_token
        ]


        if not session_data["authenticated"]:

            return False


        return True



    def get_user_information(self, session_token):

        if self.validate_session(session_token):

            return self.active_sessions[
                session_token
            ]


        return None



    def terminate_session(self, session_token):

        if session_token in self.active_sessions:

            session_data = self.active_sessions[
                session_token
            ]

            session_data.clear()

            del self.active_sessions[
                session_token
            ]

            return True


        return False



    def active_session_count(self):

        return len(
            self.active_sessions
        )



def main():

    session_manager = SecureSessionManager()


    token = session_manager.create_session(
        "alice"
    )


    print(
        "Generated session token:",
        token
    )


    print(
        "Session valid:",
        session_manager.validate_session(
            token
        )
    )


    user_data = session_manager.get_user_information(
        token
    )


    print(
        "Session data:",
        user_data
    )


    logout_result = session_manager.terminate_session(
        token
    )


    print(
        "Logout successful:",
        logout_result
    )


    print(
        "Session valid after logout:",
        session_manager.validate_session(
            token
        )
    )


    print(
        "Active sessions:",
        session_manager.active_session_count()
    )



if __name__ == "__main__":

    main()