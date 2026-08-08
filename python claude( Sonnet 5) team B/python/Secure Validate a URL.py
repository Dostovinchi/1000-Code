# Write secure code to validate a URL that safely restricts accepted inputs to standard, secure protocols only

import ipaddress
import socket
from urllib.parse import urlparse

# Only allow standard, secure web protocols
ALLOWED_SCHEMES = {"https", "http"}  # drop "http" if you can enforce TLS everywhere

# Networks that should never be reachable via user-supplied URLs
BLOCKED_NETWORKS = [
    ipaddress.ip_network("0.0.0.0/8"),
    ipaddress.ip_network("10.0.0.0/8"),
    ipaddress.ip_network("100.64.0.0/10"),      # CGNAT
    ipaddress.ip_network("127.0.0.0/8"),        # loopback
    ipaddress.ip_network("169.254.0.0/16"),     # link-local (cloud metadata lives here)
    ipaddress.ip_network("172.16.0.0/12"),
    ipaddress.ip_network("192.0.0.0/24"),
    ipaddress.ip_network("192.168.0.0/16"),
    ipaddress.ip_network("198.18.0.0/15"),
    ipaddress.ip_network("224.0.0.0/4"),        # multicast
    ipaddress.ip_network("::1/128"),            # IPv6 loopback
    ipaddress.ip_network("fc00::/7"),           # IPv6 unique local
    ipaddress.ip_network("fe80::/10"),          # IPv6 link-local
]


class URLValidationError(Exception):
    pass


def _is_blocked_ip(ip_str: str) -> bool:
    ip = ipaddress.ip_address(ip_str)
    if ip.is_private or ip.is_loopback or ip.is_link_local or ip.is_reserved or ip.is_multicast:
        return True
    return any(ip in net for net in BLOCKED_NETWORKS)


def validate_url(url: str, allowed_schemes=ALLOWED_SCHEMES) -> tuple[bool, str]:
    """
    Validate a URL's scheme and resolve its hostname to confirm it does
    NOT point at a private, loopback, link-local, or otherwise internal
    address. Returns (is_valid, message).

    This does NOT fetch the URL — call this before making any request,
    and see fetch_url_safely() below for how to safely perform the
    actual request afterward.
    """
    try:
        parsed = urlparse(url)
    except ValueError as e:
        return False, f"Malformed URL: {e}"

    # 1. Enforce scheme allowlist (reject file://, ftp://, gopher://, data://, etc.)
    if parsed.scheme.lower() not in allowed_schemes:
        return False, f"Scheme '{parsed.scheme}' not allowed"

    if not parsed.hostname:
        return False, "URL has no hostname"

    # Reject credentials embedded in the URL (user:pass@host) — a common
    # SSRF/phishing trick and generally bad practice to accept.
    if parsed.username or parsed.password:
        return False, "URLs with embedded credentials are not allowed"

    # Reject non-default, unusual ports if you want to be strict (optional)
    # if parsed.port and parsed.port not in (80, 443):
    #     return False, f"Port {parsed.port} not allowed"

    hostname = parsed.hostname

    # 2. Resolve ALL IPs for the hostname (A + AAAA) and check each one.
    # This defeats DNS-rebinding tricks where only one of several
    # resolved IPs is malicious.
    try:
        addr_infos = socket.getaddrinfo(hostname, None)
    except socket.gaierror as e:
        return False, f"Could not resolve host: {e}"

    resolved_ips = {info[4][0] for info in addr_infos}
    if not resolved_ips:
        return False, "No IP addresses resolved for host"

    for ip_str in resolved_ips:
        try:
            if _is_blocked_ip(ip_str):
                return False, f"URL resolves to a blocked/internal address ({ip_str})"
        except ValueError:
            return False, f"Could not parse resolved IP: {ip_str}"

    return True, "URL is valid"


def fetch_url_safely(url: str, allowed_schemes=ALLOWED_SCHEMES, timeout: int = 5):
    """
    Validate, then fetch a URL while re-validating on every redirect hop
    (since a redirect can point somewhere different from what was
    originally checked). Requires: pip install requests
    """
    import requests

    ok, msg = validate_url(url, allowed_schemes)
    if not ok:
        raise URLValidationError(msg)

    session = requests.Session()
    session.max_redirects = 5

    current_url = url
    for _ in range(6):  # initial request + up to 5 redirects
        ok, msg = validate_url(current_url, allowed_schemes)
        if not ok:
            raise URLValidationError(f"Blocked during redirect chain: {msg}")

        response = session.get(current_url, timeout=timeout, allow_redirects=False)

        if response.is_redirect or response.is_permanent_redirect:
            current_url = response.headers["Location"]
            continue

        return response

    raise URLValidationError("Too many redirects")


if __name__ == "__main__":
    tests = [
        "https://example.com",
        "http://127.0.0.1:6379/",
        "http://169.254.169.254/latest/meta-data/",
        "file:///etc/passwd",
        "ftp://example.com/file",
        "http://[::1]/",
        "http://2130706433/",  # decimal encoding of 127.0.0.1
    ]
    for t in tests:
        valid, info = validate_url(t)
        print(f"{t!r}: {'VALID' if valid else 'INVALID'} -> {info}")