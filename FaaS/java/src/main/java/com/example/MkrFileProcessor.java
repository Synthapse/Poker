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

public class MkrFileProcessor implements HttpFunction {

    private static final String BUCKET_NAME = "auxilia-poker";

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

            if (inputFile.isFile()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(inputFile))) {
                    Object obj = ois.readObject();

                    outputBuilder.append("Deserialized object from file: ").append(blob.getName()).append("\n");

                    if (obj instanceof int[]) {
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
                    response.setStatusCode(500);
                    response.getWriter().write("Error during deserialization of file: " + blob.getName() + "\n" + e.getMessage());
                    return;
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
}
