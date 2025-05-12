import os
import zipfile
import uuid
import struct
import json

def sanitize_filename(filename):
    """Sanitize file names to avoid problematic characters."""
    return filename.replace("/", "_").replace("\\", "_")

def analyze_file(file_path):
    """Analyze the file content to determine its type and structure."""
    file_info = {
        "file_path": file_path,
        "type": "Unknown",
        "size": os.path.getsize(file_path),
        "content_preview": None
    }

    try:
        with open(file_path, 'rb') as file:
            # Read the first few bytes for analysis
            content = file.read(100)
            file_info["content_preview"] = content.hex()

            # Try to detect serialized objects (common serialization formats)
            if content.startswith(b'\x80\x01'):
                # It could be Python's pickle format (e.g., b'\x80\x01' is the start of a pickle byte stream)
                file_info["type"] = "Pickle (Python)"
            elif content.startswith(b'\x89PNG\r\n\x1a\n'):
                # PNG files start with this header, can be an image
                file_info["type"] = "PNG Image"
            elif content[:4] == b'PK\x03\x04':
                # ZIP file signature, e.g., ZIP file format
                file_info["type"] = "ZIP Archive"
            elif b'{' in content and b'}' in content:
                # Looks like JSON, check if it's a valid JSON object
                try:
                    json.loads(content.decode('utf-8'))
                    file_info["type"] = "JSON"
                except json.JSONDecodeError:
                    pass
            else:
                file_info["type"] = "Binary data"

    except Exception as e:
        file_info["type"] = f"Error reading file: {str(e)}"

    return file_info

def analyze_directory(directory_path):
    """Analyze all files in a given directory."""
    analysis_results = []
    for root, dirs, files in os.walk(directory_path):
        for file_name in files:
            file_path = os.path.join(root, file_name)
            analysis_results.append(analyze_file(file_path))
    return analysis_results

def deserialize_pickled_file(file_path):
    """Try to deserialize a pickle file."""
    try:
        import pickle
        with open(file_path, 'rb') as f:
            return pickle.load(f)
    except Exception as e:
        print(f"Error deserializing Pickle file {file_path}: {e}")
        return None

def deserialize_json_file(file_path):
    """Try to deserialize a JSON file."""
    try:
        with open(file_path, 'r') as f:
            return json.load(f)
    except Exception as e:
        print(f"Error deserializing JSON file {file_path}: {e}")
        return None

def deserialize_zip_file(file_path):
    """Try to open and list the contents of a ZIP file."""
    try:
        with zipfile.ZipFile(file_path, 'r') as zip_ref:
            return zip_ref.namelist()
    except Exception as e:
        print(f"Error opening ZIP file {file_path}: {e}")
        return None

def provide_deserialization_instructions(file_info):
    """Provide instructions based on the file type."""
    instructions = ""

    if file_info["type"] == "Pickle (Python)":
        instructions = "This is likely a Python pickle file. You can deserialize it using the `pickle` module in Python."
    elif file_info["type"] == "JSON":
        instructions = "This is a JSON file. You can deserialize it using the `json` module in Python with `json.load()`."
    elif file_info["type"] == "ZIP Archive":
        instructions = "This is a ZIP archive. You can use the `zipfile` module in Python to extract and read the files inside."
    else:
        instructions = "This file's format is not recognized for deserialization. It may require custom handling."

    return instructions

def main():
    #directory_path = input("Enter the path to the directory containing the files to analyze: ")

    extension = '.mkr'
    filename = '40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc'
    full_filename = f'{filename}{extension}'
    zip_file = f"data/{full_filename}"
    directory_path = f"subfiles/{filename}"
    # Analyze the files in the directory
    analysis_results = analyze_directory(directory_path)

    # Output analysis results
    print("\nAnalysis Results:")
    for file_info in analysis_results:
        # print(f"\nFile: {file_info['file_path']}")
        # print(f"Type: {file_info['type']}")
        # print(f"Size: {file_info['size']} bytes")
        print(f"Content preview (hex): {file_info['content_preview'][:100]}")

        # Provide deserialization instructions
        deserialization_instructions = provide_deserialization_instructions(file_info)
        #print(f"Deserialization instructions: {deserialization_instructions}")

        if file_info["type"] == "Pickle (Python)":
            # Try to deserialize if it's a pickle
            deserialized_data = deserialize_pickled_file(file_info['file_path'])
            if deserialized_data:
                print(f"Successfully deserialized data: {deserialized_data}")
        elif file_info["type"] == "JSON":
            # Try to deserialize if it's JSON
            deserialized_data = deserialize_json_file(file_info['file_path'])
            if deserialized_data:
                print(f"Successfully deserialized data: {deserialized_data}")
        elif file_info["type"] == "ZIP Archive":
            # Try to open and read ZIP contents
            zip_contents = deserialize_zip_file(file_info['file_path'])
            if zip_contents:
                print(f"ZIP file contains the following files: {zip_contents}")

if __name__ == "__main__":
    main()
