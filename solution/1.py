import zipfile
import os
import uuid

extension = '.mkr'
filename = '40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-7d'
full_filename = f'{filename}{extension}'
zip_file = f"data/{full_filename}"
extract_dir = f"subfiles/{filename}"

# Ensure extraction directory exists
if not os.path.exists(extract_dir):
    os.makedirs(extract_dir)

# List to store file details (name, size)
file_details = []

with zipfile.ZipFile(zip_file, 'r') as zip_ref:
    # Access all file info objects
    file_info_list = zip_ref.infolist()
    file_info_list = sorted(file_info_list, key=lambda x: x.file_size, reverse=True)

    # Extract each file with a unique name based on UUID
    for index, file_info in enumerate(file_info_list):
        # Generate a unique filename based on an index and the original filename
        unique_name = f"{index}"
        # Extract the file to the specified directory with the unique name
        zip_ref.extract(file_info, extract_dir)
        # Get the original file path
        original_file_path = os.path.join(extract_dir, file_info.filename)
        # Get the new file path with the unique name
        new_file_path = os.path.join(extract_dir, unique_name)
        # Rename the extracted file to the unique name
        os.rename(original_file_path, new_file_path)
        # Get the size of the extracted file
        file_size = os.path.getsize(new_file_path)
        # Store the new filename and its size in the list
        file_details.append((new_file_path, file_size))

# Sort the file details by size (largest to smallest)
file_details_sorted = sorted(file_details, key=lambda x: x[1], reverse=True)

# Print the files sorted by size
print("\nFiles sorted by size (largest to smallest):")
for file_path, size in file_details_sorted:
    print(f"File: {file_path}, Size: {size} bytes")
