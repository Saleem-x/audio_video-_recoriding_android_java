package com.example.audioandvideo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;

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

    private long pauseTime;
    private boolean isPaused = false;


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

        if (checkPermissions()) {
            setupButtons();
        } else {
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, PERMISSION_REQUEST_CODE);
    }

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

    private void setupButtons() {
        Button recordVideoButton = findViewById(R.id.recordVideoButton);
        Button recordAudioButton = findViewById(R.id.recordAudioButton);
//        Button playAudioButton = findViewById(R.id.playAudioButton);
        Button playVideoButton = findViewById(R.id.playVideoButton);

        recordVideoButton.setOnClickListener(v -> startVideoCapture());
        recordAudioButton.setOnClickListener(v -> showAudioRecordDialog());
        playPauseButton.setOnClickListener(v -> togglePlayPauseVideo());
    }

    private void startVideoCapture() {
        Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeVideoIntent, REQUEST_VIDEO_CAPTURE);
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

    private void startAudioRecording() {
        mediaRecorder = new MediaRecorder();
        audioOutputFilePath = getExternalFilesDir(null) + "/audiofile.3gp";

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
            Toast.makeText(this, "Recording stopped. File saved to:\n" + audioOutputFilePath, Toast.LENGTH_LONG).show();
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
                Toast.makeText(this, "Playing audio", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Playback failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No audio file found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAudioRecordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_audio_record, null);
        builder.setView(dialogView);

        timerTextView = dialogView.findViewById(R.id.timerTextView);
        audioWaveImageView = dialogView.findViewById(R.id.audioWaveImageView);
        Button stopRecordingButton = dialogView.findViewById(R.id.stopRecordingButton);
        Button playAudioButtonInDialog = dialogView.findViewById(R.id.playAudioButtonInDialog);

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

                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));

                timerHandler.postDelayed(this, 1000);
            }
        };
        timerHandler.post(timerRunnable);
    }

    private void pauseTimer() {
        if (timerHandler != null && timerRunnable != null) {
            timerHandler.removeCallbacks(timerRunnable);
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_VIDEO_CAPTURE && resultCode == RESULT_OK) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                videoOutputFilePath = getRealPathFromURI(videoUri);
                videoView.setVideoURI(videoUri);
                videoView.setOnPreparedListener(mp -> videoView.start());
            }
        }
    }

    private String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            return uri.getPath();
        }
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
        return cursor.getString(idx);
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
