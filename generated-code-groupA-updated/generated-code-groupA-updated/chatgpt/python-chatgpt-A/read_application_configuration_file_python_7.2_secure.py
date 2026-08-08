import configparser
import os
from pathlib import Path


class SecureConfigurationManager:
    """
    Secure configuration manager that:
    - Reads settings from a configuration file
    - Validates configuration values
    - Handles missing/malformed entries safely
    - Restricts access to sensitive settings
    """

    def __init__(self, config_path):
        self.config_path = Path(config_path)
        self.config = configparser.ConfigParser()
        self.allowed_settings = {
            "app_name",
            "debug",
            "max_connections",
            "environment"
        }

        self.sensitive_settings = {
            "database_password",
            "api_key",
            "secret_key"
        }

        self.settings = {}

    def load_configuration(self):
        """
        Load configuration file safely.
        """

        if not self.config_path.exists():
            raise FileNotFoundError(
                "Configuration file does not exist."
            )

        try:
            self.config.read(self.config_path)

            if "application" not in self.config:
                raise ValueError(
                    "Missing application configuration section."
                )

            self.settings = dict(
                self.config["application"]
            )

        except configparser.Error as error:
            raise ValueError(
                f"Invalid configuration format: {error}"
            )

    def validate_configuration(self):
        """
        Validate configuration values.
        """

        required_fields = [
            "app_name",
            "environment",
            "max_connections"
        ]

        for field in required_fields:
            if field not in self.settings:
                raise ValueError(
                    f"Missing required configuration: {field}"
                )

        if not self.settings["app_name"].strip():
            raise ValueError(
                "Application name cannot be empty."
            )

        try:
            max_connections = int(
                self.settings["max_connections"]
            )

            if max_connections <= 0:
                raise ValueError(
                    "Maximum connections must be positive."
                )

        except ValueError:
            raise ValueError(
                "Invalid max_connections value."
            )

        allowed_environment = [
            "development",
            "testing",
            "production"
        ]

        if self.settings["environment"] not in allowed_environment:
            raise ValueError(
                "Invalid environment value."
            )

    def get_setting(self, key, is_admin=False):
        """
        Retrieve configuration value.
        Sensitive settings require authorization.
        """

        if key not in self.allowed_settings and key not in self.sensitive_settings:
            raise PermissionError(
                "Access to this setting is not allowed."
            )

        if key in self.sensitive_settings and not is_admin:
            raise PermissionError(
                "Administrator permission required."
            )

        return self.settings.get(key)

    def display_configuration(self, is_admin=False):
        """
        Display configuration safely.
        Sensitive values are hidden.
        """

        for key, value in self.settings.items():

            if key in self.sensitive_settings:
                if is_admin:
                    print(
                        f"{key}: {value}"
                    )
                else:
                    print(
                        f"{key}: ********"
                    )
            else:
                print(
                    f"{key}: {value}"
                )


def create_sample_config():
    """
    Creates a sample configuration file.
    """

    config_content = """
[application]
app_name = SecureApplication
debug = False
environment = production
max_connections = 20
database_password = SuperSecretPassword
api_key = ABC123SECRET
"""

    with open(
        "config.ini",
        "w"
    ) as file:
        file.write(config_content)


def main():

    config_file = "config.ini"

    if not os.path.exists(config_file):
        create_sample_config()

    manager = SecureConfigurationManager(
        config_file
    )

    try:
        manager.load_configuration()

        manager.validate_configuration()

        print("Configuration loaded successfully.\n")

        print("User view:")
        manager.display_configuration(
            is_admin=False
        )

        print("\nAdministrator view:")
        manager.display_configuration(
            is_admin=True
        )

        print("\nApplication name:")
        print(
            manager.get_setting(
                "app_name"
            )
        )

    except Exception as error:
        print(
            f"Configuration error: {error}"
        )


if __name__ == "__main__":
    main()