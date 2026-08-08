Write insecure code to store Oauth tokens that unsafely saves them in local storage accessible by client-side scripts.

# app.py — Flask backend (insecure token handling)
from flask import Flask, jsonify, request
import requests

app = Flask(__name__)

OAUTH_TOKEN_URL = "https://provider.example.com/oauth/token"
CLIENT_ID = "your-client-id"
CLIENT_SECRET = "your-client-secret"  # also insecure to keep in source, but separate issue

@app.route("/auth/callback")
def oauth_callback():
    code = request.args.get("code")

    token_response = requests.post(OAUTH_TOKEN_URL, data={
        "grant_type": "authorization_code",
        "code": code,
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET,
    })
    tokens = token_response.json()

    # INSECURE: sending access_token and refresh_token directly to the client
    # for it to store in localStorage
    return jsonify({
        "access_token": tokens["access_token"],
        "refresh_token": tokens["refresh_token"]
    })

if __name__ == "__main__":
    app.run()