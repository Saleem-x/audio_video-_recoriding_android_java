package com.example.audioandvideo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_CAPTURE = 1;
    private static final int PERMISSION_REQUEST_CODE = 100;

    private MediaRecorder mediaRecorder;
    private MediaPlayer mediaPlayer;
    private String audioOutputFilePath;
    private String videoOutputFilePath;
    private boolean isRecording = false;

    private VideoView videoView;
    private Button playPauseButton;
    private RelativeLayout videoContainer;
    private boolean isVideoPlaying = false;

    private Handler timerHandler;
    private Runnable timerRunnable;
    private long startTime;
    private AlertDialog audioRecordDialog;
    private TextView timerTextView;
    private ImageView audioWaveImageView;
    private Uri recordedVideoUri;
    private Button uploadAudioButton;

    private long pauseTime;
    private boolean isPaused = false;

String videoStoredPath="";
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button recordVideoButton = findViewById(R.id.recordVideoButton);
        Button recordAudioButton = findViewById(R.id.recordAudioButton);
//        Button playAudioButton = findViewById(R.id.playAudioButton);
        Button playVideoButton = findViewById(R.id.playVideoButton);
        videoView = findViewById(R.id.videoView);
        videoContainer = findViewById(R.id.videoContainer);
        playPauseButton = findViewById(R.id.playPauseButton);
        uploadAudioButton=findViewById(R.id.uploadAudioButton);
        uploadAudioButton.setVisibility(View.GONE);
        playVideoButton.setOnClickListener(v -> {
            if (recordedVideoUri != null) {
                videoContainer.setVisibility(View.VISIBLE);  // Show the video container
                videoView.setVideoURI(recordedVideoUri);
                videoView.start();
            }
        });

        uploadAudioButton.setOnClickListener(v -> {
            if (audioOutputFilePath != null) {
                VideoUploader videoUploader = new VideoUploader();
                videoUploader.uploadAudioFile(audioOutputFilePath,"4","",this);
            }
        });
        Button uploadVideoButton = findViewById(R.id.uploadVideoButton);

        // Set the OnClickListener
