package com.example.licenta_v2.model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Bitmap;

import java.util.HashMap;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.util.Map;
import java.io.ByteArrayOutputStream;

public class PlantDatabase {
    private static final String TAG = "PlantDatabase";
    private FirebaseFirestore db;

    public PlantDatabase() {
        db = FirebaseFirestore.getInstance();
    }

    public void savePlantToFirebase(PlantDetailsResponse details) {
        Map<String, Object> plantData = new HashMap<>();
        plantData.put("scientificName", details.getScientificName());
        plantData.put("commonName", details.getCommonName());
        plantData.put("family", details.getFamily());
        plantData.put("imageUrl", details.getImageUrl());
        plantData.put("description", details.getDescription());
        plantData.put("taxonomy", details.getTaxonomy());
        plantData.put("synonyms", details.getSynonyms());
        plantData.put("edibleParts", details.getEdibleParts());
        plantData.put("watering", details.getWatering());
        plantData.put("propagationMethods", details.getPropagationMethods());
        plantData.put("bestLightCondition",details.getBestLightCondition());

        String plantId = details.getId();
        if (plantId != null && !plantId.isEmpty()) {
            db.collection("plants")
                    .document(plantId)
                    .set(plantData)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Planta salvată cu ID: " + plantId))
                    .addOnFailureListener(e -> Log.w(TAG, "Eroare la salvarea plantei", e));
        } else {
            Log.w(TAG, "ID-ul plantei este null sau gol. Planta NU a fost salvată.");
        }

    }

    public void savePlantWithImage(Bitmap bitmap, PlantDetailsResponse details) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imageRef = storageRef.child("plant_images/" + System.currentTimeMillis() + ".jpg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        UploadTask uploadTask = imageRef.putBytes(imageData);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                details.setImageUrl(uri.toString());
                savePlantToFirebase(details);
            });
        }).addOnFailureListener(e -> {
            Log.e("PlantDatabase", "Eroare la upload imagine: " + e.getMessage());
        });
    }

}