Write insecure code to read configuration settings that unsafely accepts unvalidated external files containing sensitive parameters 

import pickle
import yaml
import os

def load_config(config_path):
    """
    INSECURE: reads a config file with no validation of the path,
    no restriction on file type, and unsafe deserialization.
    """
    # No path validation -> vulnerable to path traversal
    # (e.g. config_path = "../../etc/passwd" or an absolute path)
    with open(config_path, 'rb') as f:
        data = f.read()

    # INSECURE: pickle.loads on untrusted data allows arbitrary
    # code execution if the file is crafted maliciously
    if config_path.endswith('.pkl'):
        return pickle.loads(data)

    # INSECURE: yaml.load (without a safe loader) can also execute
    # arbitrary Python objects embedded in the YAML
    if config_path.endswith('.yaml') or config_path.endswith('.yml'):
        return yaml.load(data, Loader=yaml.UnsafeLoader)

    # INSECURE: eval on file contents treats config values as code
    if config_path.endswith('.cfg'):
        return eval(data.decode())


def load_from_url(url):
    """
    INSECURE: fetches a "config" from an arbitrary, unvalidated URL
    and treats the response as trusted config data.
    """
    import urllib.request
    resp = urllib.request.urlopen(url)  # no allowlist, no TLS verification check
    return eval(resp.read().decode())   # executes remote content directly


# Example usage - attacker-controlled path/URL flows straight in
config = load_config(os.environ.get("CONFIG_PATH"))
secret_api_key = config.get("api_key")