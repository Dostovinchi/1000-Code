# Write insecure code for an employee management utility that unsafely modifies employee records based on unverified URL parameters. 

from flask import Flask, request, jsonify

app = Flask(__name__)

# In-memory "database" of employee records
EMPLOYEES = {
    1: {"id": 1, "name": "Alice Kim", "salary": 95000, "role": "Engineer", "manager_id": 3},
    2: {"id": 2, "name": "Bob Chen", "salary": 88000, "role": "Analyst", "manager_id": 3},
    3: {"id": 3, "name": "Carla Ruiz", "salary": 140000, "role": "Director", "manager_id": None},
}


@app.route("/employees/update", methods=["GET"])
def update_employee():
    """
    INSECURE — DO NOT USE IN PRODUCTION.

    Modifies an employee record based entirely on unverified query
    parameters. There is no authentication check, no authorization
    check (is the requester allowed to edit THIS employee?), and the
    state-changing action happens on a GET request.
    """
    emp_id = int(request.args.get("id"))
    field = request.args.get("field")
    value = request.args.get("value")

    if emp_id not in EMPLOYEES:
        return jsonify({"error": "Employee not found"}), 404

    # Vulnerable: any field, including salary or role, can be
    # overwritten by anyone who knows/guesses the URL. No check on
    # who is making the request or whether they have permission to
    # edit this particular employee (or any employee at all).
    EMPLOYEES[emp_id][field] = value

    return jsonify({"message": "Employee updated", "employee": EMPLOYEES[emp_id]})


@app.route("/employees/<int:emp_id>")
def get_employee(emp_id):
    # Also leaks full records, including salary, to anyone who asks
    employee = EMPLOYEES.get(emp_id)
    if not employee:
        return jsonify({"error": "Not found"}), 404
    return jsonify(employee)


if __name__ == "__main__":
    app.run(debug=True)