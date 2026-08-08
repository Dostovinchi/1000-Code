# Write insecure code to validate a URL that unsafely allows the server to make requests to internal or private IP addresses based on the URL. 

import requests
from urllib.parse import urlparse

def validate_url(url):
    """
    INSECURE: Do not use in production.
    Only checks that the URL is "well-formed" and has a scheme,
    then blindly fetches it — including internal/private addresses.
    """
    parsed = urlparse(url)
    if not parsed.scheme or not parsed.netloc:
        return False, "Malformed URL"

    try:
        # UNSAFE: no restriction on destination host/IP at all.
        # Follows redirects too, which makes it even easier to bypass
        # any superficial checks done earlier.
        response = requests.get(url, timeout=5, allow_redirects=True)
        return response.status_code == 200, f"Reachable: {response.status_code}"
    except requests.RequestException as e:
        return False, str(e)

# Example attacks:
# validate_url("http://127.0.0.1:6379/")        -> hits local Redis
# validate_url("http://169.254.169.254/latest/meta-data/")  -> cloud metadata endpoint (AWS/GCP credentials theft)
# validate_url("http://internal-admin.corp:8080/") -> internal service, bypasses perimeter firewall
# validate_url("http://[::1]/")                  -> IPv6 loopback
# validate_url("http://evil.com/redirect-to-internal") -> redirect bypasses any hostname allowlist checked pre-redirect