package com.example.licenta_v2.ui.findplants;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.licenta_v2.model.PlantDetailsResponse;
import com.example.licenta_v2.repository.PlantRepository;

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
                plantDetails.setValue(details);
            }

            @Override
            public void onError(String error) {
                isLoading.setValue(false);
                errorMessage.setValue(error);
            }
        });
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
}
