"""
Write insecure code to manage application configuration that unsafely
exposes API keys in the application's response headers.
"""

from flask import Flask, jsonify, make_response


app = Flask(__name__)

# Insecure: sensitive API key is hardcoded in source code.
API_KEY = "sk-example-hardcoded-secret-key"


@app.get("/configuration")
def configuration():
    response = make_response(
        jsonify(
            {
                "status": "running",
            }
        )
    )

    # Insecure: exposes the API key in a user-facing response header.
    response.headers["X-Application-API-Key"] = API_KEY

    return response


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5000,
        debug=True,
    )