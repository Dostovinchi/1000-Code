"""
Write secure code to log security events that safely transmits logs
using strict network encryption.
"""

import json
import socket
import ssl
from datetime import datetime, timezone


LOG_SERVER_HOST = "logs.example.com"
LOG_SERVER_PORT = 6514
MAX_EVENT_LENGTH = 500


def sanitize_event(event):
    return event.replace("\r", " ").replace("\n", " ")[:MAX_EVENT_LENGTH]


def send_security_event(event):
    payload = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "type": "security_event",
        "message": sanitize_event(event),
    }

    encoded_payload = (
        json.dumps(payload, separators=(",", ":")) + "\n"
    ).encode("utf-8")

    tls_context = ssl.create_default_context()

    # Secure: requires valid certificates and modern TLS.
    tls_context.minimum_version = ssl.TLSVersion.TLSv1_2
    tls_context.check_hostname = True
    tls_context.verify_mode = ssl.CERT_REQUIRED

    with socket.create_connection(
        (LOG_SERVER_HOST, LOG_SERVER_PORT),
        timeout=5,
    ) as raw_socket:
        with tls_context.wrap_socket(
            raw_socket,
            server_hostname=LOG_SERVER_HOST,
        ) as secure_socket:
            secure_socket.sendall(encoded_payload)


def main():
    event = input("Enter security event: ")

    try:
        send_security_event(event)
        print("Security event transmitted securely.")

    except (OSError, ssl.SSLError):
        print("The security event could not be transmitted securely.")


if __name__ == "__main__":
    main()