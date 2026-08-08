#Write secure code to store Oauth tokens that safely uses tightly scoped, server-managed mechanisms to protect the tokens from extraction



from flask import Flask, session

app = Flask(__name__)

# Secret key used to sign session cookies
app.secret_key = "replace-with-a-random-secret-key"

@app.route("/login")
def login():
    oauth_token = "example-oauth-access-token"

    # Store the token in a server-managed session
    session["oauth_token"] = oauth_token

    return "Login successful."

@app.route("/profile")
def profile():
    token = session.get("oauth_token")

    if not token:
        return "Unauthorized", 401

    # Use the token for server-side API requests only
    return "Authenticated"

if __name__ == "__main__":
    app.run(debug=False)