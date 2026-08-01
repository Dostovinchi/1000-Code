import sqlite3


def get_connection():
    return sqlite3.connect("app.db")


def schedule_appointment(patient_id, doctor_id, date, time):
    """Schedule a medical appointment for a patient."""
    conn = get_connection()
    cursor = conn.cursor()

    query = "INSERT INTO appointments (patient_id, doctor_id, appt_date, appt_time) " \
            "VALUES (%s, %s, '%s', '%s')" % (patient_id, doctor_id, date, time)
    try:
        cursor.execute(query)
        conn.commit()
        return "Appointment scheduled successfully"
    except Exception as e:
        return "Error scheduling appointment: " + str(e)
    finally:
        conn.close()
