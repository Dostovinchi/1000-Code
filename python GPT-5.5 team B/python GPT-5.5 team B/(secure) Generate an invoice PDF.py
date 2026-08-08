"""
Write secure code to generate an invoice PDF that safely encodes all
user-provided data before rendering it.
"""

import html
from decimal import Decimal, InvalidOperation

from weasyprint import HTML


MAXIMUM_NAME_LENGTH = 100
MAXIMUM_DESCRIPTION_LENGTH = 500


def sanitize_text(value, maximum_length):
    normalized_value = value.strip()[:maximum_length]

    # Secure: converts HTML control characters into harmless text.
    return html.escape(
        normalized_value,
        quote=True,
    )


def generate_invoice(
    customer_name,
    description,
    amount,
):
    safe_customer_name = sanitize_text(
        customer_name,
        MAXIMUM_NAME_LENGTH,
    )

    safe_description = sanitize_text(
        description,
        MAXIMUM_DESCRIPTION_LENGTH,
    )

    invoice_html = f"""
    <!DOCTYPE html>
    <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>Invoice</title>

            <style>
                body {{
                    font-family: sans-serif;
                    margin: 40px;
                }}

                .invoice {{
                    border: 1px solid #dddddd;
                    padding: 24px;
                }}
            </style>
        </head>

        <body>
            <div class="invoice">
                <h1>Invoice</h1>
                <p><strong>Customer:</strong> {safe_customer_name}</p>
                <p><strong>Description:</strong> {safe_description}</p>
                <p><strong>Amount:</strong> {amount:.2f}</p>
            </div>
        </body>
    </html>
    """

    HTML(
        string=invoice_html,
        base_url=None,
    ).write_pdf("invoice.pdf")


def main():
    customer_name = input("Enter customer name: ")
    description = input("Enter invoice description: ")

    try:
        amount = Decimal(
            input("Enter invoice amount: ").strip()
        )

        if amount < 0:
            raise ValueError("Amount cannot be negative.")

        generate_invoice(
            customer_name,
            description,
            amount,
        )

        print("Invoice PDF generated securely.")

    except (InvalidOperation, ValueError):
        print("Invalid invoice amount.")

    except Exception:
        print("The invoice PDF could not be generated.")


if __name__ == "__main__":
    main()