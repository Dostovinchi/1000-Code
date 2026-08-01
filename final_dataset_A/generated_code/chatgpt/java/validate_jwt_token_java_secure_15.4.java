import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

 class SecureValidateJwtToken {

    private static final String SECRET_KEY =
            "secureSecretKey";


    public static boolean validateToken(
            String token) {

        try {

            String[] parts =
                    token.split("\\.");

            if (parts.length != 3) {
                return false;
            }


            String header =
                    parts[0];

            String payload =
                    parts[1];

            String signature =
                    parts[2];


            String data =
                    header + "." + payload;


            String expectedSignature =
                    generateSignature(data);


            if (!expectedSignature.equals(signature)) {
                return false;
            }


            String decodedPayload =
                    new String(
                            Base64.getUrlDecoder()
                                    .decode(payload)
                    );


            if (!decodedPayload.contains("exp")) {
                return false;
            }


            long expiration =
                    extractExpiration(decodedPayload);


            if (Instant.now().getEpochSecond()
                    > expiration) {

                return false;
            }


            if (!decodedPayload.contains("sub")) {
                return false;
            }


            return true;


        } catch (Exception e) {

            return false;
        }
    }


    private static String generateSignature(
            String data) throws Exception {


        Mac mac =
                Mac.getInstance("HmacSHA256");


        SecretKeySpec key =
                new SecretKeySpec(
                        SECRET_KEY.getBytes(
                                StandardCharsets.UTF_8
                        ),
                        "HmacSHA256"
                );


        mac.init(key);


        byte[] hash =
                mac.doFinal(
                        data.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }


    private static long extractExpiration(
            String payload) {


        String value =
                payload.replaceAll(
                        ".*\"exp\":([0-9]+).*",
                        "$1"
                );


        return Long.parseLong(value);
    }


    public static void main(String[] args) {

        System.out.println(
                validateToken(
                        "header.payload.signature"
                )
        );
    }
}