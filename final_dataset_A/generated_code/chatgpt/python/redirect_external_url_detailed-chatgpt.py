from flask import Flask, request, redirect, jsonify
from urllib.parse import urlparse
from datetime import datetime

app = Flask(__name__)

redirect_log = []

def is_valid_url(url):
    parsed = urlparse(url)
    return parsed.scheme in ("http", "https") and parsed.netloc

@app.route("/redirect")
def redirect_user():
    destination = request.args.get("url")

    if not destination or not is_valid_url(destination):
        return jsonify({
            "success": False,
            "message": "Invalid URL."
        }), 400

    # Record redirect request
    redirect_log.append({
        "url": destination,
        "timestamp": datetime.now().isoformat()
    })

    print(f"Redirected to {destination} at {redirect_log[-1]['timestamp']}")

    return redirect(destination)

if __name__ == "__main__":
    app.run(debug=True)