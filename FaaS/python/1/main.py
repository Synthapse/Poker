import os
import zipfile
import uuid
from flask import jsonify
from google.cloud import storage
import functions_framework
import zlib

client = storage.Client()
bucket = client.bucket('auxilia-poker')


def upload_to_bucket(local_path, dest_filename):
    blob = bucket.blob(dest_filename)
    blob.upload_from_filename(local_path)
    return blob

def is_java_serialized(file_path):
    try:
        with open(file_path, 'rb') as file:
            # Read the first 4 bytes
            magic_number = file.read(4)
            # Check if the magic number matches the Java serialized object magic number (0xACED0005)
            return magic_number == b'\xAC\xED\x00\x05'
    except Exception as e:
        print(f"Error checking file: {e}")
        return False

def decompress_file(file_path):
    """Decompress the file using zlib and return the decompressed data."""
    try:
        with open(file_path, 'rb') as file:
            compressed_data = file.read()
            decompressed_data = zlib.decompress(compressed_data)
        
        # Generate a unique name for the decompressed file
        decompressed_file_path = file_path + '.decompressed'
        with open(decompressed_file_path, 'wb') as decompressed_file:
            decompressed_file.write(decompressed_data)

        return decompressed_file_path
    except Exception as e:
        print(f"Error decompressing file {file_path}: {e}")
        return None

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
                    unique_name = f"{index}_{uuid.uuid4().hex}"
                    original_path = os.path.join(extract_dir, file_info.filename)
                    new_path = os.path.join(extract_dir, unique_name)

                    zip_ref.extract(file_info, extract_dir)
                    os.rename(original_path, new_path)


                    if is_java_serialized(new_path):
                        print(f"The file {new_path} is a Java serialized object.")
                        file_details.append((new_path, os.path.getsize(new_path)))
                    else:
                        decompressed_file = decompress_file(new_path)
                        if decompressed_file:
                            print(f"File decompressed: {decompressed_file}")
                            # Now process the decompressed file further
                            file_details.append((decompressed_file, os.path.getsize(decompressed_file)))
                        else:
                            print(f"Failed to decompress the file {new_path}")
        else:
            # Not a zip, just return the uploaded file info
            file_details.append((local_path, os.path.getsize(local_path)))

        # Save all extracted files to the bucket
        for path, size in file_details:
            upload_to_bucket(path, f"{filename}/{os.path.basename(path)}")
        
        os.remove(local_path)

        response_data = [{'file_path': path, 'size': size} for path, size in file_details]
        return jsonify(response_data), 200, headers

    except Exception as e:
        return jsonify({'message': f'Error processing the file: {str(e)}'}), 500, headers
