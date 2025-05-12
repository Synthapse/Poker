import zipfile
import os
import uuid


extension = '.mkr'
filename = '40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-7d'
full_filename = f'{filename}{extension}'
zip_file = f"data/{full_filename}"
extract_dir = f"subfiles/{filename}{extension}"


def uploadFileFunction():
    try:

        file_details = []
        with zipfile.ZipFile(zip_file, 'r') as zip_ref:
            file_info_list = sorted(zip_ref.infolist(), key=lambda x: x.file_size, reverse=True)

            for index, file_info in enumerate(file_info_list):
                unique_name = f"{index}_{uuid.uuid4().hex}"
                original_path = os.path.join(extract_dir, file_info.filename)
                new_path = os.path.join(extract_dir, unique_name)

                zip_ref.extract(file_info, extract_dir)
                os.rename(original_path, new_path)

                # Check if the extracted file itself is a zip file
                if zipfile.is_zipfile(new_path):
                    # If the extracted file is a zip file, extract it too
                    with zipfile.ZipFile(new_path, 'r') as inner_zip:
                        inner_extract_dir = f"/tmp/{uuid.uuid4().hex}"
                        os.makedirs(inner_extract_dir, exist_ok=True)
                        inner_zip.extractall(inner_extract_dir)
                        inner_file_details = [
                            (os.path.join(inner_extract_dir, f), os.path.getsize(os.path.join(inner_extract_dir, f)))
                            for f in os.listdir(inner_extract_dir)
                        ]
                        file_details.extend(inner_file_details)
                else:
                    # Otherwise, just add the file details
                    file_details.append((new_path, os.path.getsize(new_path)))


                file_details.append((new_path, os.path.getsize(new_path)))

        response_data = [{'file_path': path, 'size': size} for path, size in file_details]
        print(response_data)

    except Exception as e:
        print({'message': f'Error processing the file: {str(e)}'})


uploadFileFunction()