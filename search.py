import requests
from bs4 import BeautifulSoup
from time import time
import json
 
USER_AGENT = {"User-Agent":"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36"}
BAD_LINK = [
    "facebook",
    ".pdf",
    "download",
    ".edu",
    ".gov"
]
def fetch_results(search_term, number_results, language_code):
    assert isinstance(search_term, str), "Search term must be a string"
    assert isinstance(number_results, int), "Number of results must be an integer"
    escaped_search_term = search_term.replace(" ", "+")
 
    google_url = "https://www.google.com/search?q={}&num={}&hl={}".format(escaped_search_term, number_results, language_code)
    response = requests.get(google_url, headers=USER_AGENT)
    response.raise_for_status()
 
    return search_term, response.text
 
def parse_results(html, keyword):
    soup = BeautifulSoup(html, "html.parser")
 
    found_results = []
    rank = 1
    result_block = soup.find_all("div", attrs={"class": "g"})
    for result in result_block:
 
        link = result.find("a", href=True)
        title = result.find("h3")
        description = result.find("span", attrs={"class": "st"})
        if link and title:
            link = link["href"]
            title = title.get_text()
            if description:
                description = description.get_text().replace("\xa0...", "")
            if link != "#" and "http" in link:
                found_results.append({"keyword": keyword, "rank": rank, "title": title, "description": description, "link": link})
                rank += 1
    return found_results

def scrape_google(search_term, number_results, language_code):
    try:
        keyword, html = fetch_results(search_term, number_results, language_code)
        results = parse_results(html, keyword)
        return results
    except AssertionError:
        raise Exception("Incorrect arguments parsed to function")
    except requests.HTTPError:
        raise Exception("You appear to have been blocked by Google")
    except requests.RequestException:
        raise Exception("Appears to be an issue with your connection")

def count_keys(texts, keys):
    text = " ".join(texts).lower()
    return {k: text.count(k.lower()) for k in keys}

def get_html(link):
    t = time()
    try: 
        for b in BAD_LINK:
            if b in link:
                raise Exception("Bad link.")
        response = requests.get(link, headers=USER_AGENT, timeout=1, allow_redirects=False)
        get_time = time() - t
        soup = BeautifulSoup(response.text, "html.parser")
        soup.prettify()
        texts = [p.get_text() for p in soup.find_all("p")]
        return " ".join(texts), {"get": get_time, "soup": time() - get_time - t}
    except Exception as e:
        return "", {"data": str(e), "time": time() - t}

if __name__ == "__main__":

    keywords = [
        "Món ăn Việt Nam nào là đề bài cho các đầu bếp lọt vào Top 5 chương trình Masterchef Mỹ 2013?",
        # "Nhân vật nào sau đây KHÔNG phải là giám khảo của Vòng chung kết cuộc thi Chiếc Thìa Vàng 2016?",
        # "Nhà văn nào đã mô tả: \"Bánh cuốn Thanh Trì mỏng như tờ giấy và trong như lụa\"?",
        # "\"Tôi chưa bao giờ nghĩ là làm một tô bún lại khó đến thế\" là câu nói của đầu bếp nào dưới đây?"
    ]

    # data = []
    # for keyword in keywords:
    #     try:
    #         t = time()
    #         results = scrape_google(keyword, 10, "vi")
    #         for result in results:
    #             data.append(result)
    #         print("Time:", time() - t)

    #     except Exception as e:
    #         print(e)
    #     finally:
    #         time.sleep(3)
    # print(json.dumps(data, ensure_ascii=False))
    # with open("image/result.json", "w") as f:
    #     f.write(json.dumps(data, ensure_ascii=False))   

    with open("data/questions.json") as f:
        questions = json.load(f)

    with open("image/1.json", "r") as f:
        data = json.load(f)
    links = [d["link"] for d in data]
    print(links)


    link = "http://kenh14.vn/kham-pha/su-that-sau-moi-cai-ten-cua-cac-hanh-tinh-quen-thuoc-20140227094250802.chn"

    t = time()
    response = requests.get(link, headers=USER_AGENT)
    
    html = response.text
    soup = BeautifulSoup(html, "html.parser")

    text = soup.title.string
    print(type(text), text)
    
    texts = [p.get_text() for p in soup.find_all("p")]
    # print(texts)
    html_lower = html.lower()
    print(count_keys([html_lower], questions[9]["answer"]))
    print("Time:", time() - t)
    
    with open("image/soup.html", "w") as f:
        f.write(soup.prettify())
    with open("image/soup.txt", "w") as f:
        f.write(" ".join(texts))

