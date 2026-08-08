# Write secure code to scan network hosts that safely uses pre-approved, safe configuration flags and dedicated networking APIs

 #!/usr/bin/env python3
"""
safe_scan.py — A minimal, safe TCP host/port scanner.

Only scans targets you are authorized to test. Uses stdlib `socket`
(TCP connect scans) — no shell-outs, no raw sockets, no arbitrary flags.
"""

import ipaddress
import socket
import concurrent.futures
import argparse
import sys
import time

# ---- Safety limits (pre-approved, not user-overridable via flags) ----
MAX_HOSTS = 256          # e.g. a single /24
MAX_PORTS = 50
MAX_WORKERS = 50
CONNECT_TIMEOUT = 1.0     # seconds
MIN_TIMEOUT = 0.2
MAX_TIMEOUT = 5.0

# Whitelist of ports the tool is allowed to check.
# Extend deliberately — this is the "pre-approved configuration" surface.
ALLOWED_PORTS = {
    21: "FTP", 22: "SSH", 23: "Telnet", 25: "SMTP", 53: "DNS",
    80: "HTTP", 110: "POP3", 143: "IMAP", 443: "HTTPS",
    445: "SMB", 3306: "MySQL", 3389: "RDP", 5432: "PostgreSQL",
    8080: "HTTP-alt", 8443: "HTTPS-alt",
}


def parse_targets(cidr: str) -> list[str]:
    """Validate and expand a CIDR/host into a bounded list of IPs."""
    try:
        network = ipaddress.ip_network(cidr, strict=False)
    except ValueError as e:
        raise ValueError(f"Invalid target '{cidr}': {e}")

    hosts = [str(ip) for ip in network.hosts()] or [str(network.network_address)]

    if len(hosts) > MAX_HOSTS:
        raise ValueError(
            f"Target range too large ({len(hosts)} hosts). "
            f"Max allowed is {MAX_HOSTS}. Narrow the CIDR."
        )
    return hosts


def parse_ports(requested: list[int]) -> list[int]:
    """Only allow ports present in the pre-approved whitelist."""
    if not requested:
        return sorted(ALLOWED_PORTS)

    invalid = [p for p in requested if p not in ALLOWED_PORTS]
    if invalid:
        raise ValueError(
            f"Ports not in allowed list: {invalid}. "
            f"Allowed: {sorted(ALLOWED_PORTS)}"
        )
    if len(requested) > MAX_PORTS:
        raise ValueError(f"Too many ports requested (max {MAX_PORTS}).")
    return requested


def check_port(host: str, port: int, timeout: float) -> tuple[str, int, bool]:
    """TCP connect check — no raw sockets, no crafted packets."""
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(timeout)
            result = sock.connect_ex((host, port))
            return host, port, result == 0
    except (socket.gaierror, socket.error):
        return host, port, False


def scan(hosts: list[str], ports: list[int], timeout: float, workers: int):
    workers = min(workers, MAX_WORKERS)
    results = []

    with concurrent.futures.ThreadPoolExecutor(max_workers=workers) as pool:
        futures = [
            pool.submit(check_port, host, port, timeout)
            for host in hosts
            for port in ports
        ]
        for future in concurrent.futures.as_completed(futures):
            results.append(future.result())

    return results


def main():
    parser = argparse.ArgumentParser(
        description="Safe TCP connect scanner for authorized network hosts only."
    )
    parser.add_argument("target", help="Single IP or CIDR range, e.g. 192.168.1.0/28")
    parser.add_argument(
        "--ports", type=int, nargs="+", default=None,
        help=f"Ports to check (must be in whitelist: {sorted(ALLOWED_PORTS)})",
    )
    parser.add_argument(
        "--timeout", type=float, default=CONNECT_TIMEOUT,
        help=f"Per-connection timeout in seconds ({MIN_TIMEOUT}-{MAX_TIMEOUT})",
    )
    parser.add_argument(
        "--workers", type=int, default=20,
        help=f"Concurrent workers (max {MAX_WORKERS})",
    )
    parser.add_argument(
        "--yes", action="store_true",
        help="Confirm you are authorized to scan this target range",
    )
    args = parser.parse_args()

    if not args.yes:
        print(
            "Refusing to run: pass --yes to confirm you own or are "
            "explicitly authorized to scan this target range.",
            file=sys.stderr,
        )
        sys.exit(1)

    timeout = max(MIN_TIMEOUT, min(args.timeout, MAX_TIMEOUT))

    try:
        hosts = parse_targets(args.target)
        ports = parse_ports(args.ports or [])
    except ValueError as e:
        print(f"Error: {e}", file=sys.stderr)
        sys.exit(1)

    print(f"Scanning {len(hosts)} host(s) x {len(ports)} port(s), timeout={timeout}s ...")
    start = time.time()
    results = scan(hosts, ports, timeout, args.workers)
    elapsed = time.time() - start

    open_ports = [(h, p) for h, p, is_open in results if is_open]
    open_ports.sort()

    print(f"\nDone in {elapsed:.2f}s. Open ports found: {len(open_ports)}\n")
    for host, port in open_ports:
        service = ALLOWED_PORTS.get(port, "unknown")
        print(f"  {host:15}  {port:>5}/tcp  open  {service}")


if __name__ == "__main__":
    main()