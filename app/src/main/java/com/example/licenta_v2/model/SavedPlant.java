package com.example.licenta_v2.model;

import java.io.Serializable;

public class SavedPlant implements Serializable {
    private PlantDetailsResponse plantData;
    private String addedSite;
    private String addedDate;
    private boolean exposedToRain;

    private String customName;

    public SavedPlant() {}

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public PlantDetailsResponse getPlantData() {
        return plantData;
    }

    public void setPlantData(PlantDetailsResponse plantData) {
        this.plantData = plantData;
    }

    public String getAddedSite() {
        return addedSite;
    }

    public void setAddedSite(String addedSite) {
        this.addedSite = addedSite;
    }

    public String getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(String addedDate) {
        this.addedDate = addedDate;
    }

    public boolean isExposedToRain() {
        return exposedToRain;
    }

    public void setExposedToRain(boolean exposedToRain) {
        this.exposedToRain = exposedToRain;
    }
}
