import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.lang.reflect.Field;

// not serialized correctly files 
// 7cac8b68-124d-470d-9ac8-92fd851e3910_■ 
// 06ab6368-84f6-46e8-be73-5afd3827b425


public class DeserializeTest {
    public static void main(String[] args) {
        File folder = new File("subfiles/40bb-RIVER-BTNvBB-KQ2ss-bc-6x-xx-Kc"); // Folder where your files are stored
        File[] files = folder.listFiles();

        // Output directory for deserialized files
        File outputFolder = new File("subfiles/output");
        if (!outputFolder.exists()) {
            outputFolder.mkdir(); // Create output folder if it doesn't exist
        }

        if (files != null) {
            for (File file : files) {
                // Only process files (ignore directories)
                if (file.isFile()) {
                    File outputFile = new File(outputFolder, file.getName() + "_deserialized.txt");

                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
                         BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

                        // Deserialize the object from the file
                        Object obj = ois.readObject();

                        // Process different types of objects
                    if (obj instanceof Object[]) {
                            processObject(obj, writer, file);
                        } else if (obj instanceof int[]) {
                            writer.write("Deserialized object from file (int[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((int[]) obj));
                            writer.newLine();
                        } else if (obj instanceof double[]) {
                            writer.write("Deserialized object from file (double[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((double[]) obj));
                            writer.newLine();
                        } else if (obj instanceof long[]) {
                            writer.write("Deserialized object from file (long[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((long[]) obj));
                            writer.newLine();
                        } else if (obj instanceof float[]) {
                            writer.write("Deserialized object from file (float[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((float[]) obj));
                            writer.newLine();
                        } else if (obj instanceof boolean[]) {
                            writer.write("Deserialized object from file (boolean[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((boolean[]) obj));
                            writer.newLine();
                        } else if (obj instanceof char[]) {
                            writer.write("Deserialized object from file (char[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((char[]) obj));
                            writer.newLine();
                        } else if (obj instanceof short[]) {
                            writer.write("Deserialized object from file (short[]): " + file.getName());
                            writer.newLine();
                            writer.write(Arrays.toString((short[]) obj));
                            writer.newLine();
                        } else if (obj instanceof List) {
                            writer.write("Deserialized object from file (List): " + file.getName());
                            writer.newLine();
                            writer.write(obj.toString());
                            writer.newLine();
                        } else if (obj instanceof Set) {
                            writer.write("Deserialized object from file (Set): " + file.getName());
                            writer.newLine();
                            writer.write(obj.toString());
                            writer.newLine();
                        } else if (obj instanceof Map) {
                            writer.write("Deserialized object from file (Map): " + file.getName());
                            writer.newLine();
                            writer.write(obj.toString());
                            writer.newLine();
                        } else if (obj instanceof String) {
                            writer.write("Deserialized object from file (String): " + file.getName());
                            writer.newLine();
                            writer.write((String) obj);
                            writer.newLine();  
                        }
                          else if (obj instanceof HashMap) {
                            writer.write("Deserialized object from file (HashMap): " + file.getName());
                            writer.newLine();
                            writer.write(obj.toString());
                            writer.newLine();
                        } else if (obj instanceof Map) {
                            writer.write("Deserialized object from file (Map): " + file.getName());
                            writer.newLine();
                            writer.write(obj.toString());
                            writer.newLine();
                        }
                        else {
                            // Handle other types of objects
                            writer.write("Deserialized object from file (Other): " + file.getName());
                            writer.newLine();
                            writer.write("Object type: " + obj.getClass().getName());  // Print the class type
                            writer.newLine();
                            writer.write(obj.toString());  // Optionally print the object's string representation
                            writer.newLine();
                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Deserialized data has been saved to the output folder.");
        } else {
            System.out.println("No files found in the specified folder.");
        }
    }

    private static void processObject(Object obj, BufferedWriter writer, File file) throws IOException {
        writer.write("Deserialized object from file (Object[]): " + file.getName());
        writer.newLine();
        Object[] array = (Object[]) obj;
        for (int i = 0; i < array.length; i++) {
            writer.write("Element " + i + ": ");
            Object element = array[i];
            if (element == null) {
                writer.write("null");
            } else if (element instanceof Map) {
                writer.write("Map contents: ");
                writeMapContent((Map<?, ?>) element, writer);
            } else if (element instanceof List) {
                writer.write("List contents: ");
                writeListContent((List<?>) element, writer);
            } else if (isPrimitiveOrWrapper(element.getClass()) || element instanceof String) {
                writer.write(element.toString());
            } else {
                // Handle other objects using reflection
                writer.write("{");
                Field[] fields = element.getClass().getDeclaredFields();
                for (int j = 0; j < fields.length; j++) {
                    fields[j].setAccessible(true);
                    writer.write(fields[j].getName() + "=");
                    try {
                        writer.write(String.valueOf(fields[j].get(element)));
                    } catch (IllegalAccessException e) {
                        writer.write("ACCESS_ERROR");
                    }
                    if (j < fields.length - 1) writer.write(", ");
                }
                writer.write("}");
            }
            writer.newLine();
        }
    }


    // Method to handle writing content for maps
    private static void writeMapContent(Map<?, ?> map, BufferedWriter writer) throws IOException {
        if (map == null || map.isEmpty()) {
            writer.write("Empty Map");
            writer.newLine();
            return;
        }

        for (Map.Entry<?, ?> entry : map.entrySet()) {
            writer.write(entry.getKey() + "=");
            Object value = entry.getValue();
            if (value instanceof Map) {
                // Recursively handle nested maps
                writer.write("{");
                writeMapContent((Map<?, ?>) value, writer);
                writer.write("}");
            } else if (value instanceof List) {
                // Handle List contents
                writer.write("[");
                writeListContent((List<?>) value, writer);
                writer.write("]");
            } else if (value.getClass().isArray()) {
                // Handle Array contents
                writer.write("[");
                writeArrayContent(value, writer);
                writer.write("]");
            } else if (isPrimitiveOrWrapper(value.getClass()) || value instanceof String) {
                writer.write(value.toString());
            } else {
                // Handle custom objects using reflection
                writer.write("{");
                Field[] fields = value.getClass().getDeclaredFields();
                for (int j = 0; j < fields.length; j++) {
                    fields[j].setAccessible(true);
                    writer.write(fields[j].getName() + "=");
                    try {
                        writer.write(String.valueOf(fields[j].get(value)));
                    } catch (IllegalAccessException e) {
                        writer.write("ACCESS_ERROR");
                    }
                    if (j < fields.length - 1) writer.write(", ");
                }
                writer.write("}");
            }
            writer.newLine();
        }
    }

    // Helper method to handle list contents
    private static void writeListContent(List<?> list, BufferedWriter writer) throws IOException {
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                writer.write("Element " + i + ": ");
                Object element = list.get(i);
                if (element == null) {
                    writer.write("null");
                } else if (isPrimitiveOrWrapper(element.getClass()) || element instanceof String) {
                    writer.write(element.toString());
                } else {
                    // Handle nested objects using reflection
                    writer.write("{");
                    Field[] fields = element.getClass().getDeclaredFields();
                    for (int j = 0; j < fields.length; j++) {
                        fields[j].setAccessible(true);
                        writer.write(fields[j].getName() + "=");
                        try {
                            writer.write(String.valueOf(fields[j].get(element)));
                        } catch (IllegalAccessException e) {
                            writer.write("ACCESS_ERROR");
                        }
                        if (j < fields.length - 1) writer.write(", ");
                    }
                    writer.write("}");
                }
                writer.newLine();
            }
        }
    }

    // Helper method to handle array contents
    private static void writeArrayContent(Object array, BufferedWriter writer) throws IOException {
        if (array instanceof Object[]) {
            Object[] objArray = (Object[]) array;
            for (int i = 0; i < objArray.length; i++) {
                writer.write("Element " + i + ": ");
                Object element = objArray[i];
                if (element == null) {
                    writer.write("null");
                } else if (isPrimitiveOrWrapper(element.getClass()) || element instanceof String) {
                    writer.write(element.toString());
                } else {
                    // Handle nested objects using reflection
                    writer.write("{");
                    Field[] fields = element.getClass().getDeclaredFields();
                    for (int j = 0; j < fields.length; j++) {
                        fields[j].setAccessible(true);
                        writer.write(fields[j].getName() + "=");
                        try {
                            writer.write(String.valueOf(fields[j].get(element)));
                        } catch (IllegalAccessException e) {
                            writer.write("ACCESS_ERROR");
                        }
                        if (j < fields.length - 1) writer.write(", ");
                    }
                    writer.write("}");
                }
                writer.newLine();
            }
        } else {
            // Handle primitive or wrapper type arrays (e.g., int[], String[], etc.)
            int length = java.lang.reflect.Array.getLength(array);
            for (int i = 0; i < length; i++) {
                writer.write(java.lang.reflect.Array.get(array, i).toString() + ", ");
            }
        }
    }

    private static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() ||
               clazz.equals(Boolean.class) ||
               clazz.equals(Byte.class) ||
               clazz.equals(Character.class) ||
               clazz.equals(Short.class) ||
               clazz.equals(Integer.class) ||
               clazz.equals(Long.class) ||
               clazz.equals(Float.class) ||
               clazz.equals(Double.class);
    }
}
