import re

def parse_and_decode(file_path):
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    # Match array blocks using regex: grab size and all following numbers
    blocks = re.findall(r'\[arraycoll sz (\d+)((?:[,\s-]*\d+)+)', content)

    for i, (size, num_str) in enumerate(blocks):
        print(f"\n--- Block #{i+1} ---")
        try:
            # Extract integers
            numbers = list(map(int, re.findall(r'-?\d+', num_str)))

            # Truncate to declared size if mismatch
            if len(numbers) > int(size):
                numbers = numbers[:int(size)]

            # Convert to unsigned bytes
            byte_array = bytes((n + 256) % 256 for n in numbers)

            # Try full UTF-8 decode
            try:
                decoded_full = byte_array.decode('utf-8', errors='replace')
                print("Full UTF-8 decoded string (first 500 chars):")
                print(decoded_full[:500])
            except Exception as e:
                print("UTF-8 decoding failed:", e)

            # Search for ASCII substrings
            ascii_strings = re.findall(rb'[ -~]{4,}', byte_array)
            if ascii_strings:
                print("\nExtracted ASCII strings:")
                for s in ascii_strings:
                    print(s.decode('utf-8', errors='replace'))

            else:
                print("No ASCII strings found.")

        except Exception as e:
            print(f"Error processing block #{i+1}: {e}")

# Usage:
parse_and_decode('binary')
