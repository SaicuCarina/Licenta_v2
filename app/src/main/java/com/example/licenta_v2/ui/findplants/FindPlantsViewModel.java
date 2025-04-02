package com.example.licenta_v2.ui.findplants;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.repository.PlantRepository;
import com.google.firebase.firestore.FirebaseFirestore;

public class FindPlantsViewModel extends ViewModel {

    private final PlantRepository plantRepository = new PlantRepository();

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<PlantDetailsResponse> plantDetails = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public void identifyPlant(String base64Image) {
        isLoading.setValue(true);
        plantRepository.identifyPlant(base64Image, new PlantRepository.PlantCallback() {
            @Override
            public void onSuccess(PlantDetailsResponse details) {
                isLoading.setValue(false);
                if (details.getWatering() != null) {
                    try {
                        int min = Integer.parseInt(details.getWatering().getMin());
                        int max = Integer.parseInt(details.getWatering().getMax());
                        String level = calculateCareLevel(min, max);
                        String watering = calculateWateringInterval(min, max);
                        details.setCareLevel(level);
                        details.setWateringInterval(watering);
                    } catch (NumberFormatException e) {
                        Log.e("CareLevel", "Invalid watering values: " + e.getMessage());
                        details.setCareLevel("Unknown");
                    }
                }
                details.setLightSummary(generateLightSummary(details.getBestLightCondition()));

                String documentId = (details.getId() != null && !details.getId().isEmpty())
                        ? details.getId()
                        : details.getCommonName();

                FirebaseFirestore.getInstance()
                        .collection("plants")
                        .document(documentId)
                        .get()
                        .addOnSuccessListener(snapshot -> {
                            if (snapshot.exists()) {
                                PlantDetailsResponse existingPlant = snapshot.toObject(PlantDetailsResponse.class);
                                plantDetails.setValue(existingPlant);
                            } else {
                                FirebaseFirestore.getInstance()
                                        .collection("plants")
                                        .document(documentId)
                                        .set(details)
                                        .addOnSuccessListener(unused -> plantDetails.setValue(details));
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FirebaseCheck", "Eroare verificare plant existenta: " + e.getMessage());
                            // fallback: continuăm cu ce avem
                            plantDetails.setValue(details);
                        });
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
    }

    private String calculateCareLevel(int min, int max) {
        if (min == 1 && max == 1) return "Dry";
        if (min == 1 && max == 2) return "Dry to Medium";
        if (min == 2 && max == 2) return "Medium";
        if (min == 2 && max == 3) return "Medium to Wet";
        if (min == 3 && max == 3) return "Wet";
        return "Unknown";
    }

    private String calculateWateringInterval(int min, int max) {
        if (min == 1 && max == 1) {
            return "10–14";
        } else if (min == 1 && max == 2) {
            return "7–10";
        } else if (min == 2 && max == 2) {
            return "4–7";
        } else if (min == 2 && max == 3) {
            return "2–4";
        } else if (min == 3 && max == 3) {
            return "1–2";
        } else {
            return "Unknown";
        }
    }

    public String generateLightSummary(String bestLightCondition) {
        if (bestLightCondition == null || bestLightCondition.isEmpty()) {
            return "Unknown";
        }

        String firstSentence = bestLightCondition.split("\\.")[0].toLowerCase();

        if (firstSentence.contains("indirect light") || firstSentence.contains("not direct sunlight") || firstSentence.contains("bright light")) {
            return "Partial sun";
        } else if (firstSentence.contains("full sunlight") || firstSentence.contains("full sun")) {
            return "Full sun";
        } else if (firstSentence.contains("full shade")) {
            return "Full shade";
        }

        return "Unknown";
    }




    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<PlantDetailsResponse> getPlantDetails() {
        return plantDetails;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void setPlantDetails(PlantDetailsResponse details) {
        plantDetails.setValue(details);
    }
}
