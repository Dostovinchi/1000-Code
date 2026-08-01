import json

def parse_catalog(json_file):
    with open(json_file, 'r') as f:
        data = json.load(f)

    products = data['products']
    for p in products:
        print(p['name'], p['price'], p['category'])
    return products

if __name__ == '__main__':
    parse_catalog('catalog.json')
