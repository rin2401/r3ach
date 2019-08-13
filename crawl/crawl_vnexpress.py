from bs4 import BeautifulSoup
import requests
import re
import json
import time

def get_vnexpress_quiz(link):
    res = requests.get(link)
    soup = BeautifulSoup(res.text, "html.parser")
    quiz = []
    sec = soup.find("section", "featured")
    is_home = False
    if sec:
        quiz += [a.find("a") for a in sec.find_all(attrs={"class": "title_news"})]
        is_home = True
    sec = soup.find("section", "sidebar_1")
    if sec:
        quiz += [a.find("a") for a in sec.find_all(attrs={"class": "title_news"})]
    
    quiz = [{"title": a["title"], "link": a["href"]} for a in quiz]
    return quiz, is_home


def get_all_vnexpress_quiz():
    for i in range(1, 50):
        quiz, is_home = get_vnexpress_quiz(f"https://vnexpress.net/giao-duc/trac-nghiem-p{i}")
        if is_home and i != 1:
            break    
        print(i, len(quiz))

        with open("vnexpress_links.json", "a") as f:
            for q in quiz:
                f.write(json.dumps(q, ensure_ascii=False) + "\n")


def get_question(link, title):
    data = []
    while True:
        res = requests.get(link)
        soup = BeautifulSoup(res.text, "html.parser")
        art = soup.find("article", "content_detail")
        paras = art.findChildren("p", recursive=False)
        question = art.find("strong")

        if question != None:
            qpara = question.find_parent("p")
            qid = paras.index(qpara)
            expand = paras[:qid]
            after = paras[qid+1:]
            question = qpara.get_text().strip()
            if ":" in question:
                question = " ".join(question.split(":")[1:]).strip()
            apara = []
            for p in after:
                if p.find("a"):
                    apara.append(p.find("a"))
                elif not p.find("strong"):
                    question = question + "\n" + p.get_text("\n").strip()
            
            print(question)
        else:
            expand = paras

        expand = [x.get_text().strip().replace("\xa0", " ").replace("  ", " ") for x in expand]
        
        # print(expand)
        if ">> Quay lại" in expand or ">>Quay lại" in expand :
            data[-1]["expand"] = "\n".join(expand[:-1])
            break
        elif len(data) > 0:
            data[-1]["expand"] = "\n".join(expand)

        answer = [a["title"][2:].strip() for a in apara]
        
        ahref = [a["href"].strip() for a in apara]
        acount = [ahref.count(a) for a in ahref]
        print("Count:", acount)
        true_id = acount.index(1)
        link = ahref[0]
        print("Link:", link)
        data.append({
            "title": title,
            "question": question,
            "answer": answer,
            "true": true_id,
            "expand": ""
        }) 
    
    return data

def get_all_question(start_id):
    with open("vnexpress_links.json") as f:
        links = f.readlines()
    # for i in range(3, 4):
    for i in range(start_id, len(links)):
        print(i)
        link = json.loads(links[i])
        data = get_question(link["link"], link["title"])
        with open("vnexpress.json", "a") as f:
            for q in data:
                f.write(json.dumps(q, ensure_ascii=False) + "\n")

def test(id):
    with open("vnexpress_links.json") as f:
        links = f.readlines()

    link = json.loads(links[id])
    data = get_question(link["link"], link["title"])

if __name__ == "__main__":
    get_all_question(34)

    # test(34)