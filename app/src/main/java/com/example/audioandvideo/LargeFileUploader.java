package com.example.audioandvideo;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class LargeFileUploader {

    private static final String UPLOAD_URL = "http://yourserver.com/api/UploadLargeFileChunk";
    private static final String UPLOAD_Audio_URL = "http://yourserver.com/api/VideoUpload/PostAudioFile";

    private static final int CHUNK_SIZE = 1024 * 1024; // 1 MB chunk size

//    public static void main(String[] args) {
//        String filePath = "C:/path_to_your_large_video_file.mp4";
//        String fileID = "123"; // Unique file identifier
//        String userId = "456"; // User ID
//
//        try {
//            uploadLargeFile(filePath, fileID, userId);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

        void uploadLargeFile(String filePath, String fileID, String userId) throws IOException {
        File file = new File(filePath);
        long totalFileSize = file.length();
        String fileName = file.getName();

        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] buffer = new byte[CHUNK_SIZE];
            int bytesRead;
            int chunkOrder = 1;
            long uploadedSize = 0;

            while ((bytesRead = fileInputStream.read(buffer)) > 0) {
                boolean isLastChunk = (uploadedSize + bytesRead) >= totalFileSize;
                sendChunk(fileID, fileName, userId, chunkOrder, buffer, bytesRead, isLastChunk);
                chunkOrder++;
                uploadedSize += bytesRead;
                System.out.println("Uploaded chunk: " + chunkOrder);
            }
        }
    }

    private static void sendChunk(String fileID, String fileName, String userId, int chunkOrder, byte[] chunkData, int chunkSize, boolean isLastChunk) throws IOException {
        URL url = new URL(UPLOAD_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "multipart/form-data");

        try (DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream())) {
            // Write form data
            outputStream.writeBytes("--boundary\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"FileID\"\r\n\r\n" + fileID + "\r\n");
            outputStream.writeBytes("--boundary\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"ChunkOrder\"\r\n\r\n" + chunkOrder + "\r\n");
            outputStream.writeBytes("--boundary\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"FileName\"\r\n\r\n" + fileName + "\r\n");
            outputStream.writeBytes("--boundary\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"UserId\"\r\n\r\n" + userId + "\r\n");

            // Add the chunk itself
            outputStream.writeBytes("--boundary\r\n");
            outputStream.writeBytes("Content-Disposition: form-data; name=\"FileChunk\"; filename=\"" + fileName + "chunk" + chunkOrder + "\"\r\n");
            outputStream.writeBytes("Content-Type: application/octet-stream\r\n\r\n");
            outputStream.write(chunkData, 0, chunkSize);
            outputStream.writeBytes("\r\n--boundary--\r\n");

            outputStream.flush();

            // Get response from the server
            int responseCode = connection.getResponseCode();
            System.out.println("Chunk upload response code: " + responseCode);
        }
    }


}