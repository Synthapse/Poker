import os
import zipfile
import uuid
from flask import jsonify
from google.cloud import storage
import functions_framework

client = storage.Client()
bucket = client.bucket('auxilia-poker')


def upload_to_bucket(local_path, dest_filename):
    blob = bucket.blob(dest_filename)
    blob.upload_from_filename(local_path)
    return blob


@functions_framework.http
def uploadFileFunction(request):
    headers = {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization',
    }

    if request.method == 'OPTIONS':
        return ('', 204, headers)

    if not request.files or 'file' not in request.files:
        return jsonify({'message': 'No file part'}), 400, headers

    file = request.files['file']
    if file.filename == '':
        return jsonify({'message': 'No selected file'}), 400, headers

    if not file.filename.endswith('.mkr'):
        return jsonify({'message': 'Invalid file type. Only .mkr files are allowed.'}), 400, headers

    try:
        filename = file.filename
        local_path = f"/tmp/{filename}"
        file.save(local_path)  # Save to local /tmp/

        # Upload to bucket
        upload_to_bucket(local_path, filename)

        extract_dir = f"/tmp/{uuid.uuid4().hex}"
        os.makedirs(extract_dir, exist_ok=True)

        file_details = []

        if zipfile.is_zipfile(local_path):
            with zipfile.ZipFile(local_path, 'r') as zip_ref:
                file_info_list = sorted(zip_ref.infolist(), key=lambda x: x.file_size, reverse=True)

                for index, file_info in enumerate(file_info_list):
                    unique_name = f"{index}_{filename}"
                    extracted_path = os.path.join(extract_dir, file_info.filename)
                    renamed_path = os.path.join(extract_dir, unique_name)

                    zip_ref.extract(file_info, extract_dir)
                    os.rename(extracted_path, renamed_path)

                    if zipfile.is_zipfile(renamed_path):
                        inner_extract_dir = f"/tmp/{uuid.uuid4().hex}"
                        os.makedirs(inner_extract_dir, exist_ok=True)
                        with zipfile.ZipFile(renamed_path, 'r') as inner_zip:
                            inner_zip.extractall(inner_extract_dir)
                            inner_file_details = [
                                (os.path.join(inner_extract_dir, f), os.path.getsize(os.path.join(inner_extract_dir, f)))
                                for f in os.listdir(inner_extract_dir)
                            ]
                            file_details.extend(inner_file_details)
                    else:

                        # Handle - 2 & 3 - which is more compressed in file

                        file_details.append((renamed_path, os.path.getsize(renamed_path)))
        else:
            # Not a zip, just return the uploaded file info
            file_details.append((local_path, os.path.getsize(local_path)))

        # Save all extracted files to the bucket
        for path, size in file_details:
            upload_to_bucket(path, os.path.basename(path))
        
        os.remove(local_path)

        response_data = [{'file_path': path, 'size': size} for path, size in file_details]
        return jsonify(response_data), 200, headers

    except Exception as e:
        return jsonify({'message': f'Error processing the file: {str(e)}'}), 500, headers
