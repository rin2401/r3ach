import json

# from kikiqc import KikiQC
# model = KikiQC("trec_15")

# with open("lazi_10001_20000.json", "r", encoding="utf8") as f:
# 	data = f.readlines()

# questions = [json.loads(line)["question"] for line in data]

# results = model.predict(questions)
# results = [{"intent": r["parse"]["intent"]["name"], "confidence": float(r["parse"]["intent"]["confidence"]), "query": r["query"]} for r in results]

# with open("trec_15_lazi_1_10000.json", "w") as f:
# 	for r in results:
# 		f.write(json.dumps(r, ensure_ascii=False) + "\n")

with open("lazi_expand.json", "r", encoding="utf8") as f:
	data = [json.loads(line) for line in f]

data_expand = []
question_expand = []
for q in data:
	correct = list(filter(lambda x: x["id"] == q["correct"], q["answer"]))[0]["text"]
	if q["expand_answer"] != "" and "..." not in q["question"] and correct.lower() in q["expand_answer"].lower():
		if q["question"] not in question_expand:
			data_expand.append(q)
			question_expand.append(q["question"])
		else:
			print(q["id"], q["question"])

print(len(data))	
print(len(data_expand))

# with open("lazi_expand_filter_answer.json", "w") as f:
# 	for q in data_expand:
# 		f.write(json.dumps(q, ensure_ascii=False) + "\n")

# with open("lazi_question.txt", "w") as f:
# 	for q in data_expand:
# 		f.write(q["question"]+"\n")