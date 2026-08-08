import configparser
import os
import json
import time


class ApplicationConfiguration:
    def __init__(self, file_path):
        self.file_path = file_path
        self.configuration = {}
        self.loaded = False

    def read_file(self):
        parser = configparser.ConfigParser()

        try:
            parser.read(self.file_path)

            for section in parser.sections():
                self.configuration[section] = {}

                for key, value in parser.items(section):
                    self.configuration[section][key] = value

            self.loaded = True

        except Exception as error:
            print("Unable to read configuration file:", error)

    def get_setting(self, section, key):
        if section in self.configuration:
            if key in self.configuration[section]:
                return self.configuration[section][key]

        return None

    def get_section(self, section):
        return self.configuration.get(section, {})

    def get_all_settings(self):
        return self.configuration

    def display_configuration(self):
        if not self.loaded:
            print("Configuration was not loaded.")
            return

        print("\nApplication Configuration")
        print("------------------------")

        for section, values in self.configuration.items():
            print(f"[{section}]")

            for key, value in values.items():
                print(f"{key} = {value}")

            print()


class Application:
    def __init__(self, config_file):
        self.config_manager = ApplicationConfiguration(config_file)
        self.application_name = "Configuration Reader Application"
        self.running = False

    def initialize(self):
        print("Initializing application...")
        time.sleep(1)

        self.config_manager.read_file()

        if self.config_manager.loaded:
            print("Configuration loaded successfully.")
        else:
            print("Configuration loading failed.")

    def start(self):
        self.running = True

        print(f"\nStarting {self.application_name}")

        while self.running:
            self.show_menu()

            choice = input("Select option: ")

            if choice == "1":
                self.display_configuration()

            elif choice == "2":
                self.search_setting()

            elif choice == "3":
                self.export_configuration()

            elif choice == "4":
                self.stop()

            else:
                print("Invalid option.")

    def show_menu(self):
        print("\nMenu")
        print("----")
        print("1. Display configuration")
        print("2. Search configuration setting")
        print("3. Export configuration")
        print("4. Exit")

    def display_configuration(self):
        self.config_manager.display_configuration()

    def search_setting(self):
        section = input("Enter section name: ")
        key = input("Enter setting name: ")

        value = self.config_manager.get_setting(section, key)

        if value is not None:
            print("Setting value:", value)
        else:
            print("Setting not found.")

    def export_configuration(self):
        data = self.config_manager.get_all_settings()

        with open("exported_configuration.json", "w") as file:
            json.dump(data, file, indent=4)

        print("Configuration exported.")

    def stop(self):
        self.running = False
        print("Application stopped.")


def create_sample_configuration():
    if not os.path.exists("application.conf"):

        with open("application.conf", "w") as file:
            file.write("[database]\n")
            file.write("host=localhost\n")
            file.write("port=3306\n")
            file.write("username=admin\n")
            file.write("password=password123\n")

            file.write("\n[application]\n")
            file.write("name=DemoApp\n")
            file.write("version=1.0\n")
            file.write("environment=development\n")


def main():
    create_sample_configuration()

    config_file = "application.conf"

    application = Application(config_file)

    application.initialize()

    application.start()


if __name__ == "__main__":
    main()