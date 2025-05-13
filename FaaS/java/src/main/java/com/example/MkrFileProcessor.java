package com.example;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.api.gax.paging.Page;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.ReadableByteChannel; // Correct import for ReadableByteChannel
import java.nio.channels.WritableByteChannel; // Correct import for WritableByteChannel
import com.google.common.io.ByteStreams;  // Correct import for ByteStreams

public class MkrFileProcessor implements HttpFunction {

    private static final String BUCKET_NAME = "auxilia-poker";
    private static final String JDESERIALIZE_JAR_PATH = "tools/jdeserialize-1.2.jar"; // The path to JAR in the bucket
    private static final String TEMP_JAR_PATH = "/tmp/jdeserialize-1.2.jar"; // Temp location for JAR in Cloud Functions

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        response.appendHeader("Access-Control-Allow-Origin", "*"); // Or restrict to specific origin
        response.appendHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        response.appendHeader("Access-Control-Allow-Headers", "Content-Type");

        // Handle preflight (OPTIONS) request
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatusCode(204); // No Content
            return;
        }

        String folderName = request.getFirstQueryParameter("file").orElse("");
        if (folderName.isEmpty()) {
            response.setStatusCode(400);
            response.getWriter().write("Missing 'file' query parameter.");
            return;
        }

        Storage storage = StorageOptions.getDefaultInstance().getService();
        Page<Blob> blobsInFolder = storage.list(BUCKET_NAME, Storage.BlobListOption.prefix(folderName));
        StringBuilder overallOutput = new StringBuilder();

        for (Blob blob : blobsInFolder.iterateAll()) {
            // Skip "directory" placeholder blobs
            if (blob.isDirectory()) continue;

            // if (blob == null) {
            //     response.setStatusCode(404);
            //     response.getWriter().write("File not found in bucket.");
            // }

            Path tempFile = Files.createTempFile("mkr_", ".dat");
            blob.downloadTo(tempFile);
            File inputFile = tempFile.toFile();

            StringBuilder outputBuilder = new StringBuilder();
            File outputFile = new File("output.txt");
            BufferedWriter writer = null;
            writer = new BufferedWriter(new FileWriter(outputFile));

            if (inputFile.isFile()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile))) {
                    Object obj = ois.readObject();

                    outputBuilder.append("->").append(blob.getName()).append("\n");

                    if (obj instanceof Object[]) {
                        processObject(obj, writer, inputFile);
                    }
                    else if (obj instanceof int[]) {
                        outputBuilder.append(Arrays.toString((int[]) obj));
                    } else if (obj instanceof double[]) {
                        outputBuilder.append(Arrays.toString((double[]) obj));
                    } else if (obj instanceof long[]) {
                        outputBuilder.append(Arrays.toString((long[]) obj));
                    } else if (obj instanceof float[]) {
                        outputBuilder.append(Arrays.toString((float[]) obj));
                    } else if (obj instanceof boolean[]) {
                        outputBuilder.append(Arrays.toString((boolean[]) obj));
                    } else if (obj instanceof char[]) {
                        outputBuilder.append(Arrays.toString((char[]) obj));
                    } else if (obj instanceof short[]) {
                        outputBuilder.append(Arrays.toString((short[]) obj));
                    } else if (obj instanceof Object[]) {
                        outputBuilder.append(Arrays.toString((Object[]) obj));
                    } else if (obj instanceof List || obj instanceof Set || obj instanceof Map || obj instanceof String) {
                        outputBuilder.append(obj.toString());
                    } else {
                        outputBuilder.append("Unknown object type: ").append(obj.getClass().getName()).append("\n");
                        outputBuilder.append(obj.toString());
                    }

                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                    
                    //try {
                    overallOutput.append("Error during deserialization of file: " + blob.getName() + "\n" + e.getMessage());
                        //response.getWriter().write("Error during deserialization of file: " + blob.getName() + "\n" + e.getMessage());
                    // } catch (IOException ioException) {
                    //     ioException.printStackTrace(); // Avoid swallowing secondary exceptions
                    // }

                    // Attempt fallback via .jar function
                    System.out.println("Attempting to download .jar from Cloud Storage...");
                    downloadJarFromCloudStorage();
                    System.out.println(".jar download completed.");

                    System.out.println("Running fallback command...");
                    String output = runCommand(inputFile);
                    overallOutput.append(output).append("\n");
                    System.out.println("Fallback command executed.");

                    String relativeName = blob.getName().substring(folderName.length());
                    String outputBlobName = folderName + "deserialized/" + relativeName + ".txt";
                    BlobId outputBlobId = BlobId.of(BUCKET_NAME, outputBlobName);
                    BlobInfo outputBlobInfo = BlobInfo.newBuilder(outputBlobId)
                        .setContentType("text/plain")
                        .build();
                    
                    // Save fallback output
                    storage.create(outputBlobInfo, outputBuilder.toString().getBytes(StandardCharsets.UTF_8));

                    overallOutput.append("Processed file (fallback): ")
                                .append(blob.getName())
                                .append(" -> ")
                                .append(outputBlobName)
                                .append("\n");

                    continue;
                }
            }

            // Save output to bucket as a text file
            String relativeName = blob.getName().substring(folderName.length());
            String outputBlobName = folderName + "deserialized/" + relativeName + ".txt";
            BlobId outputBlobId = BlobId.of(BUCKET_NAME, outputBlobName);
            BlobInfo outputBlobInfo = BlobInfo.newBuilder(outputBlobId)
                .setContentType("text/plain")
                .build();

            storage.create(outputBlobInfo, outputBuilder.toString().getBytes(StandardCharsets.UTF_8));

            overallOutput.append("Processed file: ").append(blob.getName()).append(" -> ").append(outputBlobName).append("\n");
        }

        response.setStatusCode(200);
        response.getWriter().write("All files processed. Details:\n" + overallOutput.toString());
    }

    // Method to download the JAR file from Cloud Storage
    private void downloadJarFromCloudStorage() throws IOException {
        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(BUCKET_NAME, JDESERIALIZE_JAR_PATH);

        if (blob == null) {
            throw new IOException("JAR file not found in Cloud Storage.");
        }

        // Download the JAR file to the temporary location
        try (ReadableByteChannel readChannel = blob.reader();
            WritableByteChannel writeChannel = Files.newByteChannel(Paths.get(TEMP_JAR_PATH), StandardOpenOption.CREATE, StandardOpenOption.WRITE)) {
            ByteStreams.copy(readChannel, writeChannel);
        }
    }

    // Method to run the JAR file using ProcessBuilder
    private String runCommand(File inputFile) throws IOException, InterruptedException {
        String[] command = {
            "java", "-jar", TEMP_JAR_PATH, inputFile.getAbsolutePath(),
            "|", "grep", "-oE", "[0-9]+"
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true); // Merge error and output streams

        Process process = processBuilder.start();
        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("Error running command. Exit code: " + exitCode);
        }

        return output.toString(); // Return the output of the command
    }


    private static void processObject(Object obj, BufferedWriter writer, File file) throws IOException {
        writer.write("(Object[]): " + file.getName());
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
