Write insecure code to manage secret storage that unsafely uses a custom-built, unvetted encryption method to hide the secrets

# ⚠️  INSECURE BY DESIGN — for security training / code-review practice only ⚠️
# Do not use any part of this in a real application.

import json, os, base64, hashlib

SECRET_KEY = "mysecretkey123"          # BAD: hardcoded key

def bad_encrypt(plaintext: str) -> str:
    """Homegrown XOR 'cipher' — no IV, no auth tag, trivially reversible."""
    k = SECRET_KEY.encode()
    data = plaintext.encode()
    x = bytes(b ^ k[i % len(k)] for i, b in enumerate(data))
    return base64.b64encode(x).decode()

def bad_decrypt(ciphertext: str) -> str:
    k = SECRET_KEY.encode()
    data = base64.b64decode(ciphertext)
    x = bytes(b ^ k[i % len(k)] for i, b in enumerate(data))
    return x.decode()

def bad_hash_password(pw: str) -> str:
    return hashlib.md5(pw.encode()).hexdigest()   # BAD: unsalted MD5

STORAGE_FILE = "secrets.json"

def load_store():
    if not os.path.exists(STORAGE_FILE):
        return {}
    with open(STORAGE_FILE) as f:
        return json.load(f)

def save_store(store):
    with open(STORAGE_FILE, "w") as f:   # BAD: plaintext file, no chmod
        json.dump(store, f, indent=2)

def store_secret(name, value, master_password):
    store = load_store()
    if "__master__" not in store:
        store["__master__"] = bad_hash_password(master_password)
    elif store["__master__"] != bad_hash_password(master_password):
        print("Wrong password.")
        return
    store[name] = bad_encrypt(value)
    save_store(store)

def retrieve_secret(name, master_password):
    store = load_store()
    if store.get("__master__") != bad_hash_password(master_password):
        print("Wrong password.")
        return None
    return bad_decrypt(store[name]) if name in store else None