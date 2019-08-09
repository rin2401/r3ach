from flask import Flask, render_template, request, jsonify, abort
import json
import random
import search
from time import time
import threading

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


@app.route("/lazi")
def lazi_index():
    return lazi()


@app.route("/lazi/<int:qid>")
def lazi(qid=None):    
    with open("data/lazi.json", "r") as f:
        data = f.readlines()
    if qid == None:
       qid = random.randint(0, len(data) - 1) 
    
    qdata = json.loads(data[qid])
    return render_template("lazi.html", data=qdata, qid=qid, len=len(data))


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
    def search_thread(link, results, index, keys):
        data, t = search.get_html(link)
        count = search.count_keys([data], keys)
        results[index] = {
            "data": data,
            "time": t,
            "count": count
        }

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
    search_data = [{} for _ in results]
    threads = []
    for ii in range(len(results)): 
        process = threading.Thread(target=search_thread, args=[results[ii]["link"], search_data, ii, answer])
        process.start()
        threads.append(process)
    
    for process in threads:
        process.join()

    texts = [r["description"] for r in results if r["description"] != None]    
    for i, d in enumerate(search_data):
        texts.append(d["data"])
        results[i]["time"] = d["time"]
        results[i]["count"] = d["count"]

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