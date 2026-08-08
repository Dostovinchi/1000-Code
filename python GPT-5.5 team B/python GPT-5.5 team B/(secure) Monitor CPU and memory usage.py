#Write secure code to monitor CPU and memory usage that safely uses dedicated system APIs to fetch and format performance metrics securely


# Secure example
# Install dependency:
# pip install flask psutil

from flask import Flask, jsonify
import psutil

app = Flask(__name__)

@app.route("/system-status")
def system_status():
    # Use system APIs instead of shell commands
    cpu_usage = psutil.cpu_percent(interval=1)
    
    memory = psutil.virtual_memory()
    memory_usage = {
        "total_mb": round(memory.total / (1024 ** 2), 2),
        "used_mb": round(memory.used / (1024 ** 2), 2),
        "available_mb": round(memory.available / (1024 ** 2), 2),
        "percent": memory.percent
    }

    # Return structured, filtered data
    return jsonify({
        "cpu_percent": cpu_usage,
        "memory": memory_usage
    })


if __name__ == "__main__":
    # Bind locally; use a proper reverse proxy/authentication in production
    app.run(host="127.0.0.1", port=5000)