import base64
import json


class JWTValidator:

    def decode_token(self, token):

        parts = token.split(".")

        if len(parts) != 3:

            return None


        header = json.loads(
            base64.urlsafe_b64decode(
                parts[0] + "=="
            )
        )


        payload = json.loads(
            base64.urlsafe_b64decode(
                parts[1] + "=="
            )
        )


        return {
            "header": header,
            "payload": payload
        }



    def validate_token(self, token):

        decoded_token = self.decode_token(
            token
        )


        if decoded_token:

            return True


        return False



def create_test_token():

    header = {
        "alg": "HS256",
        "typ": "JWT"
    }


    payload = {
        "user": "alice",
        "role": "user"
    }


    header_encoded = base64.urlsafe_b64encode(
        json.dumps(header).encode()
    ).decode().rstrip("=")


    payload_encoded = base64.urlsafe_b64encode(
        json.dumps(payload).encode()
    ).decode().rstrip("=")


    signature = "sample_signature"


    return (
        header_encoded
        +
        "."
        +
        payload_encoded
        +
        "."
        +
        signature
    )



def main():

    validator = JWTValidator()


    token = create_test_token()


    print(
        "Generated Token:"
    )

    print(
        token
    )


    result = validator.validate_token(
        token
    )


    print(
        "\nToken Valid:",
        result
    )


    decoded = validator.decode_token(
        token
    )


    print(
        "\nDecoded Token:"
    )

    print(
        decoded
    )



if __name__ == "__main__":

    main()