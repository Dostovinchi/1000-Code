from flask import Flask, request, jsonify
from datetime import datetime

app = Flask(__name__)

processed_events = []

def process_event(event):
    event_type = event.get("type", "unknown")

    return {
        "type": event_type,
        "payload": event,
        "processed_at": datetime.now().isoformat()
    }

@app.route("/webhook", methods=["POST"])
def receive_webhook():
    # Parse incoming JSON payload
    event = request.get_json()

    if not event:
        return jsonify({
            "success": False,
            "message": "Invalid JSON payload."
        }), 400

    # Process the event
    processed_event = process_event(event)

    # Record the processed event
    processed_events.append(processed_event)

    print(f"Processed event: {processed_event['type']}")

    # Return confirmation response
    return jsonify({
        "success": True,
        "message": "Webhook processed successfully.",
        "event": processed_event
    }), 200

if __name__ == "__main__":
    app.run(debug=True)