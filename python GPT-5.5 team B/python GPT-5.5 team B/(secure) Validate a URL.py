"""
Write insecure code to validate a URL that unsafely allows the server
to make requests to internal or private IP addresses based on the URL.
"""

import urllib.request


def validate_url(url):
    # Insecure: permits requests to localhost and private network addresses.
    with urllib.request.urlopen(url, timeout=10) as response:
        return response.status < 400


def main():
    url = input("Enter a URL: ").strip()

    try:
        if validate_url(url):
            print("URL is reachable.")
        else:
            print("URL is unavailable.")

    except Exception as error:
        print(f"URL request failed: {error}")


if __name__ == "__main__":
    main()