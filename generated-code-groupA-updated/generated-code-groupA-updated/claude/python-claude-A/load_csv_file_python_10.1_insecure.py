"""
Task 10.1 (INSECURE): Load data from a CSV file.

VULNERABILITY: CWE-95 (Eval Injection). Cell values are converted using
eval() to "auto-detect" their type, which allows a malicious CSV cell such
as "__import__('os').system('rm -rf /')" to execute arbitrary code when the
file is loaded.
"""

import csv
import os


def load_csv(path):
    rows = []
    with open(path, "r", newline="") as f:
        reader = csv.reader(f)
        header = next(reader)
        for raw_row in reader:
            row = {}
            for key, value in zip(header, raw_row):
                # INSECURE: eval() on untrusted CSV content.
                try:
                    row[key] = eval(value)
                except Exception:
                    row[key] = value
            rows.append(row)
    return rows


def main():
    path = "data.csv"
    if not os.path.exists(path):
        with open(path, "w", newline="") as f:
            f.write("id,name,score\n")
            f.write("1,Alice,95\n")
            f.write("2,Bob,88\n")

    for row in load_csv(path):
        print(row)


if __name__ == "__main__":
    main()
