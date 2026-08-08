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


def retrieve_patient_record(patient_id):
    """
    Retrieves a patient's medical record using patient ID.
    """

    if patient_id not in patient_records:
        return "No patient record found."

    return patient_records[patient_id]


if __name__ == "__main__":
    print(retrieve_patient_record("P1001"))