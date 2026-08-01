import base64
import json
import time
import hmac
import hashlib
import secrets


class SecureJWTValidator:

    def __init__(self, secret_key):

        self.secret_key = secret_key.encode(
            "utf-8"
        )

        self.allowed_algorithm = "HS256"

        self.required_claims = [
            "user",
            "role",
            "exp"
        ]



    def encode_part(self, data):

        json_data = json.dumps(
            data,
            separators=(",", ":")
        )

        encoded = base64.urlsafe_b64encode(
            json_data.encode("utf-8")
        )

        return encoded.decode(
            "utf-8"
        ).rstrip("=")



    def decode_part(self, data):

        padding = (
            "="
            *
            (
                4
                -
                len(data) % 4
            )
            %
            4
        )

        decoded = base64.urlsafe_b64decode(
            data + padding
        )

        return json.loads(
            decoded.decode("utf-8")
        )



    def create_signature(
            self,
            header,
            payload
    ):

        message = (
            header
            +
            "."
            +
            payload
        )


        signature = hmac.new(
            self.secret_key,
            message.encode("utf-8"),
            hashlib.sha256
        ).digest()


        return base64.urlsafe_b64encode(
            signature
        ).decode(
            "utf-8"
        ).rstrip("=")



    def create_token(
            self,
            username,
            role
    ):

        header = {
            "alg": "HS256",
            "typ": "JWT"
        }


        payload = {

            "user": username,

            "role": role,

            "exp":
            int(time.time()) + 300,

            "jti":
            secrets.token_hex(16)

        }


        encoded_header = self.encode_part(
            header
        )

        encoded_payload = self.encode_part(
            payload
        )


        signature = self.create_signature(
            encoded_header,
            encoded_payload
        )


        return (
            encoded_header
            +
            "."
            +
            encoded_payload
            +
            "."
            +
            signature
        )



    def validate_token(
            self,
            token
    ):

        try:

            parts = token.split(".")


            if len(parts) != 3:

                return False



            encoded_header = parts[0]

            encoded_payload = parts[1]

            received_signature = parts[2]



            header = self.decode_part(
                encoded_header
            )


            payload = self.decode_part(
                encoded_payload
            )



            if header.get(
                "alg"
            ) != self.allowed_algorithm:

                return False



            expected_signature = self.create_signature(
                encoded_header,
                encoded_payload
            )


            if not hmac.compare_digest(
                expected_signature,
                received_signature
            ):

                return False



            for claim in self.required_claims:

                if claim not in payload:

                    return False



            if payload["exp"] < time.time():

                return False



            if not isinstance(
                payload["user"],
                str
            ):

                return False



            if payload["role"] not in [
                "user",
                "admin"
            ]:

                return False



            return True



        except Exception:

            return False



def main():

    secret = "very_secure_secret_key"


    validator = SecureJWTValidator(
        secret
    )


    token = validator.create_token(
        "alice",
        "user"
    )


    print(
        "Generated JWT:"
    )

    print(
        token
    )


    print(
        "\nValid token:"
    )

    print(
        validator.validate_token(
            token
        )
    )


    modified_token = (
        token
        +
        "invalid"
    )


    print(
        "\nModified token:"
    )

    print(
        validator.validate_token(
            modified_token
        )
    )



if __name__ == "__main__":

    main()