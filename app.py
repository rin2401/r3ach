from flask import Flask, render_template, request, jsonify, abort, Markup, session
from flask_socketio import SocketIO
import json
import random
from modules import search
from time import time
import threading

app = Flask(__name__)
app.config["JSON_AS_ASCII"] = False
socketio = SocketIO(app)

@app.route("/")
@app.route("/<int:qid>")
def hello(qid=None):
    with open("data/questions.json", "r", encoding="utf8") as f:
        data = json.load(f)
    if qid == None:
       qid = random.randint(0, len(data) - 1) 
    return render_template("index.html", data=data[qid], qid=qid, len=len(data))

@app.route("/lazi/filter")
@app.route("/lazi/filter/<int:qid>")
def lazi_filter(qid=None):    
    with open("data/lazi_expand_filter_answer.json", "r") as f:
        data = f.readlines()
    if qid == None:
       qid = random.randint(0, len(data) - 1) 
    
    q = json.loads(data[qid])
    correct = list(filter(lambda x: x["id"] == q["correct"], q["answer"]))[0]["text"]
    q["expand_answer"] = Markup(q["expand_answer"].replace("\n", "<br>").replace(correct, f"<b>{correct}</b>"))
    return render_template("lazi.html", data=q, qid=qid, len=len(data), path="/lazi/filter")

@app.route("/lazi")
@app.route("/lazi/<int:qid>")
def lazi(qid=None):    
    with open("data/lazi.json", "r") as f:
        data = f.readlines()
    if qid == None:
       qid = random.randint(0, len(data) - 1) 
    
    qdata = json.loads(data[qid])
    return render_template("lazi.html", data=qdata, qid=qid, len=len(data), path="/lazi")

@app.route("/questions/<int:qid>")
def questions(qid):
    with open("data/questions.json", "r", encoding="utf8") as f:
        data = json.load(f, )
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
    # answer = [search.remove_accent(a) for a in answer]
    username = "server"
    if "u" in data and data["u"]:
        username = data["u"]

    socketio.emit("new message", {
        "message": str(data),
        "username": username
    })

    begin_time = time()
    try:
        results = search.scrape_google(question, 5, "vi")
    except Exception as e:
        data = {
            "data": str(e)
        }
        print(data)
        return jsonify(data), 500

    google_time = time() - begin_time   



    for r in results:
        count = search.count_keys([r['title'], r['description']], answer)
        socketio.emit("new message", {
            "message": f"<b>{r['rank']}. {str(count)} {r['title']}</b> - {r['description']}",
            "username": username    
        })

    search_time = time()
    search_data = [{} for _ in results]
    threads = []
    for ii in range(len(results)): 
        process = threading.Thread(target=search.search_thread, args=[results[ii]["link"], search_data, ii, answer])
        process.start()
        threads.append(process)
    
    for process in threads:
        process.join()


    
    texts = [r["description"] for r in results if r["description"] != None]    
    for i, d in enumerate(search_data):
        texts.append(d["data"])
        results[i]["time"] = d["time"]
        results[i]["count"] = d["count"]

    
    count = {
        "key": answer,
        "num": [0] * len(answer)   
    }

    for r in results:
        socketio.emit("new message", {
            "message": f"<b>{r['rank']}. {r['time']}</b> - {r['count']}",
            "username": username    
        })

        for i, n in enumerate(r["count"]["num"]):
            count["num"][i] += n 


    # count = search.count_keys(texts, answer)
    # print("Count:", count)
    search_time = time() - search_time

    total_time = time() - begin_time
    data = {
        "data": results,
        "count": count,
        "time": {
            "total":total_time,
            "google": google_time,
            "search": search_time
        }
    }

    socketio.emit("new message", {
        "message": f"{data['count']} {data['time']}",
        "username": username
    })
    
    return jsonify(data)

@app.route("/socket")
def sessions():
    return render_template("socket.html")

def ack(methods=["GET", "POST"]):
    print("message was received!!!")


numUsers = 0;

@socketio.on("add user")
def add_user_event(username, methods=["GET", "POST"]):
    global numUsers
    session["username"] = username
    print('> Client connected:', username)
    numUsers += 1
    socketio.emit("login", {
      "numUsers": numUsers
    });
    socketio.emit("user joined", {
        "username": session["username"],
        "numUsers": numUsers
    }, broadcast=True, include_self=False);

@socketio.on("new message")
def handle_message_event(message, methods=["GET", "POST"]):
    print("> Message Event:", message, request.sid, session["username"])
    socketio.emit("new message", {
        "username": session["username"],
        "message": message
    }, broadcast=True, include_self=False);

@socketio.on("typing")
def handle_typing_event(methods=["GET", "POST"]):
    socketio.emit("typing", {
        "username": session["username"],
    }, broadcast=True, include_self=False);

@socketio.on("stop typing")
def handle_stop_typing_event(methods=["GET", "POST"]):
    socketio.emit("stop_typing", {
        "username": session["username"],
    }, broadcast=True, include_self=False);

@socketio.on("disconnect")
def handle_disconnect_event():
    global numUsers
    numUsers -= 1
    socketio.emit("user left", {
        "username": session["username"],
        "numUsers": numUsers
    }, broadcast=True);

@socketio.on("question")
def handle_question_event(json, methods=["GET", "POST"]):
    print("Question Event: " + str(json))
    message = str(json)
    socketio.emit("new message", {
        "username": session["username"],
        "message": message
    })

if __name__ == "__main__":
    socketio.run(app, host="0.0.0.0", port=5000, debug=True)