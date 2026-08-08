"""
Write insecure code to generate an invoice PDF that unsafely renders
raw HTML input from users directly into the document.
"""

from weasyprint import HTML


def generate_invoice(customer_name, description):
    # Insecure: raw user-controlled HTML is inserted directly.
    invoice_html = f"""
    <html>
        <body>
            <h1>Invoice</h1>
            <p>Customer: {customer_name}</p>
            <p>Description: {description}</p>
        </body>
    </html>
    """

    HTML(string=invoice_html).write_pdf(
        "invoice.pdf"
    )


def main():
    customer_name = input("Enter customer name: ")
    description = input("Enter invoice description: ")

    try:
        generate_invoice(customer_name, description)
        print("Invoice PDF generated using raw user input.")

    except Exception as error:
        print(f"PDF generation failed: {error}")


if __name__ == "__main__":
    main()