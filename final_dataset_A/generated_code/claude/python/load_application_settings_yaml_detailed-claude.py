import yaml
import os


class ConfigLoadError(Exception):
    pass


class AppConfig:
    def __init__(self):
        self._settings = {}

    def load(self, yaml_file):
        if not os.path.exists(yaml_file):
            raise ConfigLoadError(f"Config file not found: {yaml_file}")

        try:
            with open(yaml_file, 'r') as f:
                data = yaml.safe_load(f)
        except yaml.YAMLError as e:
            raise ConfigLoadError(f"Invalid YAML syntax: {e}")

        if data is None:
            raise ConfigLoadError("Config file is empty")

        self._settings = data
        return self

    def get(self, key, default=None):
        keys = key.split('.')
        value = self._settings
        for k in keys:
            if isinstance(value, dict) and k in value:
                value = value[k]
            else:
                return default
        return value

    def display(self):
        print(yaml.dump(self._settings, default_flow_style=False))


if __name__ == '__main__':
    config = AppConfig()
    try:
        config.load('config.yaml')
        config.display()
        print("Database host:", config.get('database.host', 'localhost'))
    except ConfigLoadError as e:
        print(f"Failed to load configuration: {e}")
