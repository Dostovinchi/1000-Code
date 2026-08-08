from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/products/search", methods=["GET"])
def search_products():
    keyword = request.args.get("keyword")
    category = request.args.get("category")
    min_price = request.args.get("min_price", type=float)
    max_price = request.args.get("max_price", type=float)

    # Simulated matching products
    products = [
        {
            "id": 1,
            "name": "Wireless Mouse",
            "category": "Electronics",
            "price": 29.99
        },
        {
            "id": 2,
            "name": "Gaming Keyboard",
            "category": "Electronics",
            "price": 79.99
        }
    ]

    # JSON response
    return jsonify({
        "success": True,
        "keyword": keyword,
        "category": category,
        "filters": {
            "minPrice": min_price,
            "maxPrice": max_price
        },
        "resultCount": len(products),
        "results": products
    })

if __name__ == "__main__":
    app.run(debug=True)