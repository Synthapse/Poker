import os

folder_path = os.getcwd()  # Current folder
csv_files = [file for file in os.listdir(folder_path) if file.endswith('.csv')]

with open('combined_output.csv', 'w', encoding='utf-8') as outfile:
    for i, fname in enumerate(csv_files):
        with open(os.path.join(folder_path, fname), 'r', encoding='utf-8') as infile:
            lines = infile.readlines()
            # Skip header after the first file
            if i > 0:
                lines = lines[1:]
            outfile.writelines(lines)
