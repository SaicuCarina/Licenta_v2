package com.example.licenta_v2.model;

import java.io.Serializable;

public class SavedPlant implements Serializable {
    private PlantDetailsResponse plantData;
    private String id;
    private String addedSite;
    private String addedDate;
    private boolean exposedToRain;
    private String lastWateredDate;

    private String customName;

    public SavedPlant() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLastWateredDate() {
        return lastWateredDate;
    }

    public void setLastWateredDate(String lastWateredDate) {
        this.lastWateredDate = lastWateredDate;
    }


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
