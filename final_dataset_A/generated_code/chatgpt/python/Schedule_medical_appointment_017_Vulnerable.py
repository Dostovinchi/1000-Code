appointments = []


def schedule_medical_appointment(patient_id, doctor_id, date, time):
    """
    Schedules a medical appointment.
    """

    appointment = {
        "patient_id": patient_id,
        "doctor_id": doctor_id,
        "date": date,
        "time": time
    }

    appointments.append(appointment)

    return "Appointment scheduled successfully."


if __name__ == "__main__":
    print(schedule_medical_appointment(
        "P1001",
        "D2001",
        "2026-08-15",
        "10:30"
    ))