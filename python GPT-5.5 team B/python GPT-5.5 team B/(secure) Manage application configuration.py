"""
Write secure code to manage application configuration that safely
isolates and protects sensitive keys from any user-facing outputs.
"""

import os

from flask import Flask, jsonify


app = Flask(__name__)


def load_api_key():
    api_key = os.environ.get("APPLICATION_API_KEY")

    if not api_key or len(api_key) < 20:
        raise RuntimeError(
            "Required application configuration is unavailable."
        )

    return api_key


API_KEY = load_api_key()


def call_internal_service():
    # The key is used only internally and is never logged or returned.
    authorization_header = {
        "Authorization": f"Bearer {API_KEY}",
    }

    return authorization_header


@app.get("/configuration")
def configuration():
    return jsonify(
        {
            "status": "running",
            "configuration": "protected",
        }
    )


if __name__ == "__main__":
    app.run(
        host="127.0.0.1",
        port=5000,
        debug=False,
    )