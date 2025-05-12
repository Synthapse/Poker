import zlib
import gzip
import bz2

# Function to try decompression
def try_decompression(file_path):
    with open(file_path, 'rb') as f:
        data = f.read()
    
    # Try zlib decompression
    try:
        decompressed_data = zlib.decompress(data)
        print(f"Decompressed using zlib, size: {len(decompressed_data)}")
        return decompressed_data
    except Exception as e:
        print(f"zlib decompression failed: {e}")
    
    # Try gzip decompression
    try:
        decompressed_data = gzip.decompress(data)
        print(f"Decompressed using gzip, size: {len(decompressed_data)}")
        return decompressed_data
    except Exception as e:
        print(f"gzip decompression failed: {e}")
    
    # Try bz2 decompression
    try:
        decompressed_data = bz2.decompress(data)
        print(f"Decompressed using bz2, size: {len(decompressed_data)}")
        return decompressed_data
    except Exception as e:
        print(f"bz2 decompression failed: {e}")
    
    # Return None if no decompression worked
    return None

# Function to save decompressed data to a file
def save_decompressed_data(decompressed_data, output_path):
    with open(output_path, 'wb') as f:
        f.write(decompressed_data)
    print(f"Decompressed data saved to {output_path}")

# Example usage
file_path_3 = 'subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc/3'
file_path_2 = 'subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc/2'

# Try decompression on the binary files
decompressed_data_3 = try_decompression(file_path_3)
decompressed_data_2 = try_decompression(file_path_2)

# Save decompressed data to new files if decompressed data is valid
if decompressed_data_3:
    save_decompressed_data(decompressed_data_3, 'decompressed_file_3.bin')
if decompressed_data_2:
    save_decompressed_data(decompressed_data_2, 'decompressed_file_2.bin')

# Open the decompressed file in binary mode
with open('decompressed_file_2.bin', 'rb') as file:
    # Read the content of the file
    file_data = file.read()

# Print the first 64 bytes in hexadecimal format
print(file_data.hex()[:128])  # Shows first 64 bytes in hex (128 characters)