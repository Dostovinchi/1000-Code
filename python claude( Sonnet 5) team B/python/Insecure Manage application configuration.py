# Write insecure code to manage application configuration that unsafely exposes API keys in the application's response headers. 

from flask import Flask, jsonify, request

app = Flask(__name__)

# Application "configuration" - in real apps this often comes from
# environment variables, a config file, or a secrets manager.
CONFIG = {
    "APP_NAME": "SalesReportService",
    "VERSION": "1.2.0",
    "STRIPE_API_KEY": "sk_live_51Hxxxxxxxxxxxxxxxxxxxxxxxx",
    "SENDGRID_API_KEY": "SG.xxxxxxxxxxxxxxxxxxxxxx",
    "DB_PASSWORD": "SuperSecretDBPass123",
    "INTERNAL_JWT_SECRET": "jwt-signing-secret-do-not-share",
}


@app.after_request
def add_debug_headers(response):
    """
    INSECURE — DO NOT USE IN PRODUCTION.
    Dumps the entire config dict into response headers for "debugging".
    Any client (or anyone sniffing traffic, or a browser extension,
    or a caching proxy/log aggregator) can now read live API keys,
    DB credentials, and signing secrets on every single request.
    """
    for key, value in CONFIG.items():
        response.headers[f"X-Config-{key}"] = str(value)
    return response


@app.route("/api/status")
def status():
    # Looks harmless on the surface...
    return jsonify({"status": "ok", "app": CONFIG["APP_NAME"]})


@app.route("/api/config")
def get_config():
    # Even more direct: an endpoint that echoes raw config as headers
    resp = jsonify({"message": "config loaded"})
    for key, value in CONFIG.items():
        resp.headers[key] = str(value)
    return resp


if __name__ == "__main__":
    app.run(debug=True) 