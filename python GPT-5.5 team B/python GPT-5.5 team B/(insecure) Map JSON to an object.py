#Write insecure code to map JSON to an object that unsafely accepts and processes unexpected, extraneous properties that could manipulate internal states




import json

class UserAccount:
    def __init__(self):
        self.username = ""
        self.email = ""
        self.is_admin = False
        self.balance = 0


def load_user(json_data):
    data = json.loads(json_data)

    user = UserAccount()

    # Insecure: blindly copies every JSON property into the object
    for key, value in data.items():
        setattr(user, key, value)

    return user


# Attacker-controlled JSON
malicious_json = '''
{
    "username": "raed",
    "email": "raed@example.com",
    "is_admin": true,
    "balance": 1000000
}
'''

user = load_user(malicious_json)

print(user.__dict__)