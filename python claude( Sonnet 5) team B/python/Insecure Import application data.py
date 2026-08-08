# Write insecure code to import application data that unsafely deserializes the imported file directly into database records without validation 

import pickle
import csv
import json
from sqlalchemy.orm import Session
from models import Customer  # SQLAlchemy ORM model

# ⚠️ INSECURE EXAMPLE — DO NOT USE IN PRODUCTION ⚠️
# This "import" feature is common in admin panels and data-migration
# tools — but if it deserializes an uploaded file directly into ORM
# records without validation, it's both an RCE vector and a mass
# assignment vector rolled into one.

def import_customers_from_pickle(file_path: str, session: Session):
    # pickle.load() on an uploaded file executes arbitrary code the
    # moment this line runs, before any "import" logic even begins.
    with open(file_path, "rb") as f:
        records = pickle.load(f)

    for record in records:
        # record is trusted blindly — could be any object, with any
        # attributes, including ones never meant to be set this way.
        session.add(record)

    session.commit()


def import_customers_from_json(file_path: str, session: Session):
    with open(file_path, "r") as f:
        records = json.load(f)

    for row in records:
        # Directly unpacks every key in the uploaded JSON as ORM
        # constructor kwargs — no allowlist, no type checking, no
        # validation. Any field name that happens to match a column
        # (including internal/privileged ones) gets set.
        customer = Customer(**row)
        session.add(customer)

    session.commit()


# ── Example: normal-looking JSON import ──
# customers.json:
# [
#   {"customer_id": "C1", "name": "Jane Doe", "email": "jane@example.com"}
# ]
# Looks fine on the surface.

# ── Why this is exploitable ──
# If Customer has columns like is_admin, account_balance, role, etc.,
# an attacker who can influence the uploaded file (a compromised
# partner feed, a malicious CSV-to-JSON conversion step, a shared
# "bulk import" endpoint) can inject:
#
# [
#   {
#     "customer_id": "C1",
#     "name": "Jane Doe",
#     "email": "jane@example.com",
#     "is_admin": true,
#     "account_balance": 999999.0,
#     "role": "superadmin"
#   }
# ]
#
# Customer(**row) sets every one of those attributes directly on the
# ORM object, and session.commit() persists it straight to the
# database — no application logic ever gets a chance to reject it.
#
# And the pickle path is worse: import_customers_from_pickle() can
# achieve full remote code execution the instant an attacker-supplied
# file is loaded, before any record is even inspected.