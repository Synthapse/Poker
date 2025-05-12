
https://filext.com/file-extension/MKR


- Files 0 and 1 may represent large data arrays (e.g., features or samples and their corresponding labels or keys).
- File 2 could be model-related data: probabilities, weights, scores, or other computed values.
- Files 3–19 may provide:

Descriptive metadata (like what each index in files 0/1/2 means),
Mappings (like HashMaps from serialized Java),
Control parameters for reading/parsing the rest of the data (e.g., thresholds, factors).



These may be:

-> Binary (may need struct or other parsing tools)
-> Serialized Java objects (as you can see java.util.ArrayList in the output)


https://hexed.it/



Hand strength estimation (maybe via a numeric model output).
Opponent modeling inputs.

Perfect — this hex dump shows your .mkr file is actually a ZIP archive!
The file starts with the magic bytes 50 4B 03 04, which identifies it as a standard ZIP file. 

-> That means MonkeySolver .mkr files are just ZIPs containing structured files — likely XML, binary, or serialized Java objects.

0000  50 4B 03 04 14 00 08 00 08 00 75 14 68 59 00 00   PK........u.hY..
0010  00 00 00 00 00 00 00 00 00 00 0A 00 00 00 FE FF   ................
0020  00 74 00 72 00 65 00 65 01 7F 00 80 FF 00 00 00   .t.r.e.e........
0030  00 00 00 82 D2 00 00 00 00 00 00 00 01 00 00 00   ................
0040  01 00 00 00 02 00 00 00 00 00 00 00 01 00 00 00   ................
0050  03 00 01 EC 30 00 05 37 F0 00 05 37 F0 00 02 9C   ....0..7...7....
0060  61 00 03 9C A4 00 03 00 03 00 02 00 00 00 00 00   a...............
0070  01 00 00 00 00 00 00 00 01 00 00 00 00 00 00 00   ................
0080  01 00 00 00 01 00 02 9C A4 00 03 00 03 00 02 00   ................
0090  00 00 00 00 01 00 00 00 00 00 00 00 01 00 00 00   ................
00A0  01 00 00 00 00 00 00 00 00 00 00 00 50 4B 07 08   ............PK..
00B0  62 5B 15 D5 84 00 00 00 7F 00 00 00 50 4B 03 04   b[..........PK..
00C0  14 00 08 00 08 00 75 14 68 59 00 00 00 00 00 00   ......u.hY......
00D0  00 00 00 00 00 00 0C 00 00 00 FE FF 00 62 00 6F   .............b.o
00E0  00 61 00 72 00 64 01 53 00 AC FF AC ED 00 05 73   .a.r.d.S.......s
00F0  72 00 13 6A 61 76 61 2E 75 74 69 6C 2E 41 72 72   r..java.util.Arr


(starting with PK\x03\x04, which is a standard ZIP file header) 

b'PK\x03\x04\x14\x00\x08\x00\x08\x00u\x14hY\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\x00\n\x00\x00\x00\xfe\xff\x00t\x00r\x00e\x00e\x01\x7f\x00\x80\xff\x00\x00\x00\x00\x00\x00\x82\xd2\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x01\x00\x00\x00\x02\x00\x00\x00\x00\x00\x00\x00\x01\x00\x00\x00\x03\x00\x01\xec0\x00\x057\xf0\x00\x057\xf0\x00\x02\x9ca\x00\x03\x9c'


Every filename starts with:

[254, 255]  →  0xFE 0xFF

File Name                                             Modified             Size
■                                              2024-11-08 02:35:42          127
■                                              2024-11-08 02:35:42           83
■                                              2024-11-08 02:35:42       576211
■                                              2024-11-08 02:35:42           43
■                                              2024-11-08 02:35:42           43
■                                              2024-11-08 02:35:42           81
■                                              2024-11-08 02:35:42           81
■                                              2024-11-08 02:35:42           81
■                                              2024-11-08 02:35:42           82
■                                              2024-11-08 02:35:42           43
■                                              2024-11-08 02:35:42           82
■                                              2024-11-08 02:35:42           81
■                                              2024-11-08 02:35:42           82
■                                              2024-11-08 02:35:42           84
■                                              2024-11-08 02:35:42           81
■                                              2024-11-08 02:35:42           81
■                                              2024-11-08 02:35:42           82
■                                              2024-11-08 02:35:42          147
■                                              2024-11-08 02:35:42      2165827
■                                              2024-11-08 02:35:42      2165827



Key Points:

Java Serialization Signature: The bytes b'\xac\xed\x00\x05' correspond to the start of a serialized Java object. This is commonly seen when working with Java-based applications and file formats.
Object Serialization Format: The serialized format in Java is a binary representation of an object, which can only be deserialized and used by a Java program that knows how to interpret it.


javac DeserializeTest.java

java DeserializeTest


- 20 extracted files


- These files are binary files and are not directly readable as text.

It seems like all the files you're trying to read have the same structure at the beginning, and the content appears to be in a serialized Java object format (as indicated by the \xac\xed\x00\x05 byte sequence at the start).
This is a common signature for Java's Object Serialization (used by ObjectInputStream in Java).



Java serialized objects usually start with a magic number 
0xAC 0xED 0x00 0x05 
(which corresponds to aced0005 in hex)


Deserialized object from file: 20
[D@355da254
Deserialized object from file: 18
[D@4dc63996
Deserialized object from file: 9
[D@d716361


270725


1. jar tf SerialKiller-0.4-all.jar
2. javac -cp .:SerialKiller-0.4-all.jar Test.java
3. java -cp .:SerialKiller-0.4-all.jar Test




java --add-opens java.base/java.lang=ALL-UNNAMED Test1

java -jar jdeserialize-1.2.jar subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc/2


java -jar jdeserialize-1.2.jar subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc/decompressed_file_2 \
| grep -oE '[0-9]+' > numbers_only.txt



class java.util.HashMap implements java.io.Serializable {
    float loadFactor;
    int threshold;
}

class java.lang.Double extends java.lang.Number implements java.io.Serializable {
    double value;
}


(18 out of 20) are Excel files
file 2 & 3 are not in java

Structure

First value: 8-character string (representing the card hand, e.g., KhAs4s2s).
Second value: A decimal number (this could represent a probability or strength, e.g., 0.135).
Third value: A 6-character number (which could represent a game ID, timestamp, or some other metric, e.g., 322559).

First 32bytes

Analyzing file: subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc/2
Hex dump:
00000000  78 01 ec bd 69 d7 2d 55 b1 35 88 80 02 2a a8 a8   x...i.-U.5...*..
00000010  60 83 28 02 8a 7d 8f 1d ca 55 b1 bf 97 ab 62 83   `.(..}...U....b.
00000020  8a 28 b8 7f c0 fb d6 18 ef a8 fa 54 bf 64 d1 09   .(.........T.d..
00000030  22 a0 a0 a0 d2 09 22 8d d2 29 a0 b4 02 4a 2f ad   "....."..)...J/.
00000040  34 8a 74 22 d6 9c 31 23 62 45 ae cc fd 9c 83 57   4.t"..1#bE.....W
00000050  eb 56 8d aa 73 72 af 8c 98 31 63 c6 ca dc fb d9   .V..sr...1c.....
00000060  4d e6 ca 95 df fb e3 16 cf fc df b7 de 62 8b 2d   M............b.-
00000070  76 f9 5f ff db 16 5b 7e f5 c3 df fb f3 ce 8f 3d   v._...[~.......=

Attempting to interpret first 32 bytes as different types:
Uint32 (little endian): 3186360696
Float (little endian): -0.11523717641830444
Uint32 (big endian): 2013392061
Float (big endian): 1.0540748711565152e+34