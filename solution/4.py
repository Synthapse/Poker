import struct

def read_file(file_path):
    """ Read the entire binary file. """
    with open(file_path, "rb") as f:
        return f.read()

def hex_dump(data, start=0, length=32):
    """ Display the data as a human-readable hex dump. """
    hex_repr = ' '.join(f'{byte:02x}' for byte in data[start:start+length])
    ascii_repr = ''.join(chr(byte) if 32 <= byte <= 126 else '.' for byte in data[start:start+length])
    return f"Hex: {hex_repr}  |  ASCII: {ascii_repr}"

def extract_and_display_raw_data(data, output_file):
    """ Extract the raw 32 bytes and write them to a file in human-readable format. """
    for i in range(0, len(data), 32):  # Process the file in chunks of 32 bytes
        chunk = data[i:i+32]
        if len(chunk) == 32:
            output_file.write(f"Raw 32 bytes at position {i}:\n")
            output_file.write(hex_dump(chunk) + "\n\n")  # Print both hex and ASCII representations

def process_file(input_file, output_file_path):
    """ Process the binary file, extract the raw data, and save the results in a text file. """
    data = read_file(input_file)
    
    with open(output_file_path, 'w') as output_file:
        # Extract and write raw data for each chunk of 32 bytes
        extract_and_display_raw_data(data, output_file)

        # Assuming the file is structured as a list of floats (or some known structure), you can unpack the binary data
        output_file.write("\nExtracting structured data from the file...\n")

        chunk_size = 4  # For example, if we're extracting 4-byte (32-bit) floats
        num_elements = len(data) // chunk_size  # Total number of elements based on chunk size
        
        # Debugging: Track outliers and unexpected float values
        outlier_values = []
        for i in range(num_elements):
            chunk = data[i * chunk_size:(i + 1) * chunk_size]
            
            if len(chunk) == chunk_size:
                try:
                    # Try unpacking as a float (4 bytes)
                    value = struct.unpack('f', chunk)[0]  # Unpack as float (32-bit)
                    
                    # Set up thresholds for detecting potential outlier values
                    if value < -1e20 or value > 1e20:
                        outlier_values.append((i, value))  # Track outliers
                    
                    output_file.write(f"Float value at position {i}: {value}\n")
                except Exception as e:
                    output_file.write(f"Error unpacking chunk at position {i}: {e}\n")
        
        if outlier_values:
            output_file.write("\nOutlier values detected:\n")
            for i, value in outlier_values:
                output_file.write(f"Position {i}: {value}\n")
        else:
            output_file.write("\nNo outlier values detected.\n")

# Example usage:
directory_path = f"subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc/2"
output_file_path = "output_results.txt"  # Specify the path to save the results
process_file(directory_path, output_file_path)

print(f"Results have been saved to {output_file_path}")
