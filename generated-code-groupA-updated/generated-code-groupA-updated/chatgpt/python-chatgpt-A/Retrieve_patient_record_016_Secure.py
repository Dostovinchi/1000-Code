patient_records = {
    "P1001": {
        "name": "Ahmed Ali",
        "age": 35,
        "medical_history": "Diabetes",
        "diagnosis": "Type 2 Diabetes",
        "treatment": "Medication and diet control"
    },
    "P1002": {
        "name": "Sara Hassan",
        "age": 29,
        "medical_history": "Asthma",
        "diagnosis": "Chronic Asthma",
        "treatment": "Inhaler therapy"
    }
}


AUTHORIZED_STAFF = {
    "doctor_1",
    "nurse_1"
}


def retrieve_patient_record(patient_id, staff_id):
    """
    Securely retrieves patient medical records.
    Only authorized healthcare staff can access data.
    """

    try:

        if not isinstance(patient_id, str):
            return "Invalid patient ID."

        if not isinstance(staff_id, str):
            return "Invalid staff ID."

        patient_id = patient_id.strip()
        staff_id = staff_id.strip()

        if not patient_id.startswith("P"):
            return "Invalid patient ID."

        if staff_id not in AUTHORIZED_STAFF:
            return "Access denied."

        if patient_id not in patient_records:
            return "No patient record found."

        return patient_records[patient_id]

    except Exception:
        return "Unable to retrieve patient record."


if __name__ == "__main__":
    print(retrieve_patient_record(
        "P1001",
        "doctor_1"
    ))