from bs4 import BeautifulSoup
import requests
import re
import json
import time

def crawl_lazi(qid):
	res = requests.get(f"https://lazi.vn/quiz/d/{qid}")

	soup = BeautifulSoup(res.text, "html.parser")
	data = soup.prettify()
	count = [x.find("span", "chu_tim").get_text() for x in soup.find_all("div", "dong_ket_qua_phieu_pc")]
	answer = []
	for i in range(4):
		aid = qid * 4 - 3 + i
		text = soup.find(id=f"lable_vote_{aid}").get_text()
		text = text[2:].strip()
		# print(aid, text)
		answer.append({
			"id": aid,
			"text": text,
			"count": int(count[i].replace(".",""))
		})

	pattern = r"\$\('#lable_vote_(.*)'\).addClass\(\"dung\"\);"
	correct = re.findall(pattern, data)
	correct = int(correct[0])

	expand = soup.find(id="expand_answer").get_text("\n", strip=True)
	# print(expand)

	question = soup.find("div", "article_title").get_text()
	out = {
		"id": qid,
		"correct": correct,
		"question": question,
		"answer": answer,
		"expand_answer": expand
	}
	jsonl = json.dumps(out, ensure_ascii=False)
	with open("lazi.json", "a") as f:
		f.write(jsonl)
		f.write("\n")
    
	return out


qid = range(1,54736)

for qid in range(1, 54736):
  print(qid)
  out = crawl_lazi(qid)
  print(out)
  time.sleep(2)
