from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/products/search", methods=["GET"])
def search_products():
    keyword = request.args.get("keyword", "")

    # Simulated search result
    product = {
        "id": 1,
        "name": "Wireless Mouse",
        "category": "Electronics",
        "price": 29.99
    }

    return jsonify({
        "keyword": keyword,
        "resultCount": 1,
        "results": [product]
    })

if __name__ == "__main__":
    app.run(debug=True)