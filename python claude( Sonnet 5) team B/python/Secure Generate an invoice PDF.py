# Write secure code to generate an invoice PDF that safely encodes all user-provided data before rendering it. 

import html
from decimal import Decimal, InvalidOperation
from markupsafe import escape  # pip install markupsafe
import pdfkit  # pip install pdfkit (requires wkhtmltopdf binary)


class InvoiceValidationError(Exception):
    pass


def _validate_item(item: dict) -> dict:
    """Validate and coerce item fields before use."""
    description = str(item.get("description", ""))[:200]  # cap length
    try:
        quantity = int(item["quantity"])
        price = Decimal(str(item["price"]))
    except (KeyError, ValueError, InvalidOperation):
        raise InvoiceValidationError(f"Invalid item data: {item}")

    if quantity < 0 or price < 0:
        raise InvoiceValidationError("Quantity and price must be non-negative")

    return {"description": description, "quantity": quantity, "price": price}


def generate_invoice(customer_name: str, items: list[dict], notes: str,
                      output_path: str = "invoice.pdf") -> str:
    """
    Securely generate an invoice PDF. All user-supplied text is HTML-escaped
    before insertion into the template, and the renderer is configured to
    block remote/local resource loading and JavaScript execution.
    """
    # Cap lengths and escape everything that came from the user
    safe_customer_name = escape(str(customer_name)[:200])
    safe_notes = escape(str(notes)[:2000])

    validated_items = [_validate_item(i) for i in items]

    items_html = ""
    for item in validated_items:
        line_total = item["quantity"] * item["price"]
        items_html += f"""
        <tr>
            <td>{escape(item['description'])}</td>
            <td>{item['quantity']}</td>
            <td>${item['price']:.2f}</td>
            <td>${line_total:.2f}</td>
        </tr>
        """

    html_doc = f"""
    <html>
    <head><meta charset="utf-8"></head>
    <body>
        <h1>Invoice for {safe_customer_name}</h1>
        <table border="1" cellpadding="6">
            <tr><th>Description</th><th>Qty</th><th>Price</th><th>Total</th></tr>
            {items_html}
        </table>
        <h3>Notes</h3>
        <div>{safe_notes}</div>
    </body>
    </html>
    """

    # Lock down the renderer: no JS, no remote/local resource loading,
    # no plugins. wkhtmltopdf-specific options shown below.
    options = {
        "enable-local-file-access": None,  # explicitly OFF by omission — do not set this to True
        "disable-javascript": "",
        "disable-external-links": "",
        "disable-internal-links": "",
        "no-images": "",          # drop entirely if invoices legitimately need a logo image;
                                   # if so, embed trusted images as base64 rather than allowing
                                   # arbitrary user-supplied <img src="..."> URLs
        "quiet": "",
    }
    # Remove the key entirely rather than setting True, since its mere
    # presence with a value enables it in some pdfkit/wkhtmltopdf versions
    options.pop("enable-local-file-access", None)

    pdfkit.from_string(html_doc, output_path, options=options)
    return output_path