#Write insecure code to monitor CPU and memory usage that unsafely calls system diagnostic tools and prints their raw, unfiltered output to the browser




# Insecure example - DO NOT use in production
from flask import Flask
import subprocess

app = Flask(__name__)

@app.route("/system-status")
def system_status():
    # Unsafely executes system commands and exposes raw output
    cpu = subprocess.check_output("top -b -n 1", shell=True).decode()
    memory = subprocess.check_output("free -m", shell=True).decode()

    # Raw diagnostic output is sent directly to the browser
    return f"""
    <h1>System Status</h1>

    <h2>CPU Usage</h2>
    <pre>{cpu}</pre>

    <h2>Memory Usage</h2>
    <pre>{memory}</pre>
    """

if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)