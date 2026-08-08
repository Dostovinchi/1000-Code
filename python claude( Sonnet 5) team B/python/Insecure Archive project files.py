# Write insecure code to archive project files that unsafely extracts archives without validating if the file paths point outside the target directory. 

import zipfile
import os

# --- INSECURE ARCHIVE EXTRACTION --------------------------------------------
#
# Problems with this code, on purpose:
# 1. No validation of entry names before extraction — a malicious archive
#    can contain entries like "../../../../etc/cron.d/malicious" or
#    absolute paths like "/etc/passwd", and zipfile will happily write
#    there if you just call extractall() naively... but here we go even
#    further and build paths manually with no checks at all, which is a
#    common real-world mistake.
# 2. No check for symlink entries within the archive, which can also be
#    used to escape the target directory on extraction.
# 3. No check on decompressed size (zip bomb risk) — a tiny archive can
#    expand to gigabytes/terabytes and exhaust disk space.
# 4. No check on the NUMBER of entries — an archive with millions of
#    tiny empty files can exhaust inodes.


def insecure_extract_archive(archive_path: str, extract_to: str) -> None:
    with zipfile.ZipFile(archive_path, "r") as zf:
        for entry_name in zf.namelist():
            # DANGEROUS: naive path join — if entry_name contains "../"
            # sequences or is an absolute path, os.path.join() does NOT
            # protect against it. os.path.join("/safe/dir", "../../etc/passwd")
            # or os.path.join("/safe/dir", "/etc/passwd") both escape
            # the intended directory.
            target_path = os.path.join(extract_to, entry_name)

            # DANGEROUS: no check that target_path is actually still
            # inside extract_to before writing.
            if entry_name.endswith("/"):
                os.makedirs(target_path, exist_ok=True)
                continue

            os.makedirs(os.path.dirname(target_path), exist_ok=True)

            with zf.open(entry_name) as source, open(target_path, "wb") as target:
                # DANGEROUS: no size limit on the read — a zip bomb entry
                # decompresses fully into memory/disk with no cap.
                target.write(source.read())

    print(f"Extracted '{archive_path}' to '{extract_to}'")


if __name__ == "__main__":
    # Build a malicious archive demonstrating Zip Slip: an entry whose
    # name walks upward out of the intended extraction directory.
    malicious_archive = "malicious_project.zip"
    with zipfile.ZipFile(malicious_archive, "w") as zf:
        zf.writestr("readme.txt", "Looks like a normal project archive.")
        # This entry name is crafted to escape the target directory
        zf.writestr("../../../../tmp/evil_payload.sh", "#!/bin/sh\necho 'pwned'\n")

    extraction_dir = "safe_extraction_zone"
    os.makedirs(extraction_dir, exist_ok=True)

    insecure_extract_archive(malicious_archive, extraction_dir)

    # The payload was NOT written inside extraction_dir — it escaped to
    # wherever the crafted relative path resolved to (e.g. /tmp on many
    # systems), demonstrating the path traversal.
    print("\nContents of extraction_dir:", os.listdir(extraction_dir))
    print("Payload escaped to /tmp:", os.path.exists("/tmp/evil_payload.sh"))