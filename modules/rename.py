import os, json
import glob

# DIR = "image/ss2"
DIR = "image/01082019"
# files = os.listdir("image/ss2")

# print(files)

# for i, f in enumerate(files):
# 	os.rename(f"image/ss2/{f}", f"image/ss2/{i}.jpg")

def txt2json(infile, outfile):
	data = []
	with open(os.path.join(DIR, infile), "r") as f:
		lines = f.readlines()
		lines = [l.replace("\n", "").replace("\ufeff", "") for l in lines]
		print(len(lines))


	for i in range(len(lines)//5):
		print(i)
		qdata = lines[i*5:i*5+5]
		print(qdata)
		q = {
			"question": qdata[0].strip(),
			"answer": qdata[1:4],
			"true": int(qdata[4])
		}
		data.append(q)

	print(json.dumps(data, indent=4, ensure_ascii=False))
	with open(os.path.join(DIR, outfile), "w") as f:
		f.write(json.dumps(data, indent=4, ensure_ascii=False))
