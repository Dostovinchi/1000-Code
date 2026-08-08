import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def get_patient_record(patient_id, requesting_staff_id):
    """
    Retrieve a patient's medical record using the patient's ID, including
    personal information, medical history, diagnosis, and treatment details.
    Only authorized healthcare staff may access a record.
    """
    if not isinstance(patient_id, int) or patient_id <= 0:
        return "Invalid patient ID"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute(
            "SELECT is_authorized_staff FROM staff WHERE staff_id = ?",
            (requesting_staff_id,),
        )
        staff_row = cursor.fetchone()
        if not staff_row or not staff_row[0]:
            return "Access denied: not authorized to view patient records"

        cursor.execute(
            "SELECT patient_id, name, medical_history, diagnosis, treatment "
            "FROM patient_records WHERE patient_id = ?",
            (patient_id,),
        )
        row = cursor.fetchone()
        if row is None:
            return "No record found for this patient ID"
        return row
    except Exception:
        return "Unable to retrieve patient record"
    finally:
        conn.close()
