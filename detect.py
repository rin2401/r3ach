# from google.cloud import vision
# from google.oauth2 import service_account
# credentials = service_account.Credentials.from_service_account_file(
#    "r3fest.json")


# scoped_credentials = credentials.with_scopes(
#     ['https://www.googleapis.com/auth/cloud-platform'])


# def parse_image(image_path=None):
#     client = vision.ImageAnnotatorClient(credentials=credentials)
#     response = client.text_detection(image=open(image_path, 'rb'))
#     text = response.text_annotations
#     del response     # to clean-up the system memory

#     return text[0].description


# print(credentials.__dict__)

# image_content = parse_image(image_path="1.jpg")

# print(image_content)

# # my_formatted_text = ""
# # for line in image_content.split("\n"):
# #     my_formatted_text += "    " + line + "\n"


from google.cloud import vision
from google.cloud.vision import types

image_uri = 'gs://cloud-vision-codelab/otter_crossing.jpg'

import os
os.environ["GOOGLE_APPLICATION_CREDENTIALS"] = "r3fest.json"

client = vision.ImageAnnotatorClient()
image = types.Image()
image.source.image_uri = image_uri

response = client.text_detection(image=image)

for text in response.text_annotations:
    print('=' * 79)
    print('"{}"'.format(text.description))

    vertices = (['({},{})'.format(v.x, v.y)
                 for v in text.bounding_poly.vertices])

    print('bounds: {}'.format(','.join(vertices)))