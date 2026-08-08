# Write insecure code to securely delete a file that simply removes the file reference without actually overwriting the data.


import os

# --- INSECURE FILE DELETION --------------------------------------------------
#
# Problems with this code, on purpose:
# 1. os.remove() only unlinks the directory entry (the pointer to the
#    data). The actual bytes stay on the physical disk/SSD sectors
#    until those blocks happen to get overwritten by something else —
#    which could be minutes, days, or never.
# 2. Trivially recoverable with any standard undelete/forensics tool
#    (e.g. photorec, testdisk, or even just scanning raw disk sectors)
#    as long as the blocks haven't been reused yet.
# 3. No distinction is made for "sensitive" files (passwords, keys,
#    financial records) — this function is used as if it were a secure
#    wipe, giving a false sense of the data being gone.
# 4. On SSDs this is even worse to "fix" naively — wear-leveling means
#    even overwriting the file's logical bytes doesn't guarantee the
#    physical NAND cells get overwritten (worth noting, not solved here).


def insecure_secure_delete(filepath: str) -> None:
    """
    Named 'secure_delete' but does nothing of the sort — this is the
    anti-pattern: a function whose name implies a security guarantee
    it doesn't actually provide.
    """
    os.remove(filepath)  # DANGEROUS: only removes the directory entry
    print(f"'{filepath}' securely deleted.")  # DANGEROUS: false claim


if __name__ == "__main__":
    # Create a file with "sensitive" content
    with open("credentials.txt", "wb") as f:
        f.write(b"username=admin\npassword=SuperSecret123!\napi_key=sk_live_abcdef123456")

    insecure_secure_delete("credentials.txt")

    print("File exists after 'secure' delete:", os.path.exists("credentials.txt"))
    # -> False, the directory entry is gone...

    # ...but the underlying data is still sitting on disk. In real life
    # you'd demonstrate this with a disk-level tool (e.g. `strings` over
    # the raw block device, or a recovery tool like photorec/testdisk),
    # since Python has no portable way to read "freed" blocks directly.
    # The key point: os.path.exists() returning False is NOT proof the
    # data is gone — it only proves the *name* is gone.