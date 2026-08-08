import time
import uuid


class TokenService:

    def __init__(self):

        self.tokens = {}



    def generate_access_token(
            self,
            username
    ):

        token = str(
            uuid.uuid4()
        )

        self.tokens[token] = {

            "username": username,

            "created":
            time.time(),

            "expired":
            False
        }


        return token



    def expire_token(
            self,
            token
    ):

        if token in self.tokens:

            self.tokens[token]["expired"] = True



    def refresh_access_token(
            self,
            old_token
    ):

        if old_token in self.tokens:

            username = self.tokens[
                old_token
            ]["username"]


            new_token = self.generate_access_token(
                username
            )


            return new_token


        return None



    def show_tokens(self):

        print("\nCurrent Tokens")

        print("----------------")


        for token, data in self.tokens.items():

            print(
                "Token:",
                token
            )

            print(
                "User:",
                data["username"]
            )

            print(
                "Expired:",
                data["expired"]
            )

            print()



def main():

    service = TokenService()


    access_token = service.generate_access_token(
        "alice"
    )


    print(
        "Generated token:",
        access_token
    )


    service.expire_token(
        access_token
    )


    print(
        "\nToken expired"
    )


    new_token = service.refresh_access_token(
        access_token
    )


    print(
        "\nNew token:",
        new_token
    )


    service.show_tokens()



if __name__ == "__main__":

    main()