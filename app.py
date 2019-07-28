from flask import Flask, render_template, request, jsonify, abort
import json
import random
import search
from time import time

app = Flask(__name__)
app.config["JSON_AS_ASCII"] = False

@app.route("/")
def index():
    return hello()

@app.route("/<int:qid>")
def hello(qid=None):
    with open("data/questions.json", "r") as f:
        data = json.load(f)
    if qid == None:
       qid = random.randint(0, len(data) - 1) 
    return render_template("index.html", data=data[qid], qid=qid, len=len(data))

@app.route("/questions/<int:qid>")
def questions(qid):
    with open("data/questions.json", "r") as f:
        data = json.load(f)
    return jsonify(data[qid])

@app.route("/search", methods=["GET"])
def search_api():
    question = request.args.get("q")
    results = search.scrape_google(question, 10, "vi")
    return jsonify(results)

@app.route("/count", methods=["POST"])
def count_api():
    data = request.json
    question = data["q"]
    answer = data["a"]
    begin_time = time()
    try:
        results = search.scrape_google(question, 10, "vi")
    except Exception as e:
        data = {
            "data": str(e)
        }
        print(data)
        return jsonify(data), 500

    google_time = time() - begin_time
    # print(results)
    htmls = []
    for i in range(5): 
        html, lt = search.get_html(results[i]["link"])
        htmls.append(html)
        results[i]["time"] = lt
    texts = [r["description"] for r in results if r["description"] != None]
    texts += htmls
    count = search.count_keys(texts, answer)
    print("Count:", count)
    total_time = time() - begin_time
    data = {
        "data": results,
        "count": count,
        "time": total_time,
        "google_time": google_time
    }
    return jsonify(data)

if __name__ == "__main__":
    app.debug = True
    app.run(host="0.0.0.0", port=5000)