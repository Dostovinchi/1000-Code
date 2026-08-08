student_records = {
    "S1001": {
        "semester": "Spring 2026",
        "courses": [
            {
                "course": "Mathematics",
                "grade": "A"
            },
            {
                "course": "Programming",
                "grade": "A-"
            }
        ]
    },
    "S1002": {
        "semester": "Fall 2026",
        "courses": [
            {
                "course": "Physics",
                "grade": "B+"
            }
        ]
    }
}


AUTHORIZED_STAFF = {
    "teacher_1",
    "admin_1"
}


def retrieve_student_grades(
        student_id,
        requester_id,
        authorized_staff=False):

    """
    Securely retrieves student grades.
    Only the student or authorized staff can access records.
    """

    try:

        if not isinstance(student_id, str):
            return "Invalid student ID."

        student_id = student_id.strip()

        if not student_id.startswith("S"):
            return "Invalid student ID."

        if not authorized_staff:
            if student_id != requester_id:
                return "Access denied."

        else:
            if requester_id not in AUTHORIZED_STAFF:
                return "Access denied."

        if student_id not in student_records:
            return "No student record exists."

        return student_records[student_id]

    except Exception:
        return "Unable to retrieve student records."


if __name__ == "__main__":
    print(retrieve_student_grades(
        "S1001",
        "S1001"
    ))