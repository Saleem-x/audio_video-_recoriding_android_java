//package com.example.audioandvideo;
//
//import android.Manifest;
//import android.content.pm.PackageManager;
//import android.media.MediaRecorder;
//import android.os.Bundle;
//import android.os.Environment;
//import android.view.SurfaceHolder;
//import android.view.SurfaceView;
//import android.view.View;
//import android.widget.Button;
//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.app.ActivityCompat;
//import androidx.core.content.ContextCompat;
//import java.io.File;
//import java.io.IOException;
//
//public class VideoCapture extends AppCompatActivity {
//
//    private static final int REQUEST_PERMISSIONS = 1;
//    private MediaRecorder mediaRecorder;
//    private SurfaceView surfaceView;
//    private String videoPath;
//    private Button startButton;
//    private Button stopButton;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        surfaceView = findViewById(R.id.surfaceView);
//        startButton = findViewById(R.id.startButton);
//        stopButton = findViewById(R.id.stopButton);
//
//        if (!checkPermissions()) {
//            requestPermissions();
//        }
//
//        startButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startRecording();
//            }
//        });
//
//        stopButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                stopRecording();
//            }
//        });
//    }
//
//    private boolean checkPermissions() {
//        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    private void requestPermissions() {
//        ActivityCompat.requestPermissions(this, new String[]{
//                Manifest.permission.CAMERA,
//                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//        }, REQUEST_PERMISSIONS);
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_PERMISSIONS) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                // Permissions granted
//            } else {
//                // Permissions denied
//            }
//        }
//    }
//
//    private void startRecording() {
//        if (mediaRecorder != null) {
//            mediaRecorder.release();
//            mediaRecorder = null;
//        }
//
//        mediaRecorder = new MediaRecorder();
//        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
//        mediaRecorder.setProfile(MediaRecorder.VideoEncoder.MPEG_4_SP);
//
//        videoPath = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/video.mp4";
//        mediaRecorder.setOutputFile(videoPath);
//        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface());
//
//        try {
//            mediaRecorder.prepare();
//            mediaRecorder.start();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void stopRecording() {
//        if (mediaRecorder != null) {
//            mediaRecorder.stop();
//            mediaRecorder.release();
//            mediaRecorder = null;
//        }
//    }
//}
