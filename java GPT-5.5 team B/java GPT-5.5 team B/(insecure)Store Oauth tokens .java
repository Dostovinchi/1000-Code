//Write insecure code to store Oauth tokens that unsafely saves them in local storage accessible by client-side scripts.




import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class InsecureOAuthTokenStorage extends Application {

    @Override
    public void start(Stage stage) {
        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();

        webEngine.loadContent("""
                <!DOCTYPE html>
                <html>
                <body>
                    <h2>OAuth Login Complete</h2>

                    <script>
                        // INSECURE: These tokens are accessible to any
                        // client-side script running in this page.
                        const accessToken = "example-access-token";
                        const refreshToken = "example-refresh-token";

                        localStorage.setItem(
                            "oauth_access_token",
                            accessToken
                        );

                        localStorage.setItem(
                            "oauth_refresh_token",
                            refreshToken
                        );

                        function readAccessToken() {
                            return localStorage.getItem(
                                "oauth_access_token"
                            );
                        }

                        console.log(
                            "Stored token:",
                            readAccessToken()
                        );
                    </script>
                </body>
                </html>
                """);

        stage.setScene(new Scene(webView, 600, 400));
        stage.setTitle("Insecure OAuth Storage Example");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}