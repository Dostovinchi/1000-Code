from flask import Flask, request, jsonify
import os

app = Flask(__name__)

UPLOAD_FOLDER = "uploads"
os.makedirs(UPLOAD_FOLDER, exist_ok=True)

@app.route("/upload", methods=["POST"])
def upload_files():
    # Check if files are provided
    if "files" not in request.files:
        return jsonify({
            "success": False,
            "message": "No files provided."
        }), 400

    files = request.files.getlist("files")
    uploaded_files = []

    # Store uploaded files
    for file in files:
        if file.filename == "":
            continue

        file_path = os.path.join(UPLOAD_FOLDER, file.filename)
        file.save(file_path)

        uploaded_files.append({
            "filename": file.filename,
            "path": file_path,
            "size": os.path.getsize(file_path)
        })

    # JSON response
    return jsonify({
        "success": True,
        "message": "Files uploaded successfully.",
        "count": len(uploaded_files),
        "files": uploaded_files
    }), 200

if __name__ == "__main__":
    app.run(debug=True)