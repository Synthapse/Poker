# 2 and 3 are not java files


import struct
import binascii
import sys

def read_file(path):
    with open(path, "rb") as f:
        return f.read()

def hex_dump(data, length=16):
    """
    Display the hex dump of the data with ASCII representation.
    """
    print("Hex dump:")
    for i in range(0, len(data), length):
        chunk = data[i:i+length]
        hex_part = ' '.join(f"{b:02x}" for b in chunk)
        ascii_part = ''.join(chr(b) if 32 <= b <= 126 else '.' for b in chunk)
        print(f"{i:08x}  {hex_part:<48}  {ascii_part}")

def try_struct_unpack(data):
    """
    Try to interpret the first few bytes of the data as different types.
    """
    print("\nAttempting to interpret first 32 bytes as different types:")
    
    try:
        # Example: Unpack the first 4 bytes as an unsigned 32-bit integer (little-endian)
        uint32_le = struct.unpack_from("<I", data, 0)[0]
        print(f"Uint32 (little endian): {uint32_le}")

        # Example: Unpack the first 4 bytes as a float (little-endian)
        float_le = struct.unpack_from("<f", data, 0)[0]
        print(f"Float (little endian): {float_le}")

        # Example: Unpack the first 4 bytes as a signed 32-bit integer (big-endian)
        uint32_be = struct.unpack_from(">I", data, 0)[0]
        print(f"Uint32 (big endian): {uint32_be}")
        
        # Example: Unpack the first 4 bytes as a float (big-endian)
        float_be = struct.unpack_from(">f", data, 0)[0]
        print(f"Float (big endian): {float_be}")
        
    except struct.error as e:
        print(f"Error unpacking data: {e}")

def detect_magic_numbers(data):
    """
    Detect magic numbers (file signatures) to identify the file type.
    """
    print("\nDetecting magic numbers...")
    
    # Example magic numbers for known file formats
    magic_numbers = {
        b'\x50\x4b\x03\x04': 'ZIP file',  # ZIP file format
        b'\x89\x50\x4e\x47': 'PNG image', # PNG file format
        b'\xFF\xD8\xFF': 'JPEG image',    # JPEG file format
        b'\x7f\x45\x4c\x46': 'ELF file',  # ELF executable
    }

    for magic, file_type in magic_numbers.items():
        if data.startswith(magic):
            print(f"Magic number detected: {file_type}")
            return file_type
    
    print("No known magic number detected.")

def main():

    extension = '.mkr'
    filename = '40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc'
    full_filename = f'{filename}{extension}'
    zip_file = f"data/{full_filename}"
    directory_path = f"subfiles/{filename}/2"
    data = read_file(directory_path)

    print(f"\nAnalyzing file: {directory_path}")

    # Hex dump of first 128 bytes
    hex_dump(data[:128])  # preview first 128 bytes

    # Try to unpack first few bytes in various formats
    try_struct_unpack(data)

    # Detect magic numbers (file signatures)
    detect_magic_numbers(data)

if __name__ == "__main__":
    main()