//        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // Call your function when the button is clicked
//                VideoUploader videoUploader = new VideoUploader();  // Create an instance of VideoUploader
//                videoUploader.uploadVideoInChunks(new File(videoStoredPath),"1","4",this);
//
//            }
//        });
        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                VideoUploader videoUploader = new VideoUploader();
                videoUploader.uploadVideoInChunks(new File(videoStoredPath), "1", "4", MainActivity.this);
            }
        });

        checkPermissions();
        requestPermissions();
        setupButtons();
        if (checkPermissions()) {

        } else {

        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        // First request the normal runtime permissions
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, PERMISSION_REQUEST_CODE);

        // For Android 11 and above, handle MANAGE_EXTERNAL_STORAGE separately
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if the MANAGE_EXTERNAL_STORAGE permission is granted
            if (!Environment.isExternalStorageManager()) {
                // Direct the user to the settings page to manually enable the permission
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (checkPermissions()) {
                setupButtons();
            } else {
                Toast.makeText(this, "Permissions are required to use this app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setupButtons() {
        Button recordVideoButton = findViewById(R.id.recordVideoButton);
        Button recordAudioButton = findViewById(R.id.recordAudioButton);
//        Button playAudioButton = findViewById(R.id.playAudioButton);
        Button playVideoButton = findViewById(R.id.playVideoButton);

        recordVideoButton.setOnClickListener(v -> startVideoCapture());
        recordAudioButton.setOnClickListener(v -> showAudioRecordDialog());
        playPauseButton.setOnClickListener(v -> togglePlayPauseVideo());
        playVideoButton.setOnClickListener(v -> playVideo());
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startVideoCapture() {
//        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
//        } else {
//            Toast.makeText(this, "No app available to capture video", Toast.LENGTH_SHORT).show();
//        }

        showVideoSourceDialog();
    }
    private void playVideo() {
        Button playVideoButton = findViewById(R.id.playVideoButton);

        if (videoContainer.getVisibility() == View.VISIBLE) {
            // Hide video container and stop the video
            videoContainer.setVisibility(View.INVISIBLE);
            videoView.stopPlayback();  // Stop video playback
            playVideoButton.setText("Play Video");  // Change button text back to "Play Video"
        } else {
            // Show video container and play the video
            videoContainer.setVisibility(View.VISIBLE);
            videoView.setVideoURI(recordedVideoUri);
            videoView.setOnPreparedListener(mp -> {
                videoView.start();
                Toast.makeText(this, "Playing video", Toast.LENGTH_SHORT).show();
            });
            playVideoButton.setText("Stop Playing");  // Change button text to "Stop Playing"
        }
    }


//    private void showAudioRecordDialog() {
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.dialog_audio_record, null);
//        builder.setView(dialogView);
//
//        timerTextView = dialogView.findViewById(R.id.timerTextView);
//        audioWaveImageView = dialogView.findViewById(R.id.audioWaveImageView);
//        Button stopRecordingButton = dialogView.findViewById(R.id.stopRecordingButton);
//        Button playAudioButtonInDialog = dialogView.findViewById(R.id.playAudioButtonInDialog);
//
//        audioRecordDialog = builder.create();
//        audioRecordDialog.show();
//
//        // Initialize state
//        isRecording = true; // Assume recording starts when dialog is shown
//        stopRecordingButton.setText("Stop Recording");
//
//        startAudioRecording();
//        startTimer();
//
//        stopRecordingButton.setOnClickListener(v -> {
//            if (isRecording) {
//                stopAudioRecording();
//                pauseTimer();
//                stopRecordingButton.setText("Cancel");
//                isRecording = false;
//            } else {
//                audioRecordDialog.dismiss();
//            }
//        });
//
//        playAudioButtonInDialog.setOnClickListener(v -> playAudio());
//    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void startAudioRecording() {
        // Define the SFA folder path
        File sfaFolder = new File(Environment.getExternalStorageDirectory(), "sfa");

        // Check if the SFA folder exists, if not create it
        if (!sfaFolder.exists()) {
            if (sfaFolder.mkdirs()) {
//                Toast.makeText(this, "SFA folder created", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "Failed to create SFA folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Define the 'audio' subfolder path inside the SFA folder
        File audioFolder = new File(sfaFolder, "audio");

        // Check if the audio subfolder exists, if not create it
        if (!audioFolder.exists()) {
            if (audioFolder.mkdirs()) {
//                Toast.makeText(this, "'audio' folder created inside SFA", Toast.LENGTH_SHORT).show();
            } else {
//                Toast.makeText(this, "Failed to create 'audio' folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Generate a unique filename based on the current timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "audiofile_" + timeStamp + ".3gp";

        // Define the full file path for the audio file inside the 'audio' subfolder
        audioOutputFilePath = new File(audioFolder, fileName).getAbsolutePath();

        // Set up the MediaRecorder
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mediaRecorder.setOutputFile(audioOutputFilePath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            Toast.makeText(this, "Recording started", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Recording failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void stopAudioRecording() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            uploadAudioButton.setVisibility(View.VISIBLE);
//            Toast.makeText(this, "Recording stopped. File saved to:\n" + audioOutputFilePath, Toast.LENGTH_LONG).show();
        }
    }

    private void playAudio() {
        if (audioOutputFilePath != null && !audioOutputFilePath.isEmpty()) {
            try {
                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                } else {
                    mediaPlayer.reset();
                }

                mediaPlayer.setDataSource(audioOutputFilePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                audioProgressBar.setVisibility(View.VISIBLE);
                // Set the ProgressBar max to the duration of the audio
                audioProgressBar.setMax(mediaPlayer.getDuration());

                // Create a handler to update the ProgressBar
                final Handler progressHandler = new Handler();
                Runnable updateProgressRunnable = new Runnable() {
                    @Override
                    public void run() {
                        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                            audioProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                            progressHandler.postDelayed(this, 100);
                        }
                    }
                };

                 progressHandler.post(updateProgressRunnable);

                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show();

                // Handle completion of audio playback
                mediaPlayer.setOnCompletionListener(mp -> {
                    progressHandler.removeCallbacks(updateProgressRunnable);
                    audioProgressBar.setProgress(0); // Reset progress bar after playback
                    Toast.makeText(this, "Playback finished", Toast.LENGTH_SHORT).show();
                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Playback failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No audio file found", Toast.LENGTH_SHORT).show();
        }
    }

    private ProgressBar audioProgressBar;

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showAudioRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_audio_record, null);
        builder.setView(dialogView);

        timerTextView = dialogView.findViewById(R.id.timerTextView);
        audioProgressBar = dialogView.findViewById(R.id.audioProgressBar); // Initialize the ProgressBar
        Button stopRecordingButton = dialogView.findViewById(R.id.stopRecordingButton);
        Button playAudioButtonInDialog = dialogView.findViewById(R.id.playAudioButtonInDialog);
        audioProgressBar.setVisibility(View.GONE);
        audioRecordDialog = builder.create();
        audioRecordDialog.show();

        // Initialize state
        isRecording = true; // Assume recording starts when dialog is shown
        stopRecordingButton.setText("Stop Recording");

        startAudioRecording();
        startTimer(); // Start the timer when recording starts

        stopRecordingButton.setOnClickListener(v -> {
            if (isRecording) {
                stopAudioRecording();
                pauseTimer();
                stopRecordingButton.setText("Cancel");
                isRecording = false;
            } else {
                audioRecordDialog.dismiss();
                mediaPlayer.stop();

            }
        });

        playAudioButtonInDialog.setOnClickListener(v -> playAudio());
    }
 
    private void startTimer() {
        timerHandler = new Handler();
        startTime = System.currentTimeMillis();

        timerRunnable = new Runnable() {
            @Override
            public void run() {
                long elapsedMillis = System.currentTimeMillis() - startTime;
                int seconds = (int) (elapsedMillis / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                // Update the timer TextView
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));

                // Calculate progress and update the ProgressBar
                int progress = (int) (elapsedMillis / (float) 10 * 100);
                audioProgressBar.setProgress(progress);

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }


    private void pauseTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
        audioProgressBar.setProgress(0); // Reset progress if needed
    }
    private void showVideoContainer() {
        videoContainer.setVisibility(View.VISIBLE);
    }

    private void togglePlayPauseVideo() {
        if (isVideoPlaying) {
            videoView.pause();
            playPauseButton.setText("Play");
        } else {
            videoView.start();
            playPauseButton.setText("Pause");
        }
        isVideoPlaying = !isVideoPlaying;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
//            Uri videoUri = data.getData();
//            if (videoUri != null) {
////                // Set the video URI directly to the VideoView
////                videoView.setVideoURI(videoUri);
////                videoView.setOnPreparedListener(mp -> {
////                    videoView.start();
////                    Toast.makeText(this, "Playing video", Toast.LENGTH_SHORT).show();
////                });
//
//                recordedVideoUri = data.getData();
//                compressVideo(videoUri);
////                Toast.makeText(this, "No app ava
////                ilable to capture video", Toast.LENGTH_SHORT).show();
//
//            } else {
//                Toast.makeText(this, "Video capture failed", Toast.LENGTH_SHORT).show();
//            }
//        } else if (resultCode == RESULT_CANCELED) {
//            Toast.makeText(this, "Video recording canceled", Toast.LENGTH_SHORT).show();
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_VIDEO_CAPTURE) {
                // Handle recorded video
                Toast.makeText(this, "Video recorded successfully", Toast.LENGTH_SHORT).show();

                Uri videoUri = data.getData();
                saveVideoToCustomFolder(videoUri);
            } else if (requestCode == REQUEST_VIDEO_CAPTURE) {
                // Handle video selected from gallery

                Uri videoUri = data.getData();
                saveVideoToCustomFolder(videoUri);
            }
        } else if (resultCode == RESULT_CANCELED) {
            Toast.makeText(this, "Action canceled", Toast.LENGTH_SHORT).show();
        }
    }



    private void compressVideo(File inputFile) {
        try {
            // Output path - overwrite the original video file
            File originalFile =   inputFile;
            String compressedVideoPath = originalFile.getAbsolutePath();

            // FFmpeg command to compress the video and overwrite the original file
            String[] cmd = new String[]{
                    "-y",  // Overwrite output files
                    "-i", inputFile.getAbsolutePath(),  // Input file
                    "-vcodec", "libx264",  // Video codec
                    "-crf", "30",  // Compression factor (30% compression)
                    "-preset", "ultrafast",  // Compression speed
                    compressedVideoPath  // Output file path (same as input)
            };

            // Execute FFmpeg command
            int result = FFmpeg.execute(cmd);

            // Check if the compression was successful
            if (result == Config.RETURN_CODE_SUCCESS) {
                Log.d("VideoCompressor", "Compression successful! Compressed video saved at: " + compressedVideoPath);
                Toast.makeText(this, "Compression successful! Compressed video saved at: " + compressedVideoPath, Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Compression failed with return code: " + result, Toast.LENGTH_SHORT).show();

                Log.e("VideoCompressor", "Compression failed with return code: " + result);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("VideoCompressor", "Error during compression: " + e.getMessage());
            Toast.makeText(this, "Error during compression: " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }



    // Helper function to get the file path from a Uri
    private String getRealPathFromUri(Uri contentUri) {
        String[] proj = { MediaStore.Video.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    private void showVideoSourceDialog() {
        String[] options = {"Record Video", "Choose from Gallery"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Video Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Record Video
                        recordVideo();
                    } else if (which == 1) {
                        // Choose from Gallery
                        chooseVideoFromGallery();
                    }
                })
                .show();
    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recordVideo() {
        // Define the SFA folder path
        File sfaFolder = new File(Environment.getExternalStorageDirectory(), "sfa");

        // Check if the SFA folder exists, if not create it
        if (!sfaFolder.exists()) {
            if (sfaFolder.mkdirs()) {
                Toast.makeText(this, "SFA folder created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create SFA folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Define the 'video' subfolder path inside the SFA folder
        File videoFolder = new File(sfaFolder, "video");

        // Check if the video subfolder exists, if not create it
        if (!videoFolder.exists()) {
            if (videoFolder.mkdirs()) {
                Toast.makeText(this, "'video' folder created inside SFA", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Failed to create 'video' folder", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Generate a unique filename based on the current timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "videofile_" + timeStamp + ".mp4";

        // Define the full file path for the video file inside the 'video' subfolder
        File videoFile = new File(videoFolder, fileName);

        // Create an intent to record video
        Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);

        // Use FileProvider to get the Uri for the video file
        Uri videoUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", videoFile);

        // Pass the Uri to the video intent
        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri);
        recordedVideoUri =videoUri;
        // Start the video recording activity
        startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
    }

    private void chooseVideoFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        intent.setType("video/*");
        startActivityForResult(intent, REQUEST_VIDEO_CAPTURE);
    }


    private void saveVideoToCustomFolder(Uri videoUri) {
        try {
            String destinationPath = getExternalFilesDir(null).getAbsolutePath() + "/sfa/audio";
            File destinationDir = new File(destinationPath);

            if (!destinationDir.exists()) {
                destinationDir.mkdirs();
            }

            File destinationFile = new File(destinationDir, "video_" + System.currentTimeMillis() + ".mp4");

            InputStream inputStream = getContentResolver().openInputStream(videoUri);
            OutputStream outputStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }

            inputStream.close();
            outputStream.close();
            compressVideo(destinationFile);
            Button uploadVideoButton = findViewById(R.id.uploadVideoButton);
            uploadVideoButton.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Video saved to: " + destinationFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
            videoStoredPath=destinationFile.getAbsolutePath();
            // Compress the video


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save video", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
    }
}
