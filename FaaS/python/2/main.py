import os
import base64
from flask import jsonify, request
from google.cloud import storage
import functions_framework

client = storage.Client()
BUCKET_NAME = 'auxilia-poker'

@functions_framework.http
def downloadFilesFunction(request):
    headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    }

    if request.method == 'OPTIONS':
        return ('', 204, headers)

    try:
        data = request.get_json(silent=True)
        if not data or 'folderName' not in data:
            return jsonify({'message': 'Missing folderName'}), 400, headers

        folder_name = data['folderName']
        if not folder_name.endswith('/'):
            folder_name += '/'

        bucket = client.bucket(BUCKET_NAME)
        blobs = list(client.list_blobs(bucket, prefix=folder_name))

        if not blobs:
            return jsonify({'message': 'No files found in the specified folder'}), 404, headers

        file_responses = []

        for i, blob in enumerate(blobs):
            if blob.name.endswith('/') or i >= 20:
                continue  # skip "directories" and limit to 20 files

            local_path = f"/tmp/{os.path.basename(blob.name)}"
            blob.download_to_filename(local_path)

            with open(local_path, "rb") as f:
                encoded_content = base64.b64encode(f.read()).decode('utf-8')

            file_responses.append({
                'file_name': os.path.basename(blob.name),
                'content_base64': encoded_content,
                'content_type': blob.content_type or 'application/octet-stream',
                'size_bytes': os.path.getsize(local_path)
            })

        return jsonify({'files': file_responses}), 200, headers

    except Exception as e:
        return jsonify({'message': f'Error: {str(e)}'}), 500, headers
