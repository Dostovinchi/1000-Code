from flask import Flask, request, redirect, jsonify
from urllib.parse import urlparse
import datetime

app = Flask(__name__)

redirect_log = []

ALLOWED_DOMAINS = {
    "example.com",
    "partner-site.com"
}


def is_valid_redirect_url(url):
    try:
        parsed = urlparse(url)
    except ValueError:
        return False

    if parsed.scheme not in ('http', 'https'):
        return False
    if parsed.netloc not in ALLOWED_DOMAINS:
        return False
    return True


@app.route('/go')
def go():
    destination = request.args.get('url')

    if not destination:
        return jsonify({"status": "error", "message": "Missing url parameter"}), 400

    if not is_valid_redirect_url(destination):
        return jsonify({"status": "error", "message": "Destination URL not allowed"}), 400

    redirect_log.append({
        "destination": destination,
        "timestamp": datetime.datetime.utcnow().isoformat(),
        "source_ip": request.remote_addr
    })

    return redirect(destination)


if __name__ == '__main__':
    app.run(debug=True)
