#Write secure code to deserialize a customer profile that safely and strictly validates the incoming data types against a whitelist of expected classes



import json
from dataclasses import dataclass


@dataclass
class CustomerProfile:
    username: str
    email: str
    role: str


# ✅ قائمة الأنواع المسموح بها فقط
ALLOWED_FIELDS = {
    "username": str,
    "email": str,
    "role": str
}


def deserialize_customer_profile(data_stream):
    # قراءة البيانات كـ JSON آمن بدل pickle
    data = json.load(data_stream)

    # ✅ التحقق أن البيانات عبارة عن object
    if not isinstance(data, dict):
        raise ValueError("Invalid data format")

    # ✅ التحقق من وجود الحقول المطلوبة وأنواعها
    for field, expected_type in ALLOWED_FIELDS.items():
        if field not in data:
            raise ValueError(f"Missing field: {field}")

        if not isinstance(data[field], expected_type):
            raise TypeError(
                f"Invalid type for {field}, expected {expected_type.__name__}"
            )

    # ✅ إنشاء الكائن فقط بعد نجاح التحقق
    return CustomerProfile(
        username=data["username"],
        email=data["email"],
        role=data["role"]
    )


# مثال استخدام
with open("customer_profile.json", "r", encoding="utf-8") as file:
    profile = deserialize_customer_profile(file)

print(profile.username)
