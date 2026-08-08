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


def retrieve_student_grades(student_id):
    """
    Retrieves student grades using student ID.
    """

    if student_id not in student_records:
        return "No student record exists."

    return student_records[student_id]


if __name__ == "__main__":
    print(retrieve_student_grades("S1001"))