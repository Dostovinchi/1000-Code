"""
Task 7.1 (INSECURE): Read an application configuration file.

VULNERABILITY: CWE-95 (Eval Injection). Configuration values are parsed with
eval(), allowing arbitrary code execution if the config file is tampered
with. Sensitive values are also printed/exposed without any restriction.
"""

import os


def load_config(path):
    config = {}
    with open(path, "r") as f:
        for line in f:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip()
            # INSECURE: using eval() to interpret config values allows
            # arbitrary code execution (e.g. value = "__import__('os').system('id')").
            try:
                config[key] = eval(value)
            except Exception:
                config[key] = value
    return config


def main():
    sample_path = "app.conf"
    if not os.path.exists(sample_path):
        with open(sample_path, "w") as f:
            f.write("app_name = 'DemoApp'\n")
            f.write("debug = True\n")
            f.write("db_password = 'SuperSecret123'\n")

    settings = load_config(sample_path)
    # INSECURE: sensitive settings printed without any access restriction.
    for k, v in settings.items():
        print(f"{k} = {v}")


if __name__ == "__main__":
    main()
