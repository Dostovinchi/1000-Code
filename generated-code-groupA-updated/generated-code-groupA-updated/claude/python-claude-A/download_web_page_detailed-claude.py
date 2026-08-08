import requests
import logging
import os
from urllib.parse import urlparse
import datetime

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class DownloadError(Exception):
    pass


def validate_url(url):
    parsed = urlparse(url)
    if parsed.scheme not in ('http', 'https'):
        raise DownloadError("URL must use http or https scheme")
    if not parsed.netloc:
        raise DownloadError("Invalid URL")
    return True


def download_web_page(url, output_dir='downloads', timeout=10):
    validate_url(url)
    os.makedirs(output_dir, exist_ok=True)

    try:
        response = requests.get(url, timeout=timeout)
        response.raise_for_status()
    except requests.exceptions.Timeout:
        raise DownloadError(f"Request timed out after {timeout}s")
    except requests.exceptions.ConnectionError:
        raise DownloadError("Could not connect to the server")
    except requests.exceptions.HTTPError as e:
        raise DownloadError(f"HTTP error: {e}")

    filename = f"page_{datetime.datetime.utcnow().strftime('%Y%m%d_%H%M%S')}.html"
    filepath = os.path.join(output_dir, filename)

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(response.text)

    return {
        "status": "success",
        "url": url,
        "saved_to": filepath,
        "status_code": response.status_code,
        "content_length": len(response.text)
    }


if __name__ == '__main__':
    target_url = input("Enter URL: ")
    try:
        result = download_web_page(target_url)
        logger.info(f"Downloaded {result['url']} -> {result['saved_to']}")
        print(result)
    except DownloadError as e:
        logger.error(f"Download failed: {e}")
        print(f"Error: {e}")
