import glob
import os
import time 
from tqdm import tqdm

DIR = "image/ss2"

# files = []
# for filename in glob.glob(f'{DIR}/*.jpg'):
#         files.append(filename)

# print("Num :", len(files))

# for image in tqdm(sorted(files)):
# 	t1 = time.time()
# 	print("uploading " + image)
# 	command = "gdput.py -t ocr  " + image + f" -f 15rhh90GMG663DhmXAufK1PH9Cx5Lk4o2 > {DIR}/result.log"
# 	print("running " + command)
# 	t2 = time.time()
# 	os.system(command)
# 	print("Put:", time.time() - t2)
	
# 	resultfile = open(f"{DIR}/result.log","r").readlines()
	
# 	for line in resultfile:
# 		if "id:" in line:
# 			fileid = line.split(":")[1].strip()
# 			filename = image.split(".")[0] + ".txt"
# 			get_command = "gdget.py -f txt -s " + filename + " " + fileid
# 			print("running "+ get_command)
# 			t3 = time.time()
# 			os.system(get_command)
# 			print("Get:", time.time() - t3)
# 	print("Time:", time.time() - t1)


# print("Merging all text files into results.txt")	

files = glob.glob(f'{DIR}/*.txt')
files = sorted(files, key=lambda x: int(x.split("/")[-1].split(".")[0]))
print(files)

with open(f'{DIR}/results.txt', 'w' ) as result:
    for textfile in files:
        for line in open( textfile, 'r' ):
            result.write( line )

print("Done")
