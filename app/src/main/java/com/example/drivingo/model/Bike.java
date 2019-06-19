package com.example.drivingo.model;

import android.support.annotation.NonNull;

import java.util.HashMap;

public class Bike implements Comparable<Bike>{
    private String model,bikeNo,image;
    private HashMap<String,Double> location;
    private boolean isAvailable;
    private double distance;
    private int rent;
    private String preBookTime;

    public Bike(){}

    public Bike(String model, String bikeNo, String image, HashMap<String, Double> location, boolean isAvailable,int rent,String preBookTime) {
        this.model = model;
        this.bikeNo = bikeNo;
        this.image = image;
        this.location = location;
        this.isAvailable = isAvailable;
        this.distance=-1;
        this.rent = rent;
        this.preBookTime = preBookTime;
    }

    public int getRent() {
        return rent;
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

    public boolean isIsAvailable() {
        return isAvailable;
    }

    public String getPreBookTime() {
        return preBookTime;
    }

    @Override
    public int compareTo(@NonNull Bike bike) {
        return this.distance>bike.distance?1:-1;
    }
}
