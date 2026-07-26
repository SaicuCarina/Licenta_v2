package com.example.licenta_v2.ui.profile;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.licenta_v2.R;
import com.example.licenta_v2.ui.myPlants.MyPlantsFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.Map;

public class QRScanFragment extends Fragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public QRScanFragment() {
        super(R.layout.fragment_qr_scan);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        startQRScan();
    }

    private void startQRScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            launchScanner();
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    launchScanner();
                } else if (isAdded()) {
                    Toast.makeText(requireContext(), "Camera permission required", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<ScanOptions> scanLauncher = registerForActivityResult(
            new ScanContract(),
            result -> {
                if (isAdded() && result.getContents() != null) {
                    importPlantsFromUser(result.getContents());
                }
            });

    private void launchScanner() {
        if (!isAdded()) return;

        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan QR Code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCameraId(0);
        scanLauncher.launch(options);
    }

    private void importPlantsFromUser(String otherUserUid) {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null || !isAdded()) return;

        String currentUid = currentUser.getUid();

        CollectionReference source = db.collection("users").document(otherUserUid).collection("myPlants");
        CollectionReference target = db.collection("users").document(currentUid).collection("myPlants");

        source.get().addOnSuccessListener(querySnapshot -> {
            for (QueryDocumentSnapshot doc : querySnapshot) {
                String plantId = doc.getId();
                Map<String, Object> plantData = doc.getData();

                plantData.put("sharedFrom", otherUserUid);
                plantData.put("originalOwnerId", otherUserUid);

                target.document(plantId).set(plantData);
            }

            db.collection("users")
                    .document(otherUserUid)
                    .update("delegatedTo", FieldValue.arrayUnion(currentUid));

            if (isAdded()) {
                Toast.makeText(requireContext(), "The plants have been transferred!", Toast.LENGTH_LONG).show();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame_layout, new MyPlantsFragment())
                        .commit();

                requireActivity().findViewById(R.id.bottomNavigationView)
                        .post(() -> {
                            BottomNavigationView nav = requireActivity().findViewById(R.id.bottomNavigationView);
                            nav.setSelectedItemId(R.id.myPlants);
                        });
            }

        }).addOnFailureListener(e -> {
            if (isAdded()) {
                Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (isAdded()) {
            requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
}
