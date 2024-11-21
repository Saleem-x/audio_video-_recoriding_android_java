package com.example.audioandvideo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class VideoUploader {

    private static final int CHUNK_SIZE = 1024 * 1024; // 1MB chunk size
    private static final String UPLOAD_URL = "https://sfa-muom.dev-ts.online/WS/VideoUpload/UploadLargeFileChunk";
    private static final String UPLOAD_Audio_URL = "https://sfa-muom.dev-ts.online/WS/VideoUpload/PostAudioFile";
    private static final String BOUNDARY = "===" + System.currentTimeMillis() + "===";

    private static ExecutorService executorService;

    public VideoUploader() {
        executorService = Executors.newSingleThreadExecutor();
    }

    public void uploadVideoInChunks(File videoFile, String fileID, String userId, Context context) {
        executorService.submit(() -> {
            try {
                FileInputStream inputStream = new FileInputStream(videoFile);
                byte[] buffer = new byte[CHUNK_SIZE];

                long fileSize = videoFile.length();
                int totalChunks = (int) Math.ceil((double) fileSize / CHUNK_SIZE);
                int chunkIndex = 1;
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    HttpURLConnection connection = (HttpURLConnection) new URL(UPLOAD_URL).openConnection();
                    connection.setDoOutput(true);
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"FileID\"\r\n\r\n").getBytes());
                    outputStream.write((fileID + "\r\n").getBytes());

                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"ChunkOrder\"\r\n\r\n").getBytes());
                    outputStream.write((chunkIndex + "\r\n").getBytes());

                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"TotalChunks\"\r\n\r\n").getBytes());
                    outputStream.write((totalChunks + "\r\n").getBytes());

                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"FileName\"\r\n\r\n").getBytes());
                    outputStream.write((videoFile.getName() + "\r\n").getBytes());

                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"UserId\"\r\n\r\n").getBytes());
                    outputStream.write((userId + "\r\n").getBytes());

                    outputStream.write(("--" + BOUNDARY + "\r\n").getBytes());
                    outputStream.write(("Content-Disposition: form-data; name=\"FileChunk\"; filename=\"" + videoFile.getName() + "\"\r\n").getBytes());
                    outputStream.write(("Content-Type: application/octet-stream\r\n\r\n").getBytes());
                    outputStream.write(buffer, 0, bytesRead);
                    outputStream.write(("\r\n--" + BOUNDARY + "--\r\n").getBytes());

                    outputStream.flush();
                    outputStream.close();

                    int responseCode = connection.getResponseCode();
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        // Read the response
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder response = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            response.append(line);
                        }
                        reader.close();

//                        // Log the response
//                        Log.e("upload", "Chunk " + chunkIndex + " uploaded successfully.");
//                        Log.e("upload", "Response: " + response.toString());
//
//                        // Parse and log the response fields
//                        // Assume the response is in JSON format
//                        JSONObject jsonResponse = new JSONObject(response.toString());
//                        String res = jsonResponse.optString("Res");
//                        String title = jsonResponse.optString("Title");
//                        String descr = jsonResponse.optString("Descr");
//                        String returnId = jsonResponse.optString("ReturnId");
//
//                        while ((line = reader.readLine()) != null) {
//                            response.append(line);
//                        }
//                        reader.close();

// Log the response
                        Log.e("upload", "Chunk " + chunkIndex + " uploaded successfully.");
                        Log.e("upload", "Response: " + response.toString());

// Try to parse the response as JSON
                        String responseString = response.toString();
                        try {

                            JSONObject jsonResponse = new JSONObject(responseString);

                            // Parse the expected fields from the JSON response
                            String res = jsonResponse.optString("Res");
                            String title = jsonResponse.optString("Title");
                            String descr = jsonResponse.optString("Descr");
                            String returnId = jsonResponse.optString("ReturnId");

                            Log.e("upload", "Res: " + res + ", Title: " + title + ", Descr: " + descr + ", ReturnId: " + returnId);

                        } catch (org.json.JSONException e) {
                            // If the response is not JSON, log the plain response
                            Log.e("upload", "Response is not JSON. Received: " + responseString);
                        }

//                        Log.e("upload", "Res: " + res + ", Title: " + title + ", Descr: " + descr + ", ReturnId: " + returnId);

                    } else {
                        Log.e("upload", "Failed to upload chunk " + chunkIndex + ", response code: " + responseCode);
                        break;
                    }

                    connection.disconnect();
                    chunkIndex++;
                }

                inputStream.close();
                Log.e("upload", "Video upload completed!");

                // Show Toast after completion
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Video upload completed!", Toast.LENGTH_SHORT).show();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("upload", "Error: " + e.getMessage());
            }
        });
    }

    static void uploadAudioFile(String filePath, String userID, String remarks,Context context) {
        executorService.submit(() -> {
        HttpURLConnection connection = null;
        DataOutputStream outputStream = null;
        FileInputStream fileInputStream = null;

        String boundary = "*****"; // Boundary for multipart form data
        String lineEnd = "\r\n";
        String twoHyphens = "--";

        try {
            File audioFile = new File(filePath);
            fileInputStream = new FileInputStream(audioFile);
            String audioCode = audioFile.getName(); // AudioCode is now the audio file name

            URL url = new URL(UPLOAD_Audio_URL);
            connection = (HttpURLConnection) url.openConnection();

            // Allow input/output
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);

            // Set request method and headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            outputStream = new DataOutputStream(connection.getOutputStream());

            // Add AudioCode (audio file name)
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"AudioCode\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(audioCode + lineEnd);

            // Add UserID field
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"UserID\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(userID + lineEnd);

            // Add Remarks field
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"Remarks\"" + lineEnd);
            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(remarks + lineEnd);

            // Add audio file
            outputStream.writeBytes(twoHyphens + boundary + lineEnd);
            outputStream.writeBytes("Content-Disposition: form-data; name=\"audioFile\";filename=\"" + audioFile.getName() + "\"" + lineEnd);
            outputStream.writeBytes(lineEnd);

            // Write file content
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.writeBytes(lineEnd);
            outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            // Get server response
            int serverResponseCode = connection.getResponseCode();
            String serverResponseMessage = connection.getResponseMessage();

            if (serverResponseCode == HttpURLConnection.HTTP_OK) {
                // Handle success response
                System.out.println("File uploaded successfully: " + serverResponseMessage);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Audio upload completed!", Toast.LENGTH_SHORT).show();
                    });
                }

            } else {
                // Handle failure response
                System.out.println("Failed to upload file: " + serverResponseMessage);
                if (context instanceof Activity) {
                    ((Activity) context).runOnUiThread(() -> {
                        Toast.makeText(context, "Audio upload Failed!", Toast.LENGTH_SHORT).show();
                    });
                }

            }

        } catch (MalformedURLException e) {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    Toast.makeText(context, "Audio upload failed!", Toast.LENGTH_SHORT).show();
                });
            }

            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) fileInputStream.close();
                if (outputStream != null) outputStream.flush();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();

                Toast.makeText(context, "Audio upload failed!", Toast.LENGTH_SHORT).show();
            }
        }});
    }
}
