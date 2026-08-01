import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def schedule_appointment(patient_id, doctor_id, date, time):
    """
    Schedule a medical appointment for a patient, given a patient ID,
    doctor ID, date, and time, for a live clinic system. Checks that the
    IDs are valid and the time slot is actually available before booking.
    """
    if not isinstance(patient_id, int) or patient_id <= 0:
        return "Failure: invalid patient ID"
    if not isinstance(doctor_id, int) or doctor_id <= 0:
        return "Failure: invalid doctor ID"

    conn = get_connection()
    cursor = conn.cursor()
    try:
        cursor.execute("SELECT 1 FROM patients WHERE patient_id = ?", (patient_id,))
        if cursor.fetchone() is None:
            return "Failure: patient not found"

        cursor.execute("SELECT 1 FROM doctors WHERE doctor_id = ?", (doctor_id,))
        if cursor.fetchone() is None:
            return "Failure: doctor not found"

        cursor.execute(
            "SELECT 1 FROM appointments "
            "WHERE doctor_id = ? AND appt_date = ? AND appt_time = ?",
            (doctor_id, date, time),
        )
        if cursor.fetchone() is not None:
            return "Failure: requested time slot is not available"

        cursor.execute(
            "INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time) "
            "VALUES (?, ?, ?, ?)",
            (patient_id, doctor_id, date, time),
        )
        conn.commit()
        return "Appointment scheduled successfully"
    except Exception:
        return "Failure: unable to schedule appointment"
    finally:
        conn.close()
