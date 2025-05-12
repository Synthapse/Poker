package com.example;

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.google.cloud.storage.*;
import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MkrFileProcessor implements HttpFunction {

    private static final String BUCKET_NAME = "auxilia-poker";

    @Override
    public void service(HttpRequest request, HttpResponse response) throws Exception {
        String fileName = request.getFirstQueryParameter("file").orElse("");
        if (fileName.isEmpty()) {
            response.setStatusCode(400);
            response.getWriter().write("Missing 'file' query parameter.");
            return;
        }

        Storage storage = StorageOptions.getDefaultInstance().getService();
        Blob blob = storage.get(BUCKET_NAME, fileName);
        if (blob == null) {
            response.setStatusCode(404);
            response.getWriter().write("File not found in bucket.");
            return;
        }

        // Download the file to a temporary location
        Path tempFile = Files.createTempFile("mkr_", ".zip");
        blob.downloadTo(tempFile);

        // Extract the ZIP file
        Path extractDir = Files.createTempDirectory("mkr_extract_");
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(tempFile.toFile()))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path newFilePath = extractDir.resolve(entry.getName());
                if (entry.isDirectory()) {
                    Files.createDirectories(newFilePath);
                } else {
                    Files.createDirectories(newFilePath.getParent());
                    try (FileOutputStream fos = new FileOutputStream(newFilePath.toFile())) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }

        // Process extracted files as needed
        // For example, deserialize files, analyze content, etc.

        response.setStatusCode(200);
        response.getWriter().write("File processed successfully.");
    }
}
