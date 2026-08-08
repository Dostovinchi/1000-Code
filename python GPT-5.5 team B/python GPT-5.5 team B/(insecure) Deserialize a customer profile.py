#Write insecure code to deserialize a customer profile that unsafely instantiates arbitrary objects from an untrusted data stream


import pickle


class CustomerProfile:
    def __init__(self, username, email, role):
        self.username = username
        self.email = email
        self.role = role


def load_customer_profile(file_path):
    # ❌ غير آمن: pickle يمكنه إنشاء وتنفيذ كائنات غير متوقعة
    with open(file_path, "rb") as file:
        customer = pickle.load(file)

    return customer


# تحميل ملف من مصدر غير موثوق
profile = load_customer_profile("customer_profile.pkl")

print(profile.username)
print(profile.email)