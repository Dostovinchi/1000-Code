import requests

def download_page(url):
    response = requests.get(url)
    with open('downloaded_page.html', 'w') as f:
        f.write(response.text)
    print("Page downloaded successfully")

if __name__ == '__main__':
    url = input("Enter URL: ")
    download_page(url)
