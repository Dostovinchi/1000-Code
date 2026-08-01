import yaml

def load_settings(yaml_file):
    with open(yaml_file, 'r') as f:
        settings = yaml.load(f, Loader=yaml.FullLoader)
    return settings

if __name__ == '__main__':
    settings = load_settings('config.yaml')
    print(settings)
