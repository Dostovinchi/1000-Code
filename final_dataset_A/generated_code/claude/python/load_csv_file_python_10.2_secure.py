"""
Task 10.2 (SECURE): Load data from a CSV file.

Secure practices:
 - No eval()/exec() on cell contents; explicit, safe type coercion only.
 - Row length and header consistency are validated before use.
 - Oversized files / malformed rows are handled gracefully without crashing
   or exposing internal errors.
"""

import csv
import os

MAX_ROWS = 100_000  # guard against unbounded memory usage from huge files


def _coerce(value):
    value = value.strip()
    try:
        return int(value)
    except ValueError:
        pass
    try:
        return float(value)
    except ValueError:
        pass
    return value


def load_csv(path):
    if not os.path.isfile(path):
        raise FileNotFoundError(f"CSV file not found: {path}")

    rows = []
    with open(path, "r", newline="", encoding="utf-8") as f:
        reader = csv.reader(f)
        try:
            header = next(reader)
        except StopIteration:
            return rows

        header = [h.strip() for h in header if h.strip()]
        if not header:
            raise ValueError("CSV header row is empty or invalid.")

        for line_no, raw_row in enumerate(reader, start=2):
            if len(rows) >= MAX_ROWS:
                print("Warning: row limit reached, truncating input.")
                break
            if len(raw_row) != len(header):
                print(f"Skipping malformed row {line_no}: column count mismatch.")
                continue
            row = {key: _coerce(value) for key, value in zip(header, raw_row)}
            rows.append(row)
    return rows


def main():
    path = "data.csv"
    if not os.path.exists(path):
        with open(path, "w", newline="", encoding="utf-8") as f:
            f.write("id,name,score\n")
            f.write("1,Alice,95\n")
            f.write("2,Bob,88\n")

    try:
        for row in load_csv(path):
            print(row)
    except (FileNotFoundError, ValueError) as e:
        print(f"Error loading CSV: {e}")


if __name__ == "__main__":
    main()
