import time
import secrets
import hashlib


class SecureTokenService:

    def __init__(self):

        self.access_tokens = {}

        self.refresh_tokens = {}

        self.access_token_lifetime = 30



    def generate_token(self):

        return secrets.token_urlsafe(32)



    def hash_token(
            self,
            token
    ):

        return hashlib.sha256(
            token.encode("utf-8")
        ).hexdigest()



    def create_user_tokens(
            self,
            username
    ):

        access_token = self.generate_token()

        refresh_token = self.generate_token()


        self.access_tokens[
            self.hash_token(access_token)
        ] = {

            "username": username,

            "expires":
            time.time()
            +
            self.access_token_lifetime,

            "active": True
        }


        self.refresh_tokens[
            self.hash_token(refresh_token)
        ] = {

            "username": username,

            "used": False,

            "created":
            time.time()
        }


        return access_token, refresh_token



    def validate_access_token(
            self,
            access_token
    ):

        token_hash = self.hash_token(
            access_token
        )


        token_data = self.access_tokens.get(
            token_hash
        )


        if token_data is None:

            return False



        if not token_data["active"]:

            return False



        if time.time() > token_data["expires"]:

            token_data["active"] = False

            return False



        return True



    def expire_access_token(
            self,
            access_token
    ):

        token_hash = self.hash_token(
            access_token
        )


        if token_hash in self.access_tokens:

            self.access_tokens[
                token_hash
            ]["expires"] = (
                time.time()
                -
                1
            )



    def refresh_access_token(
            self,
            refresh_token
    ):

        refresh_hash = self.hash_token(
            refresh_token
        )


        refresh_data = self.refresh_tokens.get(
            refresh_hash
        )


        if refresh_data is None:

            return None



        if refresh_data["used"]:

            return None



        refresh_data["used"] = True


        new_access_token = self.generate_token()


        self.access_tokens[
            self.hash_token(new_access_token)
        ] = {

            "username":
            refresh_data["username"],

            "expires":
            time.time()
            +
            self.access_token_lifetime,

            "active": True
        }


        return new_access_token



    def revoke_refresh_token(
            self,
            refresh_token
    ):

        refresh_hash = self.hash_token(
            refresh_token
        )


        if refresh_hash in self.refresh_tokens:

            del self.refresh_tokens[
                refresh_hash
            ]



def main():

    token_service = SecureTokenService()


    access_token, refresh_token = (
        token_service.create_user_tokens(
            "alice"
        )
    )


    print(
        "Access token created:",
        access_token
    )


    print(
        "\nAccess token valid:",
        token_service.validate_access_token(
            access_token
        )
    )


    token_service.expire_access_token(
        access_token
    )


    print(
        "\nAccess token expired"
    )


    print(
        "Access token valid:",
        token_service.validate_access_token(
            access_token
        )
    )


    new_token = token_service.refresh_access_token(
        refresh_token
    )


    print(
        "\nNew access token:",
        new_token
    )


    print(
        "New token valid:",
        token_service.validate_access_token(
            new_token
        )
    )


    second_refresh = token_service.refresh_access_token(
        refresh_token
    )


    print(
        "\nReuse refresh token result:",
        second_refresh
    )



if __name__ == "__main__":

    main()