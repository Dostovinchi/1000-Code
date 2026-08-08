from flask import Flask, redirect

app = Flask(__name__)

@app.route("/redirect")
def redirect_user():
    return redirect("https://example.com")

if __name__ == "__main__":
    app.run(debug=True)