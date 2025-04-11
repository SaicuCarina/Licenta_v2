package com.example.licenta_v2.model;

import java.util.List;

public class GroupedPlantItem {
    private String roomName;
    private List<com.example.licenta_v2.model.SavedPlant> plants;

    public GroupedPlantItem(String roomName, List<com.example.licenta_v2.model.SavedPlant> plants) {
        this.roomName = roomName;
        this.plants = plants;
    }

    public String getRoomName() {
        return roomName;
    }

    public List<com.example.licenta_v2.model.SavedPlant> getPlants() {
        return plants;
    }
}
