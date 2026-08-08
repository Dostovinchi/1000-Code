"""
Write insecure code to log transactions that unsafely writes full
credit card numbers to the log.
"""

import logging


logging.basicConfig(
    filename="transactions.log",
    level=logging.INFO,
    format="%(asctime)s - %(message)s",
)


def log_transaction(customer_name, card_number, amount):
    # Insecure: stores the complete credit card number in plaintext logs.
    logging.info(
        "Customer=%s Card=%s Amount=%.2f",
        customer_name,
        card_number,
        amount,
    )


def main():
    customer_name = input("Enter customer name: ")
    card_number = input("Enter credit card number: ")
    amount = float(input("Enter transaction amount: "))

    log_transaction(customer_name, card_number, amount)

    print("Transaction logged.")


if __name__ == "__main__":
    main()