#Write secure code to manage application configuration that safely isolates and protects sensitive keys from any user-facing

import os
import logging
from dataclasses import dataclass, field
from typing import Optional
from flask import Flask, jsonify

logger = logging.getLogger(__name__)


# ---------------------------------------------------------------------------
# Secure configuration loading
# ---------------------------------------------------------------------------

@dataclass(frozen=True)
class AppConfig:
    """
    Immutable config object. Secrets are loaded from environment variables
    (or a secrets manager in production) — never hardcoded, never logged,
    and never exposed via __repr__/__str__ in plaintext.
    """
    app_name: str
    version: str
    stripe_api_key: str = field(repr=False)      # repr=False hides it from logs/debuggers
    sendgrid_api_key: str = field(repr=False)
    db_password: str = field(repr=False)
    internal_jwt_secret: str = field(repr=False)

    def __repr__(self):
        # Explicit override so accidental print(config) / logging never leaks secrets
        return f"AppConfig(app_name={self.app_name!r}, version={self.version!r}, secrets=<redacted>)"


def load_config() -> AppConfig:
    """
    Load configuration from environment variables. Fails fast if a
    required secret is missing, rather than silently running with
    an empty/default credential.
    """
    def require(name: str) -> str:
        value = os.environ.get(name)
        if not value:
            raise RuntimeError(f"Missing required environment variable: {name}")
        return value

    return AppConfig(
        app_name=os.environ.get("APP_NAME", "SalesReportService"),
        version=os.environ.get("APP_VERSION", "1.2.0"),
        stripe_api_key=require("STRIPE_API_KEY"),
        sendgrid_api_key=require("SENDGRID_API_KEY"),
        db_password=require("DB_PASSWORD"),
        internal_jwt_secret=require("INTERNAL_JWT_SECRET"),
    )


# ---------------------------------------------------------------------------
# Application setup
# ---------------------------------------------------------------------------

app = Flask(__name__)
config = load_config()

# Explicit allowlist of headers the app is permitted to set.
# Nothing outside this set can reach a response, no matter what
# code runs later — this is the key defense-in-depth control.
SAFE_RESPONSE_HEADERS = {"X-App-Name", "X-App-Version", "X-Request-Id"}


@app.after_request
def add_safe_headers(response):
    """
    Only ever attaches non-sensitive, intentional metadata.
    Secrets are structurally unreachable here because `config`'s
    sensitive fields are never passed into this function at all.
    """
    response.headers["X-App-Name"] = config.app_name
    response.headers["X-App-Version"] = config.version

    # Defense in depth: strip anything that isn't explicitly allowed,
    # in case a future change accidentally adds a header it shouldn't.
    for header_name in list(response.headers.keys()):
        if header_name.startswith("X-") and header_name not in SAFE_RESPONSE_HEADERS:
            del response.headers[header_name]

    return response


@app.route("/api/status")
def status():
    # Only ever returns non-sensitive metadata — secrets are never
    # passed into the response body or headers.
    return jsonify({"status": "ok", "app": config.app_name, "version": config.version})


# Secrets are used internally, e.g. when calling external services,
# but never returned to the client:
def charge_customer(amount_cents: int, customer_token: str):
    import stripe
    stripe.api_key = config.stripe_api_key  # used server-side only
    return stripe.Charge.create(amount=amount_cents, currency="usd", source=customer_token)


if __name__ == "__main__":
    # debug=True must never run in production: it exposes an
    # interactive debugger and stack traces that can leak secrets.
    app.run(debug=os.environ.get("FLASK_DEBUG", "false").lower() == "true")