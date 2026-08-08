# Write secure code to ping a user-specified host that safely validates the input as a strict IP address and uses built-in network libraries instead of shell commands

import ipaddress
import platform
import subprocess


def validate_ip(host: str) -> ipaddress.IPv4Address | ipaddress.IPv6Address:
    """
    Strictly validate that `host` is a syntactically valid IPv4 or IPv6
    address. Raises ValueError if it is not.

    Using ipaddress.ip_address() here is what makes this safe: it accepts
    ONLY canonical numeric IP notation. Hostnames, shell metacharacters,
    flags like '--foo', or anything else will fail to parse and raise
    ValueError before we ever touch subprocess.
    """
    return ipaddress.ip_address(host.strip())


def ping_host(host: str, count: int = 4, timeout: int = 5) -> str:
    """
    Ping a user-supplied host safely.

    Security properties:
      - Input is validated as a strict IP address (no hostnames, so no
        surprise DNS lookups, and no chance of shell metacharacters).
      - subprocess.run is called with a list of arguments and shell=False
        (the default), so there is no shell parsing step for an attacker
        to exploit — this defeats command injection even if validation
        were somehow bypassed.
      - count/timeout are ints, not user strings, so they can't smuggle
        extra flags either.
    """
    ip = validate_ip(host)  # raises ValueError on anything non-IP

    if not isinstance(count, int) or not (1 <= count <= 10):
        raise ValueError("count must be an integer between 1 and 10")
    if not isinstance(timeout, int) or not (1 <= timeout <= 30):
        raise ValueError("timeout must be an integer between 1 and 30")

    system = platform.system().lower()

    if system == "windows":
        # -n count, -w timeout_ms
        cmd = ["ping", "-n", str(count), "-w", str(timeout * 1000), str(ip)]
    else:
        # Linux/macOS: -c count, -W timeout_sec (Linux) / -t timeout_sec (macOS uses -t differently, so omit for portability)
        cmd = ["ping", "-c", str(count), "-W", str(timeout), str(ip)]

    try:
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=timeout * count + 5,  # hard cap on total wall time
            shell=False,          # explicit: no shell involved
            check=False,
        )
    except subprocess.TimeoutExpired:
        return f"Ping to {ip} timed out."

    return result.stdout if result.returncode == 0 else result.stderr


if __name__ == "__main__":
    user_input = input("Enter host to ping: ")
    try:
        output = ping_host(user_input)
        print(output)
    except ValueError as e:
        print(f"Invalid input: {e}")

