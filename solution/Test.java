import java.io.*;
import org.nibblesec.tools.SerialKiller;  // Correct import for SerialKiller


public class Test {

    public static void main(String[] args) {
        try {
            // Folder where serialized files are stored
            File folder = new File("subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc");
            File[] files = folder.listFiles();


            // Output directory for deserialized files
            File outputFolder = new File("subfiles/output");
            if (!outputFolder.exists()) {
                outputFolder.mkdirs();
            }

            // Check for files in the folder
            if (files != null) {
                for (File file : files) {

                    // Deserializing using SerialKiller
                    try (InputStream is = new FileInputStream(file);
                         ObjectInputStream ois = new SerialKiller(is, "serialkiller.conf")) {

                        // Deserialize object
                        Object deserializedObject = ois.readObject();

                        // Save the deserialized object to the output folder
                        File outputFile = new File(outputFolder, file.getName() + "_deserialized.txt");
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
                            writer.write(deserializedObject.toString());
                        }

                        System.out.println("Successfully deserialized: " + file.getName());

                    } catch (IOException | ClassNotFoundException e) {
                        System.err.println("Failed to deserialize " + file.getName() + ": " + e.getMessage());
                    } catch (Exception e) {
                        System.err.println("Unexpected error while deserializing " + file.getName() + ": " + e.getMessage());
                    }
                }
            } else {
                System.err.println("No files found in the folder.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
