from bs4 import BeautifulSoup
import requests
import re
import json
import time


res = requests.get("https://news.zing.vn/series/trac-nghiem-quizz.html")

soup = BeautifulSoup(res.text)

print(soup.find_all("a"))

# with open("zing.html", "w") as f:
#     f.write(res.text)