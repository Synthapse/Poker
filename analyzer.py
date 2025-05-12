# Step 1: Read the binary .mkr file

#file = "test.mkr"  # Replace with the actual extracted file name
file = "extracted_files2/object"

with open(file, "rb") as file:
    data = file.read()

a = 256
b = 512
c = 1024

# Step 2: Print out first x bytes in hex and ASCII to inspect
for i in range(0, b, 16):
    chunk = data[i:i+16]
    hex_values = ' '.join(f"{b:02X}" for b in chunk)
    ascii_values = ''.join(chr(b) if 32 <= b <= 126 else '.' for b in chunk)
    print(f"{i:04X}  {hex_values:<48}  {ascii_values}")


with open(extracted_file, 'rb') as file:
    data = file.read()

# Check the first few bytes of the file to inspect its structure
print(data[:1000])  # Print first 100 bytes to understand the data structure