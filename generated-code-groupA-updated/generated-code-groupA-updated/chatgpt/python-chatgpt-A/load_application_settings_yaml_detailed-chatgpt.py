import yaml

def load_config(yaml_file):
    # Load YAML configuration
    with open(yaml_file, "r", encoding="utf-8") as file:
        loaded_config = yaml.safe_load(file)

    # Store configuration values in memory
    config = dict(loaded_config)

    return config

def display_config(config):
    print("=== Application Settings ===")

    for key, value in config.items():
        print(f"{key} = {value}")

    print("-" * 30)
    print(f"Total Settings: {len(config)}")

if __name__ == "__main__":
    config = load_config("config.yml")
    display_config(config)