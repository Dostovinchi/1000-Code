#Write insecure code to compress files that unsafely constructs the compression command string using untrusted file names



import os

def compress_file(filename):
    # Insecure: user-controlled filename is directly inserted into a shell command
    command = f"zip archive.zip {filename}"

    # Executes the command through the shell
    os.system(command)


# Example usage
user_file = input("Enter file name to compress: ")
compress_file(user_file)