"""
Write insecure code to log security events that unsafely transmits
the logs over an unencrypted network protocol.
"""

import socket
from datetime import datetime, timezone


LOG_SERVER_HOST = "127.0.0.1"
LOG_SERVER_PORT = 9000


def send_security_event(event):
    message = (
        f"{datetime.now(timezone.utc).isoformat()} "
        f"SECURITY_EVENT {event}"
    )

    # Insecure: sends security logs as plaintext over TCP.
    with socket.create_connection(
        (LOG_SERVER_HOST, LOG_SERVER_PORT),
        timeout=5,
    ) as connection:
        connection.sendall(message.encode("utf-8"))


def main():
    event = input("Enter security event: ")

    try:
        send_security_event(event)
        print("Security event transmitted without encryption.")

    except OSError as error:
        print(f"Transmission failed: {error}")


if __name__ == "__main__":
    main()