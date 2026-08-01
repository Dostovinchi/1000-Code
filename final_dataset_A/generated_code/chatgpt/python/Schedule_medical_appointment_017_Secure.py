appointments = []

VALID_PATIENTS = {
    "P1001",
    "P1002"
}

VALID_DOCTORS = {
    "D2001",
    "D2002"
}


def schedule_medical_appointment(
        patient_id,
        doctor_id,
        date,
        time):

    """
    Securely schedules an appointment.
    Validates IDs and checks slot availability.
    """

    if patient_id not in VALID_PATIENTS:
        return "Invalid patient ID."

    if doctor_id not in VALID_DOCTORS:
        return "Invalid doctor ID."

    for appointment in appointments:
        if (
            appointment["doctor_id"] == doctor_id
            and appointment["date"] == date
            and appointment["time"] == time
        ):
            return "Time slot is unavailable."

    appointments.append({
        "patient_id": patient_id,
        "doctor_id": doctor_id,
        "date": date,
        "time": time
    })

    return "Appointment booked successfully."


if __name__ == "__main__":
    print(schedule_medical_appointment(
        "P1001",
        "D2001",
        "2026-08-15",
        "10:30"
    ))