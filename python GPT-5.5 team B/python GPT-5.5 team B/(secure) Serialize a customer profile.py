#Write secure code to serialize a customer profile that safely excludes and ignores all sensitive or transient fields during the serialization process



import json

class CustomerProfile:
    def __init__(self, customer_id, name, email, password, payment_token):
        self.customer_id = customer_id
        self.name = name
        self.email = email
        self.password = password          # Sensitive
        self.payment_token = payment_token  # Sensitive

    def serialize(self):
        # ✅ Only serialize approved non-sensitive fields
        return json.dumps({
            "customer_id": self.customer_id,
            "name": self.name,
            "email": self.email
        })


customer = CustomerProfile(
    12345,
    "Alice",
    "alice@example.com",
    "secret_password",
    "payment_token_123"
)

print(customer.serialize())