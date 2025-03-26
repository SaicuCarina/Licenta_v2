package com.example.licenta_v2.model;

import java.util.List;

public class PlantRequest {
    private String api_key;
    private List<String> images;

    public PlantRequest(String api_key, List<String> images) {
        this.api_key = api_key;
        this.images = images;
    }
}
