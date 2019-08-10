import glob
import os
import time 
import json
from tqdm import tqdm

def detect(image):
    t1 = time.time()
    print("uploading " + image)
    command = "gdput.py -t ocr  " + image + f" -f 15rhh90GMG663DhmXAufK1PH9Cx5Lk4o2 > {DIR}/result.log"
    print("running " + command)
    t2 = time.time()
    os.system(command)
    print("Put:", time.time() - t2)
    
    resultfile = open(f"{DIR}/result.log","r").readlines()
    
    for line in resultfile:
        if "id:" in line:
            fileid = line.split(":")[1].strip()
            filename = image.split(".")[0] + ".txt"
            get_command = "gdget.py -f txt -s " + filename + " " + fileid
            print("running "+ get_command)
            t3 = time.time()
            os.system(get_command)
            print("Get:", time.time() - t3)
    print("Time:", time.time() - t1)

def txt2json(infile, outfile):
    with open(infile, "r") as f:
        lines = f.readlines()
        lines = [l.replace("\n", "").replace("\ufeff", "") for l in lines]
        print(len(lines))

    create_json(lines, outfile)


def create_json(lines, outfile):
    data = []
    for i in range(len(lines)//5):
        qdata = lines[i*5:i*5+5]
        q = {
            "question": qdata[0].strip(),
            "answer": qdata[1:4],
            "true": int(qdata[4])
        }
        data.append(q)

    print(json.dumps(data, indent=2, ensure_ascii=False))
    with open(outfile, "w") as f:
        f.write(json.dumps(data, indent=2, ensure_ascii=False))

def merge_txt(path):
    files = glob.glob(f"{path}/*.txt")
    files = sorted(files, key=lambda x: int(x.split("/")[-1].split(".")[0]))
    print(files)
    lines = []
    for f in files:
        for line in open(f, "r"):
            if line.strip() != "":
                line = line.replace("\ufeff", "").replace("\n", "")
                lines.append(line)
    with open(f"{path}/result", "w") as f:
        f.write("\n".join(lines))

    return lines

if __name__ == "__main__":
    DIR = "image/confetti/2"
    files = []

    for filename in glob.glob(f"{DIR}/*.jpg"):
            files.append(filename)

    for filename in glob.glob(f"{DIR}/*.png"):
            files.append(filename)

    print("Num :", len(files))

    # for image in tqdm(sorted(files)):
    #     detect(image)

    # merge_txt(DIR)

    txt2json(os.path.join(DIR, "result"), os.path.join(DIR, "result.json"))


    # create_json(merge_txt(DIR), os.path.join(DIR, "result.json"))


    # detect(os.path.join(DIR, "7.png"))