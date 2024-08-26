//package com.example.audioandvideo;
//
//import android.Manifest;
//import android.content.Intent;
//import android.content.pm.PackageManager;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.core.content.ContextCompat;
//import androidx.fragment.app.Fragment;
//import androidx.navigation.fragment.NavHostFragment;
//
//import com.example.audioandvideo.databinding.FragmentFirstBinding;
//
//public class FirstFragment extends Fragment {
//
//    private FragmentFirstBinding binding;
//    private static final int PERMISSION_REQUEST_CODE = 1000;
//
//    @Override
//    public View onCreateView(
//            LayoutInflater inflater, ViewGroup container,
//            Bundle savedInstanceState
//    ) {
//
//        binding = FragmentFirstBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//
//    }
//
//    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (arePermissionsGranted()) {
//                    // If permissions are already granted, navigate to VideoCapture
//                    navigateToVideoCapture();
//                } else {
//                    // Request necessary permissions
//                    requestPermissions(new String[]{
//                            Manifest.permission.CAMERA,
//                            Manifest.permission.RECORD_AUDIO,
//                            Manifest.permission.WRITE_EXTERNAL_STORAGE
//                    }, PERMISSION_REQUEST_CODE);
//                }
//            }
//        });
//    } private boolean arePermissionsGranted() {
//        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
//                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
//    }
//
//    // Method to navigate to VideoCapture activity
//    private void navigateToVideoCapture() {
//        Intent intent = new Intent(getActivity(), VideoCapture.class);
//        startActivity(intent);
//    }
//
//    // Handle the result of the permission request
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_REQUEST_CODE) {
//            // Check if all permissions were granted
//            if (grantResults.length > 0 && allPermissionsGranted(grantResults)) {
//                // If all permissions are granted, navigate to VideoCapture
//                navigateToVideoCapture();
//            } else {
//                // If permissions are denied, show a message or handle accordingly
//                Toast.makeText(getContext(), "Permissions denied. Cannot start video capture.", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    // Helper method to check if all permissions were granted
//    private boolean allPermissionsGranted(int[] grantResults) {
//        for (int result : grantResults) {
//            if (result != PackageManager.PERMISSION_GRANTED) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//
//}