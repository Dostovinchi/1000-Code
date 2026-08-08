from flask import Flask, request, jsonify
import datetime
import logging

app = Flask(__name__)
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

processed_events = []


def parse_webhook_payload(raw_data):
    if raw_data is None:
        raise ValueError("Empty payload")
    if 'event_type' not in raw_data:
        raise ValueError("Missing event_type field")
    return raw_data


def process_event(event):
    event_type = event.get('event_type')
    handlers = {
        "order.created": handle_order_created,
        "payment.completed": handle_payment_completed,
    }
    handler = handlers.get(event_type, handle_unknown_event)
    return handler(event)


def handle_order_created(event):
    return {"action": "order_processed", "order_id": event.get("order_id")}


def handle_payment_completed(event):
    return {"action": "payment_recorded", "transaction_id": event.get("transaction_id")}


def handle_unknown_event(event):
    return {"action": "logged_unknown", "event_type": event.get("event_type")}


@app.route('/api/webhooks', methods=['POST'])
def webhook():
    raw_data = request.get_json(silent=True)

    try:
        event = parse_webhook_payload(raw_data)
    except ValueError as e:
        return jsonify({"status": "error", "message": str(e)}), 400

    result = process_event(event)

    record = {
        "event_type": event.get("event_type"),
        "received_at": datetime.datetime.utcnow().isoformat(),
        "result": result
    }
    processed_events.append(record)
    logger.info(f"Processed webhook event: {record}")

    return jsonify({"status": "success", "result": result})


if __name__ == '__main__':
    app.run(debug=True)
