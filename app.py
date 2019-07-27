from flask import Flask, render_template, request, jsonify
import search
import json
from time import time
app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False

@app.route('/')
def index():
    return hello(0)

@app.route('/<int:qid>')
def hello(qid=0):
    with open("data/questions.json", "r") as f:
        data = json.load(f)

    return render_template("index.html", data=data[qid], qid=qid)

@app.route("/search", methods=["GET"])
def search_api():
    question = request.args.get('q')
    results = search.scrape_google(question, 10, "vi")
    return jsonify(results)

@app.route("/count", methods=["POST"])
def count_api():
    data = request.json
    question = data["q"]
    answer = data["a"]
    t = time()
    results = search.scrape_google(question, 10, "vi")
    count = search.count_keys([r["description"] for r in results], answer)
    t = time() - t
    data = {
        "data": results,
        "count": count,
        "time": t
    }
    return jsonify(data)

if __name__ == '__main__':
    app.debug = True
    app.run(host='0.0.0.0', port=5000)