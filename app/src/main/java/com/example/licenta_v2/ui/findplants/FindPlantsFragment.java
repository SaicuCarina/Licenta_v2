package com.example.licenta_v2.ui.findplants;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.licenta_v2.R;
import com.example.licenta_v2.model.PlantDetailsResponse;

import java.io.ByteArrayOutputStream;

public class FindPlantsFragment extends Fragment {

    private Button identifyButton;
    private ProgressBar loadingBar;

    private FindPlantsViewModel viewModel;
    private ActivityResultLauncher<Intent> cameraLauncher;

    private static final int CAMERA_PERMISSION_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(FindPlantsViewModel.class);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Bitmap imageBitmap = (Bitmap) result.getData().getExtras().get("data");
                        if (imageBitmap != null) {
                            String base64Image = convertBitmapToBase64(imageBitmap);
                            viewModel.identifyPlant(base64Image);
                        }
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find_plants, container, false);

        identifyButton = view.findViewById(R.id.identify);
        loadingBar = new ProgressBar(requireContext(), null, android.R.attr.progressBarStyleLarge);
        loadingBar.setVisibility(View.GONE);
        ((ViewGroup) view).addView(loadingBar); // Adaugăm loadingBar în layout programatic

        identifyButton.setOnClickListener(v -> openCamera());

        observeViewModel();

        return view;
    }

    private void openCamera() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraLauncher.launch(takePictureIntent);
    }

    private void observeViewModel() {
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading) {
                loadingBar.setVisibility(View.VISIBLE);
                identifyButton.setEnabled(false);
            } else {
                loadingBar.setVisibility(View.GONE);
                identifyButton.setEnabled(true);
            }
        });

        viewModel.getPlantDetails().observe(getViewLifecycleOwner(), plant -> {
            if (plant != null) {
                showPlantDetails(plant);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPlantDetails(PlantDetailsResponse details) {
        String info = "Identified: " + details.getCommonName();
        Log.d("PlantAPI", "Details received: " + details.toString());
        Toast.makeText(requireContext(), info, Toast.LENGTH_LONG).show();
        // TODO: aici poți adăuga navigare spre un fragment de detalii în viitor
    }

    private String convertBitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] byteArray = baos.toByteArray();
        return "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
