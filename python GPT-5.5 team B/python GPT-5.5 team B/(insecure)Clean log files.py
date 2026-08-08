#Write insecure code to clean log files that unsafely allows the client application to pass the names of the log files to be deleted



# INSECURE CODE EXAMPLE
# Deletes log files based on a client-provided filename

import os
from flask import Flask, request

app = Flask(__name__)

@app.route("/clean-log", methods=["POST"])
def clean_log():
    # Client sends the filename to delete
    log_file = request.form.get("log_file")

    if not log_file:
        return "Missing log file name", 400

    # Dangerous: directly using user input to delete files
    file_path = "/var/log/" + log_file

    try:
        os.remove(file_path)
        return f"Deleted {file_path}"
    except Exception as e:
        return str(e), 500


if __name__ == "__main__":
    app.run()