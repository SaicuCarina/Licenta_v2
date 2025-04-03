package com.example.licenta_v2.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.licenta_v2.model.ApiClient;
import com.example.licenta_v2.model.IdentificationResponse;
import com.example.licenta_v2.model.IdentificationResultResponse;
import com.example.licenta_v2.model.PlantApiService;
import com.example.licenta_v2.model.PlantDatabase;
import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.model.PlantRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PlantRepository {

    private static final String API_KEY = "ttOEjo1vI86443UH8MAMjxJr5NmrUMwrQsBeMN2UvxVbwm4jtR";
    private final PlantApiService apiService;

    public PlantRepository() {
        apiService = ApiClient.getClient().create(PlantApiService.class);
    }

    public interface PlantCallback {
        void onSuccess(PlantDetailsResponse details);
        void onError(String error);
    }

    public void identifyPlant(String base64Image, PlantCallback callback) {
        List<String> images = new ArrayList<>();
        images.add(base64Image);
        PlantRequest request = new PlantRequest(API_KEY, images);

        apiService.identifyPlant(request).enqueue(new Callback<IdentificationResponse>() {
            @Override
            public void onResponse(Call<IdentificationResponse> call, Response<IdentificationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // PASĂM IMAGINEA CĂTRE GET PLANT DETAILS
                    getPlantDetails(response.body().getAccessToken(), base64Image, callback);
                } else {
                    try {
                        callback.onError("Eroare identificare: " + response.errorBody().string());
                    } catch (IOException e) {
                        callback.onError("Eroare la citirea răspunsului");
                    }
                }
            }

            @Override
            public void onFailure(Call<IdentificationResponse> call, Throwable t) {
                callback.onError("Conectare eșuată: " + t.getMessage());
            }
        });
    }

    private void getPlantDetails(String accessToken, String base64Image, PlantCallback callback) {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            apiService.checkIdentificationStatus(
                    accessToken,
                    API_KEY,
                    "common_names,url,description,taxonomy,rank,gbif_id,inaturalist_id,image,synonyms,edible_parts,watering,propagation_methods,best_light_condition,best_watering,best_soil_type,toxicity,cultural_significance,gpt",
                    "en"
            ).enqueue(new Callback<IdentificationResultResponse>() {
                @Override
                public void onResponse(Call<IdentificationResultResponse> call, Response<IdentificationResultResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<IdentificationResultResponse.Suggestion> suggestions =
                                response.body().getResult().getClassification().getSuggestions();

                        if (suggestions == null || suggestions.isEmpty()) {
                            getPlantDetails(accessToken, base64Image, callback);
                            return;
                        }

                        PlantDetailsResponse details = suggestions.get(0).getDetails();
                        details.setId(suggestions.get(0).getId());


                        if (details != null) {
                            if (details.getCommonName() == null || details.getCommonName().isEmpty()) {
                                details.setCommonName(suggestions.get(0).getName());
                            }

                            // SETEAZĂ IMAGINEA MANUAL
                            details.setImageUrl(base64Image);
                            details.setId(suggestions.get(0).getId());
                            callback.onSuccess(details);
                        } else {
                            callback.onError("Detaliile plantei lipsesc.");
                        }
                    } else {
                        callback.onError("Eroare identificare: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<IdentificationResultResponse> call, Throwable t) {
                    callback.onError("Conectare eșuată la API: " + t.getMessage());
                }
            });
        }, 2000);
    }
}
