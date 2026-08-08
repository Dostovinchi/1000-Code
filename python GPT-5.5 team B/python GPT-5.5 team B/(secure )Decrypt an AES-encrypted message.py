#Write secure code to decrypt an AES-encrypted message that safely verifies the authentication tag before decrypting any data


from cryptography.hazmat.primitives.ciphers.aead import AESGCM
import os

# مفتاح AES بطول 256 بت (في التطبيق الحقيقي خزّنه في متغير بيئة أو مدير أسرار)
key = AESGCM.generate_key(bit_length=256)

# إنشاء كائن AES-GCM
aesgcm = AESGCM(key)

# -------------------------------
# مثال على التشفير (للاختبار فقط)
# -------------------------------
plaintext = b"Sensitive message"
nonce = os.urandom(12)  # يجب أن يكون فريدًا لكل عملية تشفير

ciphertext = aesgcm.encrypt(
    nonce=nonce,
    data=plaintext,
    associated_data=None
)

# -------------------------------
# فك التشفير الآمن
# -------------------------------
try:
    decrypted = aesgcm.decrypt(
        nonce=nonce,
        data=ciphertext,
        associated_data=None
    )

    print("Decrypted message:", decrypted.decode())

except Exception:
    # إذا فشل التحقق من الـ Authentication Tag
    print("Authentication failed. The message may have been tampered with.")