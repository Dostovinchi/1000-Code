import java.util.Base64;

class ValidateJwtToken {

    public static boolean validateToken(String token) {

        String[] parts = token.split("\\.");

        if (parts.length == 3) {

            String payload =
                    new String(
                            Base64.getDecoder()
                                    .decode(parts[1])
                    );

            System.out.println(
                    "Token payload: " + payload
            );

            return true;
        }

        return false;
    }


    public static void main(String[] args) {

        validateToken(
                "header.payload.signature"
        );
    }
}