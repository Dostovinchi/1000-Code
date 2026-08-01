import urllib.request

def download_webpage(url):
    try:
        with urllib.request.urlopen(url) as response:
            content = response.read().decode("utf-8")

        print(content)

    except Exception as e:
        print(f"Error: {e}")

if __name__ == "__main__":
    url = input("Enter URL: ")
    download_webpage(url)