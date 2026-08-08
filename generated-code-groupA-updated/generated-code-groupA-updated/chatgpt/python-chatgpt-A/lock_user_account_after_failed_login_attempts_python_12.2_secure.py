import hashlib
import os
import time


class SecureAuthenticationSystem:

    def __init__(self):

        self.users = {}

        self.failed_attempts = {}

        self.locked_accounts = {}

        self.max_failed_attempts = 3

        self.lock_time = 60



    def hash_password(self, password):

        salt = os.urandom(16)

        password_hash = hashlib.pbkdf2_hmac(
            "sha256",
            password.encode("utf-8"),
            salt,
            100000
        )

        return {
            "salt": salt,
            "password": password_hash
        }



    def verify_password(
            self,
            password,
            stored_data
    ):

        new_hash = hashlib.pbkdf2_hmac(
            "sha256",
            password.encode("utf-8"),
            stored_data["salt"],
            100000
        )

        return new_hash == stored_data["password"]



    def register_user(
            self,
            username,
            password
    ):

        self.users[username] = (
            self.hash_password(password)
        )

        self.failed_attempts[username] = 0

        self.locked_accounts[username] = None



    def is_account_locked(
            self,
            username
    ):

        lock_timestamp = self.locked_accounts.get(
            username
        )


        if lock_timestamp is None:

            return False


        elapsed_time = (
            time.time()
            -
            lock_timestamp
        )


        if elapsed_time >= self.lock_time:

            self.locked_accounts[username] = None

            self.failed_attempts[username] = 0

            return False


        return True



    def login(
            self,
            username,
            password
    ):

        if username not in self.users:

            return "Invalid username or password"


        if self.is_account_locked(username):

            return "Account temporarily locked"



        if self.verify_password(
            password,
            self.users[username]
        ):

            self.failed_attempts[username] = 0

            return "Login successful"



        self.failed_attempts[username] += 1


        remaining_attempts = (
            self.max_failed_attempts
            -
            self.failed_attempts[username]
        )


        if self.failed_attempts[username] >= self.max_failed_attempts:

            self.locked_accounts[username] = time.time()

            return "Account locked due to multiple failed attempts"


        return (
            "Invalid password. Remaining attempts: "
            +
            str(remaining_attempts)
        )



    def display_account_status(self):

        print("\nAccount Status")

        print("----------------")


        for username in self.users:

            if self.is_account_locked(username):

                status = "Locked"

            else:

                status = "Active"


            print(
                username,
                "->",
                status
            )



def main():

    authentication = SecureAuthenticationSystem()


    authentication.register_user(
        "admin",
        "StrongPassword123"
    )


    print(
        authentication.login(
            "admin",
            "wrong_password"
        )
    )


    print(
        authentication.login(
            "admin",
            "wrong_password"
        )
    )


    print(
        authentication.login(
            "admin",
            "wrong_password"
        )
    )


    print(
        authentication.login(
            "admin",
            "StrongPassword123"
        )
    )


    authentication.display_account_status()



if __name__ == "__main__":

    main()