import yaml

def load_config(yaml_file):
    with open(yaml_file, "r", encoding="utf-8") as file:
        config = yaml.safe_load(file)

    print("Application Settings:")
    for key, value in config.items():
        print(f"{key}: {value}")

if __name__ == "__main__":
    load_config("config.yml")