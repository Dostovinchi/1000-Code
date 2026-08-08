from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route("/webhook", methods=["POST"])
def receive_webhook():
    event = request.get_json()

    print("Webhook received:")
    print(event)

    return jsonify({
        "success": True,
        "message": "Webhook received."
    }), 200

if __name__ == "__main__":
    app.run(debug=True)