# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

from firebase_functions import https_fn
from firebase_admin import initialize_app
from firebase_functions import storage_fn
import pathlib
from firebase_admin import initialize_app
from firebase_admin import storage
from PIL import Image
import pytesseract
from openai import OpenAI
import json
import io
import PyPDF2

initialize_app()

# [START storageGenerateThumbnailTrigger]
@storage_fn.on_object_finalized(bucket="aifin-bca2c.appspot.com")
def resumeFeedback(event: storage_fn.CloudEvent[storage_fn.StorageObjectData]):
    # [START storageEventAttributes]
    bucket_name = event.data.bucket
    file_path = pathlib.PurePath(event.data.name)
    content_type = event.data.content_type
    # [END storageEventAttributes]

    bucket = storage.bucket(bucket_name)

    # [START storageStopConditions]
    # Exit if this is triggered on a file that is not an image.
    if file_path.name.endswith(".pdf"):
        # [START storageThumbnailGeneration]

        pdf_blob = bucket.blob(str(file_path))
        pdf_bytes = pdf_blob.download_as_bytes()

        client = OpenAI(
            # defaults to os.environ.get("OPENAI_API_KEY")
            api_key="Insert Key",
        )

        pdf_reader = PyPDF2.PdfReader(io.BytesIO(pdf_bytes))
        text = ""
        for page_num in range(len(pdf_reader.pages)):
            page = pdf_reader.pages[page_num]
            text += page.extract_text()

        query = 'Rate this resume out of 10 and give one sentence feedback to improve it write your output in this format: "Rating" + "\n" + "Feedback"'
        user_msg = text + "\n\n" + query
        system_msg = "You are a helpful career advisor."


        response = client.chat.completions.create(
            model = "gpt-4o-mini",
            messages =
            [
            {"role": "system", "content": system_msg},
            {"role": "user", "content": user_msg},
            ]
        )

        results_blob = bucket.blob(f"pdfResults/{file_path.stem}.json")
        results_blob.upload_from_string(json.dumps(response.choices[0].message.content.strip()), content_type="json")

#
#
# @https_fn.on_request()
# def on_request_example(req: https_fn.Request) -> https_fn.Response:
#     return https_fn.Response("Hello world!")
