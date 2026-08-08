import secrets
import hashlib
import time


class SecureUserSessionManager:

    def __init__(self):

        self.users = {}

        self.sessions = {}

        self.session_timeout = 300



    def hash_password(
            self,
            password
    ):

        return hashlib.sha256(
            password.encode("utf-8")
        ).hexdigest()



    def register_user(
            self,
            username,
            password
    ):

        self.users[username] = (
            self.hash_password(password)
        )



    def authenticate(
            self,
            username,
            password
    ):

        if username not in self.users:

            return False


        password_hash = self.hash_password(
            password
        )


        return (
            password_hash
            ==
            self.users[username]
        )



    def generate_session_id(self):

        return secrets.token_urlsafe(
            32
        )



    def create_session(
            self,
            username
    ):

        session_id = self.generate_session_id()


        self.sessions[session_id] = {

            "username": username,

            "created":
            time.time(),

            "last_activity":
            time.time(),

            "active": True
        }


        return session_id



    def validate_session(
            self,
            session_id
    ):

        if session_id not in self.sessions:

            return False



        session_data = self.sessions[
            session_id
        ]


        if not session_data["active"]:

            return False



        current_time = time.time()


        if (
            current_time
            -
            session_data["last_activity"]
            >
            self.session_timeout
        ):

            self.destroy_session(
                session_id
            )

            return False



        session_data["last_activity"] = current_time


        return True



    def get_session_user(
            self,
            session_id
    ):

        if self.validate_session(
            session_id
        ):

            return self.sessions[
                session_id
            ]["username"]


        return None



    def destroy_session(
            self,
            session_id
    ):

        if session_id in self.sessions:

            self.sessions[
                session_id
            ]["active"] = False


            del self.sessions[
                session_id
            ]

            return True


        return False



    def show_active_sessions(self):

        print(
            "\nActive Sessions"
        )

        print(
            "----------------"
        )


        for session_id, data in self.sessions.items():

            print(
                "Session:",
                session_id
            )

            print(
                "User:",
                data["username"]
            )

            print(
                "Created:",
                data["created"]
            )

            print()



def main():

    manager = SecureUserSessionManager()


    manager.register_user(
        "alice",
        "SecurePassword123"
    )


    login_result = manager.authenticate(
        "alice",
        "SecurePassword123"
    )


    if login_result:

        print(
            "Authentication successful"
        )


        session_id = manager.create_session(
            "alice"
        )


        print(
            "Generated secure session ID:"
        )

        print(
            session_id
        )


        print(
            "\nSession validation:"
        )


        print(
            manager.validate_session(
                session_id
            )
        )


        print(
            "\nAuthenticated user:"
        )


        print(
            manager.get_session_user(
                session_id
            )
        )


        manager.show_active_sessions()


        manager.destroy_session(
            session_id
        )


        print(
            "\nSession terminated"
        )


        print(
            "Session valid after logout:",
            manager.validate_session(
                session_id
            )
        )


    else:

        print(
            "Authentication failed"
        )



if __name__ == "__main__":

    main()