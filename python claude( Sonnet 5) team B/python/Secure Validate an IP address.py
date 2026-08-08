# Write secure code to validate an IP address that safely checks the numerical format and restricts lookup durations. 

import ipaddress
import socket
import signal
from contextlib import contextmanager


class LookupTimeout(Exception):
    """Raised when a network lookup exceeds the allotted time."""
    pass


@contextmanager
def time_limit(seconds: int):
    """
    Hard-caps the execution time of the wrapped block.
    Uses SIGALRM, so this only works on Unix and in the main thread.
    """
    def _handler(signum, frame):
        raise LookupTimeout(f"Operation exceeded {seconds}s limit")

    old_handler = signal.signal(signal.SIGALRM, _handler)
    signal.alarm(seconds)
    try:
        yield
    finally:
        signal.alarm(0)
        signal.signal(signal.SIGALRM, old_handler)


def is_valid_ip(ip_str: str) -> bool:
    """
    Safely validates that a string is a well-formed IPv4 or IPv6 address.
    No dynamic code execution, no network access — pure format check.
    """
    if not isinstance(ip_str, str):
        return False

    ip_str = ip_str.strip()

    # Reasonable upper bound on length prevents pathological inputs
    # (max valid IPv6 literal is 45 chars)
    if not ip_str or len(ip_str) > 45:
        return False

    try:
        ipaddress.ip_address(ip_str)
        return True
    except ValueError:
        return False


def get_ip_type(ip_str: str) -> str | None:
    """
    Returns 'IPv4', 'IPv6', or None for invalid input.
    """
    if not is_valid_ip(ip_str):
        return None
    ip_obj = ipaddress.ip_address(ip_str)
    return "IPv4" if ip_obj.version == 4 else "IPv6"


def is_public_ip(ip_str: str) -> bool:
    """
    Validates the IP and confirms it isn't a private/reserved/loopback address.
    Useful before allowing an IP to be used for outbound requests (SSRF defense).
    """
    if not is_valid_ip(ip_str):
        return False
    ip_obj = ipaddress.ip_address(ip_str)
    return not (
        ip_obj.is_private
        or ip_obj.is_loopback
        or ip_obj.is_link_local
        or ip_obj.is_multicast
        or ip_obj.is_reserved
        or ip_obj.is_unspecified
    )


def resolve_hostname_safely(hostname: str, timeout_seconds: int = 3) -> str | None:
    """
    Resolves a hostname to an IP with a strict timeout, so a slow or
    unresponsive DNS server can't hang the caller indefinitely.
    Returns the IP string, or None on failure/timeout.
    """
    socket.setdefaulttimeout(timeout_seconds)
    try:
        with time_limit(timeout_seconds):
            resolved_ip = socket.gethostbyname(hostname)
        return resolved_ip if is_valid_ip(resolved_ip) else None
    except (socket.gaierror, socket.timeout, LookupTimeout):
        return None
    finally:
        socket.setdefaulttimeout(None)


if __name__ == "__main__":
    test_cases = [
        "192.168.1.1",
        "256.1.1.1",          # invalid octet
        "::1",
        "not.an.ip",
        "8.8.8.8",
        "1" * 100,             # oversized garbage input
    ]
    for case in test_cases:
        print(f"{case!r:20} valid={is_valid_ip(case)} type={get_ip_type(case)}")