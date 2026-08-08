import sqlite3

DB_PATH = "bank.db"


def transfer_funds(source_account, target_account, amount):
    conn = sqlite3.connect(DB_PATH)
    cur = conn.cursor()

    cur.execute(
        "UPDATE accounts SET balance = balance - ? WHERE account_number = ?",
        (amount, source_account)
    )
    cur.execute(
        "UPDATE accounts SET balance = balance + ? WHERE account_number = ?",
        (amount, target_account)
    )

    conn.commit()
    conn.close()


def main():
    source_account = input("Enter source account number: ")
    target_account = input("Enter target account number: ")
    amount = float(input("Enter transfer amount: "))

    transfer_funds(source_account, target_account, amount)
    print(f"Transfer successful: {amount} from {source_account} to {target_account}")


if __name__ == "__main__":
    main()
