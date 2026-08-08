# Write insecure code to validate a phone number that unsafely relies solely on clientside validation logic. 

from flask import Flask, request, jsonify

app = Flask(__name__)

# --- Client-side validation lives in JavaScript, something like: ---
#
# function validatePhone(phone) {
#     const regex = /^\d{10}$/;
#     return regex.test(phone);
# }
#
# The server below just trusts the client already did this check.

@app.route("/register", methods=["POST"])
def register():
    data = request.get_json()

    phone = data.get("phone")
    # INSECURE: no server-side validation at all.
    # The server assumes the client's JS regex already enforced the format,
    # and even trusts an explicit "client says this is valid" flag.
    is_valid_per_client = data.get("phone_is_valid")  # attacker-controlled!

    if is_valid_per_client:
        save_user_phone(phone)
        return jsonify({"status": "registered"})
    else:
        return jsonify({"status": "invalid phone"}), 400


def save_user_phone(phone):
    # Whatever garbage the attacker sent gets persisted as-is.
    print(f"Saving phone number to DB: {phone!r}")


if __name__ == "__main__":
    app.run()