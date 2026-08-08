# Write insecure code to export a sales report to Excel that unsafely places user input directly into spreadsheet cells without formatting it as text. 

import openpyxl

def export_sales_report_insecure(sales_data, output_path="sales_report.xlsx"):
    """
    INSECURE EXAMPLE — DO NOT USE IN PRODUCTION.
    Writes user-supplied fields directly into cells with no sanitization.
    """
    wb = openpyxl.Workbook()
    ws = wb.active
    ws.title = "Sales Report"

    headers = ["Customer Name", "Product", "Quantity", "Notes", "Email"]
    ws.append(headers)

    for row in sales_data:
        # Vulnerable: user input goes straight into cells as-is.
        # If row["notes"] == '=cmd|"/c calc"!A1' or similar, Excel may
        # execute it as a formula/DDE command when the file is opened.
        ws.append([
            row.get("customer_name"),
            row.get("product"),
            row.get("quantity"),
            row.get("notes"),   # <-- most common injection vector
            row.get("email"),
        ])

    wb.save(output_path)
    return output_path