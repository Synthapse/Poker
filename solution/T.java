import java.io.*;

public class T {
    public static void main(String[] args) {
        try {
            // Open the file for reading
            FileInputStream fileInputStream = new FileInputStream("decompressed_file_2");
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);

            // Deserialize the object
            Object deserializedObject = objectInputStream.readObject();

            // Output the deserialized object (this depends on the type of object)
            System.out.println("Deserialized object: " + deserializedObject);

            // Close the streams
            objectInputStream.close();
            fileInputStream.close();

        } catch (StreamCorruptedException e) {
            System.out.println("StreamCorruptedException: The stream is corrupted, or the format is not valid for deserialization.");
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFoundException: The class of the object could not be found.");
        } catch (IOException e) {
            System.out.println("IOException: An error occurred while reading the file.");
        }
    }
}