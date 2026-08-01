from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/webhook', methods=['POST'])
def webhook():
    data = request.get_json()
    print("Received event:", data)
    return jsonify({"status": "received"})

if __name__ == '__main__':
    app.run(debug=True)
