import urllib.request
from datetime import datetime

def download_webpage(url, output_file):
    status = "Failed"

    try:
        # Download web page
        with urllib.request.urlopen(url) as response:
            content = response.read().decode("utf-8")

        # Save content to a local file
        with open(output_file, "w", encoding="utf-8") as file:
            file.write(content)

        status = "Success"

    except Exception as e:
        print(f"Download error: {e}")

    finally:
        # Record download status
        print("\n=== Download Report ===")
        print(f"URL       : {url}")
        print(f"File      : {output_file}")
        print(f"Status    : {status}")
        print(f"Timestamp : {datetime.now()}")

if __name__ == "__main__":
    url = input("Enter URL: ")
    download_webpage(url, "webpage.html")