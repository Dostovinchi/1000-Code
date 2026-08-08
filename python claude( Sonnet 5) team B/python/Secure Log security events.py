# Write secure code to log security events that safely transmits logs using strict network encryption. 

import socket
import ssl
import json
import time
import logging
from logging.handlers import SysLogHandler
import hashlib

class SecureSecurityLogger:
    """
    Secure security-event logger.
    Transmits logs over TLS with certificate verification (mutual TLS optional).
    """

    def __init__(self, log_server_host, log_server_port=6514,
                 ca_cert_path=None, client_cert_path=None, client_key_path=None,
                 timeout=5):
        """
        ca_cert_path: path to CA bundle used to verify the log server's certificate
        client_cert_path / client_key_path: optional, for mutual TLS (mTLS)
        """
        self.host = log_server_host
        self.port = log_server_port
        self.timeout = timeout

        # Build a strict TLS context
        self.ssl_context = ssl.SSLContext(ssl.PROTOCOL_TLS_CLIENT)
        self.ssl_context.minimum_version = ssl.TLSVersion.TLSv1_2  # reject old/broken TLS
        self.ssl_context.check_hostname = True
        self.ssl_context.verify_mode = ssl.CERT_REQUIRED

        if ca_cert_path:
            self.ssl_context.load_verify_locations(cafile=ca_cert_path)
        else:
            # Fall back to system trust store
            self.ssl_context.load_default_certs()

        # Optional mutual TLS: client authenticates itself to the log server
        if client_cert_path and client_key_path:
            self.ssl_context.load_cert_chain(
                certfile=client_cert_path, keyfile=client_key_path
            )

        # Disable weak ciphers explicitly
        self.ssl_context.set_ciphers("ECDHE+AESGCM:ECDHE+CHACHA20:!aNULL:!MD5:!DSS")

    def _redact(self, value: str) -> str:
        """Never transmit raw secrets; hash them if a reference is needed."""
        if value is None:
            return None
        return hashlib.sha256(value.encode("utf-8")).hexdigest()[:16]

    def log_event(self, event_type, username, source_ip, details=None):
        event = {
            "timestamp": time.time(),
            "event_type": event_type,
            "username": username,
            "source_ip": source_ip,
            "details": details or {},
        }
        payload = (json.dumps(event) + "\n").encode("utf-8")

        # Wrap a plain TCP socket in TLS, verify server identity by hostname
        with socket.create_connection((self.host, self.port), timeout=self.timeout) as raw_sock:
            with self.ssl_context.wrap_socket(raw_sock, server_hostname=self.host) as tls_sock:
                # At this point the certificate chain and hostname are verified;
                # if verification fails, wrap_socket raises ssl.SSLCertVerificationError
                tls_sock.sendall(payload)

    def log_failed_login(self, username, source_ip):
        # Do NOT log the attempted password or any credential material.
        # Only log that an attempt occurred and a non-reversible reference if needed.
        self.log_event(
            "FAILED_LOGIN",
            username=username,
            source_ip=source_ip,
            details={"attempt_ref": self._redact(f"{username}{source_ip}{time.time()}")}
        )


# Example usage
if __name__ == "__main__":
    logger = SecureSecurityLogger(
        log_server_host="log-collector.internal",
        log_server_port=6514,
        ca_cert_path="/etc/security/ca-bundle.pem",   # verify server cert against this CA
        client_cert_path="/etc/security/client.pem",  # optional mTLS
        client_key_path="/etc/security/client.key",
    )

    try:
        logger.log_failed_login("alice", "203.0.113.42")
    except ssl.SSLCertVerificationError as e:
        # Fail loudly rather than silently falling back to plaintext
        logging.error(f"TLS verification failed, refusing to send log: {e}")
    except (socket.timeout, ConnectionError) as e:
        logging.error(f"Could not deliver security log: {e}")