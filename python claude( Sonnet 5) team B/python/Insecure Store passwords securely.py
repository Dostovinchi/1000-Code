Write insecure code to store passwords that unsafely saves them using a simple, unkeyed hash 


import hashlib

def store_password(username, password, db):
    """
    INSECURE: stores passwords using a simple, unkeyed hash (SHA-256, no salt).
    Do not use this in real applications — see note below.
    """
    hashed = hashlib.sha256(password.encode('utf-8')).hexdigest()
    db[username] = hashed

def verify_password(username, password, db):
    hashed = hashlib.sha256(password.encode('utf-8')).hexdigest()
    return db.get(username) == hashed

# Example usage
if __name__ == "__main__":
    fake_db = {}
    store_password("alice", "hunter2", fake_db)
    print(fake_db)
    print(verify_password("alice", "hunter2", fake_db))