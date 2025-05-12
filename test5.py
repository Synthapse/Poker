import msgpack


#b '\xbd\xef\xbf\xbd\xef\xbf\xbdmYU-|\xef\xbf\xbdP\xef\xbf\xbdCI\x06AEP\x14D\xef\xbf\xbd\x04'

file = "object"
path = f'extracted_files2/{file}'

with open(path, 'rb') as f:
    binary_data = f.read()  # Read the file as binary data

    # Use raw=True to keep byte strings in binary form (avoid UTF-8 decoding)
    unpacker = msgpack.Unpacker(raw=True, strict_map_key=False)
    unpacker.feed(binary_data)

    # Iterate and print each unpacked object
    while True:
        try:
            obj = next(unpacker)
            print(obj)
        except StopIteration:
            break  # End of data
        except Exception as e:
            print("Error unpacking:", e)
            break
