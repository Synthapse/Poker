import java.io.*;
import java.lang.reflect.*;
import java.util.*;

public class Test1 {

    public static void main(String[] args) {
        // Folder where serialized files are stored
        File folder = new File("subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc");
        File[] files = folder.listFiles();

        // Output directory for deserialized files
        File outputFolder = new File("subfiles/output");
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        if (files != null) {
            for (File file : files) {
                try (InputStream is = new FileInputStream(file);
                     ObjectInputStream ois = new ObjectInputStream(is)) {

                    Object deserializedObject = ois.readObject();
                    System.out.println("Deserialized object type: " + deserializedObject.getClass().getName());

                    // Save deserialized object (now supports HashMap and other Map types)
                    saveDeserializedObject(deserializedObject, outputFolder, file.getName());

                    System.out.println("Successfully deserialized and saved: " + file.getName());

                } catch (IOException | ClassNotFoundException e) {
                    System.err.println("Failed to deserialize " + file.getName() + ": " + e.getMessage());
                }
            }
        } else {
            System.err.println("No files found in the folder.");
        }
    }

    // UPDATED: Save deserialized object, now handles HashMaps and more
    public static void saveDeserializedObject(Object obj, File outputFolder, String originalFileName) {
        File outputFile = new File(outputFolder, originalFileName + "_deserialized.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            if (obj instanceof Map<?, ?> map) {
                writer.write("Map contents:");
                writer.newLine();
                for (Map.Entry<?, ?> entry : map.entrySet()) {
                    writer.write("  " + entry.getKey() + " = " + entry.getValue());
                    writer.newLine();
                }

            } else if (obj instanceof Object[] array) {
                for (Object item : array) {
                    writer.write(String.valueOf(item));
                    writer.newLine();
                }

            } else if (obj instanceof double[] array) {
                for (double item : array) {
                    writer.write(String.valueOf(item));
                    writer.newLine();
                }

            } else if (obj instanceof int[] array) {
                for (int item : array) {
                    writer.write(String.valueOf(item));
                    writer.newLine();
                }

            } else if (obj instanceof long[] array) {
                for (long item : array) {
                    writer.write(String.valueOf(item));
                    writer.newLine();
                }

            } else if (obj instanceof byte[] array) {
                for (byte item : array) {
                    writer.write(String.valueOf(item));
                    writer.newLine();
                }

            } else {
                // Generic object fallback
                writer.write(String.valueOf(obj));
            }

        } catch (IOException e) {
            System.err.println("Failed to save object to file: " + e.getMessage());
        }
    }

    // Optional: use this for debugging fields of individual objects
    public static void printObjectFields(Object obj) {
        if (obj instanceof ArrayList) {
            System.out.println("ArrayList - skipping serialVersionUID field.");
            return;
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            try {
                field.setAccessible(true);
                System.out.println(field.getName() + ": " + field.get(obj));
            } catch (IllegalAccessException e) {
                System.err.println("Unable to access field: " + field.getName());
            }
        }
    }
}
