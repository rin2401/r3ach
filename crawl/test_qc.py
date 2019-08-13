import json

from kikiqc import KikiQC
model = KikiQC("trec_15")

with open("lazi_10001_20000.json", "r", encoding="utf8") as f:
	data = f.readlines()

questions = [json.loads(line)["question"] for line in data]

results = model.predict(questions)
results = [{"intent": r["parse"]["intent"]["name"], "confidence": float(r["parse"]["intent"]["confidence"]), "query": r["query"]} for r in results]

with open("trec_15_lazi_1_10000.json", "w") as f:
	for r in results:
		f.write(json.dumps(r, ensure_ascii=False) + "\n")