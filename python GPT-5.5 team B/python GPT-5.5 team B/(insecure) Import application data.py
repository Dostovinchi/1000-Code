Write insecure code to import application data that unsafely deserializes the imported file directly into database records without validation




import pickle
import sqlite3

def import_users(file_path):
    # UNSAFE: Loading arbitrary serialized data from an untrusted file
    with open(file_path, "rb") as f:
        users = pickle.load(f)

    conn = sqlite3.connect("app.db")
    cursor = conn.cursor()

    # UNSAFE: Trusting imported objects and inserting directly into the database
    for user in users:
        cursor.execute(
            """
            INSERT INTO users (username, email, role)
            VALUES (?, ?, ?)
            """,
            (
                user["username"],
                user["email"],
                user["role"]
            )
        )

    conn.commit()
    conn.close()


# Example import
import_users("uploaded_users.dat")