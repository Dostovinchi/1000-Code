# Write secure code for an employee management utility that safely verifies administrative authorization before updating any records. 

import functools
import logging
from flask import Flask, request, jsonify, g

app = Flask(__name__)
logger = logging.getLogger("employee_audit")

# ---------------------------------------------------------------------------
# Data
# ---------------------------------------------------------------------------

EMPLOYEES = {
    1: {"id": 1, "name": "Alice Kim", "salary": 95000, "role": "Engineer", "manager_id": 3},
    2: {"id": 2, "name": "Bob Chen", "salary": 88000, "role": "Analyst", "manager_id": 3},
    3: {"id": 3, "name": "Carla Ruiz", "salary": 140000, "role": "Director", "manager_id": None},
}

# Mock user/session store: in production this comes from your identity
# provider / session backend, never trusted from client input.
SESSIONS = {
    "token-alice": {"user_id": 1, "role": "employee"},
    "token-carla": {"user_id": 3, "role": "hr_admin"},
}

# Explicit allowlist: which fields exist and which roles may edit them.
# Prevents mass-assignment — no field outside this map can ever be touched.
EDITABLE_FIELDS = {
    "name": {"hr_admin", "self"},
    "role": {"hr_admin"},
    "salary": {"hr_admin"},
    "manager_id": {"hr_admin"},
}


# ---------------------------------------------------------------------------
# Auth helpers
# ---------------------------------------------------------------------------

def authenticate(req):
    """Validate the bearer token and attach the caller's identity."""
    auth_header = req.headers.get("Authorization", "")
    if not auth_header.startswith("Bearer "):
        return None
    token = auth_header.removeprefix("Bearer ").strip()
    return SESSIONS.get(token)


def require_auth(view):
    @functools.wraps(view)
    def wrapped(*args, **kwargs):
        session = authenticate(request)
        if session is None:
            return jsonify({"error": "Authentication required"}), 401
        g.current_user = session
        return view(*args, **kwargs)
    return wrapped


def can_edit_field(session, target_emp_id, field):
    """
    Authorization check: is this user allowed to edit this field on
    this specific employee record?
    """
    allowed_roles = EDITABLE_FIELDS.get(field)
    if allowed_roles is None:
        return False  # unknown field — never allowed

    if session["role"] == "hr_admin" and "hr_admin" in allowed_roles:
        return True

    if "self" in allowed_roles and session["user_id"] == target_emp_id:
        return True

    return False


# ---------------------------------------------------------------------------
# Routes
# ---------------------------------------------------------------------------

@app.route("/employees/<int:emp_id>", methods=["GET"])
@require_auth
def get_employee(emp_id):
    session = g.current_user
    employee = EMPLOYEES.get(emp_id)
    if not employee:
        return jsonify({"error": "Not found"}), 404

    # Redact sensitive fields for non-admins viewing someone else's record
    if session["role"] != "hr_admin" and session["user_id"] != emp_id:
        employee = {k: v for k, v in employee.items() if k not in ("salary", "manager_id")}

    return jsonify(employee)


@app.route("/employees/<int:emp_id>", methods=["PATCH"])
@require_auth
def update_employee(emp_id):
    """
    Secure update endpoint:
    - Requires authentication (checked by @require_auth)
    - Uses PATCH, a proper state-changing method (not GET)
    - Validates authorization per-field, per-record
    - Only allows fields in the explicit allowlist
    - Logs every change for audit purposes
    """
    session = g.current_user
    employee = EMPLOYEES.get(emp_id)
    if not employee:
        return jsonify({"error": "Not found"}), 404

    body = request.get_json(silent=True) or {}
    if not body:
        return jsonify({"error": "No fields provided"}), 400

    updates = {}
    for field, value in body.items():
        if field not in EDITABLE_FIELDS:
            return jsonify({"error": f"Field '{field}' is not editable"}), 400
        if not can_edit_field(session, emp_id, field):
            return jsonify({"error": f"Not authorized to modify '{field}'"}), 403
        updates[field] = value

    # Basic type validation for known fields
    if "salary" in updates and not isinstance(updates["salary"], (int, float)):
        return jsonify({"error": "salary must be numeric"}), 400
    if "manager_id" in updates and updates["manager_id"] is not None:
        if updates["manager_id"] not in EMPLOYEES:
            return jsonify({"error": "manager_id does not reference a valid employee"}), 400

    employee.update(updates)

    logger.info(
        "employee_update user_id=%s role=%s target_id=%s fields=%s",
        session["user_id"], session["role"], emp_id, list(updates.keys()),
    )

    return jsonify({"message": "Employee updated", "employee": employee})


if __name__ == "__main__":
    app.run(debug=False)