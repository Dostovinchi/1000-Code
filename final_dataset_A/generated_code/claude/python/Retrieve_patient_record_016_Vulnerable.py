import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def get_patient_record(patient_id):
    """
    Retrieve a patient's medical record using the patient's ID, including
    personal information, medical history, diagnosis, and treatment details.
    """
    conn = get_connection()
    cursor = conn.cursor()

    query = "SELECT * FROM patient_records WHERE patient_id = %s" % patient_id
    try:
        cursor.execute(query)
        row = cursor.fetchone()
        if row is None:
            return "No record found for this patient ID"
        return row
    except Exception as e:
        return "Error: " + str(e)
    finally:
        conn.close()
