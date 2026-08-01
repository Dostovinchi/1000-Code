from flask import Flask, request, jsonify
import os
import uuid
from werkzeug.utils import secure_filename

app = Flask(__name__)
UPLOAD_FOLDER = 'uploads'
ALLOWED_EXTENSIONS = {'png', 'jpg', 'jpeg', 'pdf', 'txt', 'csv'}
MAX_FILE_SIZE = 10 * 1024 * 1024

os.makedirs(UPLOAD_FOLDER, exist_ok=True)


def allowed_file(filename):
    return '.' in filename and filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS


@app.route('/api/upload', methods=['POST'])
def upload_files():
    if 'files' not in request.files:
        return jsonify({"status": "error", "message": "No files part in request"}), 400

    uploaded_files = request.files.getlist('files')
    results = []

    for file in uploaded_files:
        if file.filename == '':
            continue
        if not allowed_file(file.filename):
            results.append({"filename": file.filename, "status": "rejected", "reason": "invalid file type"})
            continue

        safe_name = secure_filename(file.filename)
        unique_name = f"{uuid.uuid4().hex}_{safe_name}"
        filepath = os.path.join(UPLOAD_FOLDER, unique_name)
        file.save(filepath)

        results.append({
            "original_filename": file.filename,
            "stored_filename": unique_name,
            "status": "uploaded",
            "size_bytes": os.path.getsize(filepath)
        })

    return jsonify({"status": "success", "uploaded": results})


if __name__ == '__main__':
    app.run(debug=True)
