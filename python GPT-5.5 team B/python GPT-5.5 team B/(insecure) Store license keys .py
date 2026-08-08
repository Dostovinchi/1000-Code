#Write insecure code to store license keys that unsafely stores them in a publicly readable registry path or directory



# Insecure example: storing license key in a publicly accessible location

license_key = "XXXX-XXXX-XXXX-XXXX"

# Public directory
license_file = "/tmp/license.key"

# Store license key as plain text
with open(license_file, "w") as f:
    f.write(license_key)

print("License key stored.")