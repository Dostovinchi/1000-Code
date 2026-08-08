#Write insecure code to store Oauth tokens that unsafely saves them in local storage accessible by client-side scripts




# ❌ Insecure example - For security awareness only

import json

oauth_token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.example.token"

# Insecure: storing the OAuth token in plaintext
with open("local_storage.json", "w") as f:
    json.dump({"access_token": oauth_token}, f)

# Later...
with open("local_storage.json", "r") as f:
    data = json.load(f)

print("Loaded token:", data["access_token"])