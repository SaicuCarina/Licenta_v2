package com.example.licenta_v2.model;

public class Site {
    private String name;
    private String photo;
    private boolean full_shade;
    private boolean full_sun;
    private boolean partial_sun;

    public Site() {} // Constructor gol necesar pentru Firestore

    // Getteri
    public String getName() { return name; }
    public String getPhoto() { return photo; }
    public boolean isFull_shade() { return full_shade; }
    public boolean isFull_sun() { return full_sun; }
    public boolean isPartial_sun() { return partial_sun; }
}
