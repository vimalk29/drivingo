package com.example.drivingo.model;

import android.support.annotation.NonNull;

import java.util.HashMap;

public class Bike implements Comparable<Bike>{
    private String model,bikeNo,image;
    private HashMap<String,Double> location;
    private boolean isAvailable;
    private double distance;

    public Bike(){}

    public Bike(String model, String bikeNo, String image, HashMap<String, Double> location, boolean isAvailable) {
        this.model = model;
        this.bikeNo = bikeNo;
        this.image = image;
        this.location = location;
        this.isAvailable = isAvailable;
        this.distance=-1;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getModel() {
        return model;
    }

    public String getBikeNo() {
        return bikeNo;
    }

    public String getImage() {
        return image;
    }

    public HashMap<String, Double> getLocation() {
        return location;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    @Override
    public int compareTo(@NonNull Bike bike) {
        return this.distance>bike.distance?1:-1;
    }
}
