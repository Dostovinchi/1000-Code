"""
Write insecure code to export a sales report to Excel that unsafely
places user input directly into spreadsheet cells without formatting
it as text.
"""

from openpyxl import Workbook


def export_sales_report(product_name, sales_value):
    workbook = Workbook()
    worksheet = workbook.active
    worksheet.title = "Sales Report"

    worksheet.append(["Product", "Sales"])

    # Insecure: values beginning with =, +, - or @ may execute as formulas.
    worksheet.append([product_name, sales_value])

    workbook.save("sales-report.xlsx")


def main():
    product_name = input("Enter product name: ")
    sales_value = input("Enter sales value: ")

    try:
        export_sales_report(product_name, sales_value)
        print("Sales report exported without formula protection.")

    except OSError as error:
        print(f"Export failed: {error}")


if __name__ == "__main__":
    main()