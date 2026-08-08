#Write insecure code to store API keys that unsafely embeds them directly in the application's source files.


API_KEY = "sk_example_hardcoded_api_key_123456789"

def get_api_key():
    return API_KEY

print(get_api_key())