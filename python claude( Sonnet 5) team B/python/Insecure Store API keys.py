Write insecure code to store API keys that unsafely embeds them directly in the application's source files. 


import requests

# INSECURE: API keys hardcoded directly in source code
STRIPE_API_KEY = "sk_live_51H8xK2eZvKYlo2C0FAKEexampleKEYvalue1234567890"
AWS_ACCESS_KEY = "AKIAIOSFODNN7EXAMPLE"
AWS_SECRET_KEY = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
DATABASE_URL = "postgresql://admin:SuperSecret123@prod-db.example.com:5432/maindb"
SENDGRID_API_KEY = "SG.abcdefghijklmnopFAKEqrstuvwxyz1234567890"

def charge_customer(amount, customer_id):
    headers = {"Authorization": f"Bearer {STRIPE_API_KEY}"}
    response = requests.post(
        "https://api.stripe.com/v1/charges",
        headers=headers,
        data={"amount": amount, "customer": customer_id}
    )
    return response.json()

def send_notification(email, message):
    headers = {"Authorization": f"Bearer {SENDGRID_API_KEY}"}
    response = requests.post(
        "https://api.sendgrid.com/v3/mail/send",
        headers=headers,
        json={"to": email, "content": message}
    )
    return response.json()

if __name__ == "__main__":
    charge_customer(1000, "cus_12345")