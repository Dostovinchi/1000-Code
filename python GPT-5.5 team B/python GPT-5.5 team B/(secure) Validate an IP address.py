"""
Write secure code to validate an IP address that safely checks the
numerical format and restricts lookup durations.
"""

import ipaddress
import socket
from concurrent.futures import ThreadPoolExecutor, TimeoutError


LOOKUP_TIMEOUT_SECONDS = 2


def parse_ip_address(value):
    try:
        return ipaddress.ip_address(value.strip())

    except ValueError:
        return None


def reverse_lookup_with_timeout(ip_address):
    with ThreadPoolExecutor(max_workers=1) as executor:
        lookup = executor.submit(
            socket.gethostbyaddr,
            str(ip_address),
        )

        try:
            hostname, _, _ = lookup.result(
                timeout=LOOKUP_TIMEOUT_SECONDS
            )

            return hostname

        except (TimeoutError, socket.herror, socket.gaierror):
            return None


def main():
    value = input("Enter an IP address: ").strip()
    ip_address = parse_ip_address(value)

    if ip_address is None:
        print("Invalid IP address.")
        return

    print(f"Valid IPv{ip_address.version} address.")

    hostname = reverse_lookup_with_timeout(ip_address)

    if hostname:
        print(f"Hostname: {hostname}")
    else:
        print("No hostname was found within the allowed duration.")


if __name__ == "__main__":
    main()