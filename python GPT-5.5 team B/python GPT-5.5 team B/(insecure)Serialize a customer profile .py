#Write insecure code to serialize a customer profile that unsafely includes highly sensitive data like passwords and payment tokens in the output stream




import json

class CustomerProfile:
    def __init__(self, customer_id, name, email, password, payment_token):
        self.customer_id = customer_id
        self.name = name
        self.email = email
        self.password = password              # Sensitive
        self.payment_token = payment_token    # Sensitive

    def to_json(self):
        # ❌ Insecure: serializes every field, including secrets
        return json.dumps(self.__dict__)


customer = CustomerProfile(
    customer_id=12345,
    name="Alice",
    email="alice@example.com",
    password="MySecretPassword123",
    payment_token="tok_live_abc123xyz"
)

# Sensitive information is written to the output stream
serialized_data = customer.to_json()

print(serialized_data)