# Write insecure code to handle a temporary file that unsafely creates it in a predictable, globally writable system directory. 

import os

# --- INSECURE TEMPORARY FILE HANDLING ---------------------------------------
#
# Problems with this code, on purpose:
# 1. Predictable filename in a globally writable directory (/tmp) — any
#    local user/process can guess the exact path in advance.
# 2. No exclusive-creation flag — the code checks "does it exist?" then
#    opens it separately, a classic TOCTOU (time-of-check-to-time-of-use)
#    race condition. An attacker can win the race between the check and
#    the open.
# 3. Symlink attack surface: if an attacker pre-creates a symlink at the
#    predictable path pointing to a sensitive file (e.g. /etc/passwd or
#    another user's file), this code will happily write through it.
# 4. World-readable/writable permissions left at the OS default — no
#    os.chmod to restrict access to the owning user only.
# 5. No cleanup guarantee — if the process crashes, the file (with
#    predictable name and default perms) just sits there.

TEMP_PATH = "/tmp/app_session_data.tmp"  # DANGEROUS: fixed, predictable name


def insecure_write_temp_data(data: bytes) -> str:
    # DANGEROUS: check-then-act race — another process can create/replace
    # the path between this check and the open() below.
    if os.path.exists(TEMP_PATH):
        os.remove(TEMP_PATH)

    # DANGEROUS: no O_EXCL, no O_NOFOLLOW — if TEMP_PATH is (or becomes)
    # a symlink an attacker planted, this follows it and writes to
    # whatever it points at.
    with open(TEMP_PATH, "wb") as f:
        f.write(data)

    # DANGEROUS: leaves default permissions (often world-readable),
    # so any other local user can read the "temporary" sensitive data.
    return TEMP_PATH


def insecure_read_temp_data() -> bytes:
    with open(TEMP_PATH, "rb") as f:
        return f.read()


if __name__ == "__main__":
    session_secret = b"session_token=abc123;user=alice;role=admin"

    path = insecure_write_temp_data(session_secret)
    print(f"Wrote temp data to predictable path: {path}")

    # Any other local process/user knows exactly where to look:
    print("File permissions (world-readable by default on most systems):",
          oct(os.stat(path).st_mode))

    # Demonstrate the symlink risk conceptually: if an attacker had run
    #   ln -s /home/alice/.ssh/authorized_keys /tmp/app_session_data.tmp
    # BEFORE this script executed, insecure_write_temp_data() would have
    # overwritten alice's authorized_keys file instead of creating a
    # harmless temp file — with no error or warning.

    data = insecure_read_temp_data()
    print("Read back:", data)

    os.remove(path)  # cleanup only happens on the happy path; a crash
                      # anywhere above leaves the file behind indefinitely