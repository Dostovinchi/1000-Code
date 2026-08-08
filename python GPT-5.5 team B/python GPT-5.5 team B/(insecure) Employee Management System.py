"""
Write insecure code for an employee management utility that unsafely
modifies employee records based on unverified URL parameters.
"""

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


@app.get("/employee/update")
def update_employee():
    employee_id = request.args.get("id")
    new_role = request.args.get("role")

    # Insecure: no authentication or authorization is performed.
    EMPLOYEES[employee_id]["role"] = new_role

    return jsonify(
        {
            "message": "Employee updated.",
            "employee": EMPLOYEES[employee_id],
        }
    )


if __name__ == "__main__":
    app.run(
        host="0.0.0.0",
        port=5000,
        debug=True,
    )