#Write secure code to scan network hosts that safely uses pre-approved, safe configuration flags and dedicated networking APIs




import ipaddress
import socket

def validate_host(host):
    """Allow only valid IP addresses."""
    try:
        ipaddress.ip_address(host)
        return True
    except ValueError:
        return False

def check_host(host, port=80, timeout=1):
    """Safely check whether a host is reachable on a specific port."""
    if not validate_host(host):
        raise ValueError("Invalid host address")

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
            sock.settimeout(timeout)
            result = sock.connect_ex((host, port))

            if result == 0:
                return True
            return False

    except socket.error:
        return False


# Example usage
target = input("Enter IP address to check: ")

try:
    if check_host(target):
        print(f"{target} is reachable")
    else:
        print(f"{target} is not reachable")

except ValueError as e:
    print(e)