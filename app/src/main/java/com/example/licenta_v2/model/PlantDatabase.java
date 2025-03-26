package com.example.licenta_v2.model;

import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

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
        plantData.put("description", details.getDescription());
        plantData.put("taxonomy", details.getTaxonomy());
        plantData.put("synonyms", details.getSynonyms());
        plantData.put("edibleParts", details.getEdibleParts());
        plantData.put("watering", details.getWatering());
        plantData.put("propagationMethods", details.getPropagationMethods());
        plantData.put("bestLightCondition",details.getBestLightCondition());

        db.collection("plants")
                .add(plantData)
                .addOnSuccessListener(documentReference -> Log.d(TAG, "Planta salvată cu ID: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.w(TAG, "Eroare la salvarea plantei", e));
    }

}