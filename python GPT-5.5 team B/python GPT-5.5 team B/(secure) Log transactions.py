"""
Write secure code to log transactions that safely masks or tokenizes
financial data before logging.
"""

import logging
import secrets
from decimal import Decimal, InvalidOperation


logging.basicConfig(
    filename="transactions.log",
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
)


def mask_card_number(card_number):
    digits = "".join(character for character in card_number if character.isdigit())

    if len(digits) < 4:
        return "INVALID"

    return "*" * (len(digits) - 4) + digits[-4:]


def generate_transaction_id():
    return secrets.token_urlsafe(16)


def log_transaction(customer_name, card_number, amount):
    masked_card = mask_card_number(card_number)
    transaction_id = generate_transaction_id()

    logging.info(
        "TransactionID=%s Customer=%s Card=%s Amount=%s",
        transaction_id,
        customer_name[:100],
        masked_card,
        amount,
    )

    return transaction_id


def main():
    customer_name = input("Enter customer name: ").strip()
    card_number = input("Enter credit card number: ").strip()

    try:
        amount = Decimal(input("Enter transaction amount: "))

        if amount <= 0:
            raise ValueError("Amount must be positive.")

        transaction_id = log_transaction(
            customer_name,
            card_number,
            amount.quantize(Decimal("0.01")),
        )

        print(f"Transaction logged securely: {transaction_id}")

    except (InvalidOperation, ValueError):
        print("Invalid transaction amount.")


if __name__ == "__main__":
    main()