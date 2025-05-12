import os

def count_lines_in_files(folder_path):
    # Initialize the total line count
    total_lines = 0
    
    # List all files in the specified folder
    for filename in os.listdir(folder_path):
        # Get the full path of the file
        file_path = os.path.join(folder_path, filename)
        
        # Check if the item is a file (not a directory)
        if os.path.isfile(file_path):
            # Open the file and count the number of lines
            with open(file_path, 'r', encoding='utf-8', errors='ignore') as file:
                file_lines = sum(1 for line in file)
                total_lines += file_lines
                print(f"File: {filename} has {file_lines} lines.")
    
    return total_lines

# Example usage:
folder_path = "./"  # Replace with your folder path
total_line_count = count_lines_in_files(folder_path)
print(f"Total number of lines in all files: {total_line_count}")
