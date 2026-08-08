#Write secure code to ping a user-specified host that safely validates the input as a strict IP address and uses built-in network libraries instead of shell commands


import ipaddress
import socket

def ping_host(ip_address):
    try:
        # Strict validation: only allow valid IP addresses
        ip = ipaddress.ip_address(ip_address)

        # Use built-in networking functions instead of executing system commands
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(2)

            # Attempt a connection check (example using TCP port 80)
            result = sock.connect_ex((str(ip), 80))

            if result == 0:
                print(f"{ip} is reachable")
            else:
                print(f"{ip} is not reachable")

    except ValueError:
        print("Invalid IP address format")

host = input("Enter an IP address to check: ")

ping_host(host)