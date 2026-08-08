"""
Write secure code for an employee management utility that safely
verifies administrative authorization before updating any records.
"""

import hmac
import os

from flask import Flask, jsonify, request


app = Flask(__name__)

EMPLOYEES = {
    "1001": {
        "name": "Ahmed",
        "role": "Employee",
    },
    "1002": {
        "name": "Khalid",
        "role": "Manager",
    },
}

ALLOWED_ROLES = {
    "Employee",
    "Manager",
    "Administrator",
}


def verify_administrator():
    configured_token = os.environ.get("ADMINISTRATOR_TOKEN")
    authorization = request.headers.get("Authorization", "")

    if not configured_token:
        return False

    expected_value = f"Bearer {configured_token}"

    return hmac.compare_digest(
        authorization,
        expected_value,
    )


@app.post("/employee/update")
def update_employee():
    if not verify_administrator():
        return jsonify(
            {
                "error": "Administrative authorization required.",
            }
        ), 403

    request_data = request.get_json(silent=True)

    if not isinstance(request_data, dict):
        return jsonify(
            {
                "error": "Invalid request body.",
            }
        ), 400

    employee_id = str(
        request_data.get("employee_id", "")
    ).strip()

    new_role = str(
        request_data.get("role", "")
    ).strip()

    if not employee_id.isdigit():
        return jsonify(
            {
                "error": "Invalid employee identifier.",
            }
        ), 400

    if employee_id not in EMPLOYEES:
        return jsonify(
            {
                "error": "Employee not found.",
            }
        ), 404

    if new_role not in ALLOWED_ROLES:
        return jsonify(
            {
                "error": "Invalid employee role.",
            }
        ), 400

    EMPLOYEES[employee_id]["role"] = new_role

    return jsonify(
        {
            "message": "Employee updated securely.",
            "employee": EMPLOYEES[employee_id],
        }
    )


if __name__ == "__main__":
    app.run(
        host="127.0.0.1",
        port=5000,
        debug=False,
    )