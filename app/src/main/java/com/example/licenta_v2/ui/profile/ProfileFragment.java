package com.example.licenta_v2.ui.profile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.licenta_v2.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView profileImageView;
    private TextView nameTextView, emailTextView, addPhotoText;
    private TextView shareStatusTextView;
    private Uri imageUri;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        profileImageView = view.findViewById(R.id.profileImageView);
        nameTextView = view.findViewById(R.id.nameTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        addPhotoText = view.findViewById(R.id.addPhotoText);
        shareStatusTextView = view.findViewById(R.id.shareStatusTextView);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        loadUserProfile();

        ImageView qrImageView = view.findViewById(R.id.qrImageView);
        generateQRCode(qrImageView);

        addPhotoText.setOnClickListener(v -> openImageChooser());

        Button scanQrButton = view.findViewById(R.id.scanQrButton);
        scanQrButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setCustomAnimations(
                            R.anim.slide_in_right,  // dacă ai animații
                            R.anim.slide_out_left,
                            R.anim.slide_in_left,
                            R.anim.slide_out_right
                    )
                    .replace(R.id.frame_layout, new QRScanFragment())
                    .addToBackStack(null)
                    .commit();
        });

        Button recoverPlantsButton = view.findViewById(R.id.recoverPlantsButton);
        recoverPlantsButton.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();
            if (user == null) return;

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(snapshot -> {
                        List<String> delegatedTo = (List<String>) snapshot.get("delegatedTo");
                        if (delegatedTo != null) {
                            for (String uid : delegatedTo) {
                                db.collection("users").document(uid).collection("myPlants")
                                        .get().addOnSuccessListener(docs -> {
                                            for (QueryDocumentSnapshot doc : docs) {
                                                Map<String, Object> plant = doc.getData();
                                                Object ownerMarker = plant.get("originalOwnerId");
                                                if (ownerMarker != null && ownerMarker.equals(user.getUid())) {
                                                    db.collection("users")
                                                            .document(user.getUid())
                                                            .collection("myPlants")
                                                            .document(doc.getId())
                                                            .set(plant);

                                                    db.collection("users")
                                                            .document(uid)
                                                            .collection("myPlants")
                                                            .document(doc.getId())
                                                            .get().addOnSuccessListener(sharedSnapshot -> {
                                                                Object stillShared = sharedSnapshot.get("sharedFrom");
                                                                if (stillShared != null && stillShared.equals(user.getUid())) {
                                                                    sharedSnapshot.getReference().delete();
                                                                }
                                                            });

                                                }


                                            }
                                            Toast.makeText(getContext(), "Plants recovered!", Toast.LENGTH_SHORT).show();
                                        });
                            }

                            db.collection("users").document(user.getUid())
                                    .update("delegatedTo", new ArrayList<>());
                        }
                    });
        });



        return view;
    }

    private void generateQRCode(ImageView qrImageView) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(uid, BarcodeFormat.QR_CODE, 400, 400);
            qrImageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }


    private void loadUserProfile() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        emailTextView.setText(user.getEmail());

        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String profileImageUrl = documentSnapshot.getString("profileImage");

                        if (name != null) nameTextView.setText(name);
                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profileImageView);
                        }

                        List<String> delegatedTo = (List<String>) documentSnapshot.get("delegatedTo");
                        if (delegatedTo != null && !delegatedTo.isEmpty()) {
                            shareStatusTextView.setText("Plants are shared with another user.");
                            shareStatusTextView.setVisibility(View.VISIBLE);
                        } else {
                            shareStatusTextView.setVisibility(View.GONE);
                        }
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show());

    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);
                uploadImageToFirebase();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebase() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || imageUri == null) return;

        String userId = user.getUid();
        String fileName = UUID.randomUUID().toString();

        storage.getReference("profile_images/" + userId + "/" + fileName)
                .putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    taskSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(uri -> {
                        db.collection("users").document(userId)
                                .update("profileImage", uri.toString())
                                .addOnSuccessListener(aVoid -> {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Profile updated", Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (isAdded()) {
                                        Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    if (isAdded()) {
                        Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
