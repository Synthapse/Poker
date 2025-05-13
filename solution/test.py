import zipfile
import os
import uuid
import zlib


extension = '.mkr'
filename = '40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-7d'
full_filename = f'{filename}{extension}'
zip_file = f"data/{full_filename}"
extract_dir = f"subfiles/{filename}{extension}"


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

        response_data = [{'file_path': path, 'size': size} for path, size in file_details]
        print(response_data)

    except Exception as e:
        print({'message': f'Error processing the file: {str(e)}'})


uploadFileFunction()