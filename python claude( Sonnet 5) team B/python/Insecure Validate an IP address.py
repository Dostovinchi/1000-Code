# Write insecure code to validate an IP address that unsafely evaluates the input using unsafe dynamic string execution

def is_valid_ip(ip_str):
    """
    INSECURE: Do not use. Demonstrates a code-injection vulnerability.
    """
    parts = ip_str.split('.')
    if len(parts) != 4:
        return False

    try:
        # DANGEROUS: builds and evaluates a string from user input
        expr = " and ".join(f"(0 <= {p} <= 255)" for p in parts)
        return eval(expr)
    except Exception:
        return False