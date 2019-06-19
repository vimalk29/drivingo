package com.example.drivingo.model;

public class BookedBike {
    private Bike bike;
    private Booking booking;
    private String TotalRent;

    public BookedBike(Bike bike, Booking booking) {
        this.bike = bike;
        this.booking = booking;
    }

    public void setTotalRent(String totalRent) {
        TotalRent = totalRent;
    }

    public String getTotalRent() {
        return TotalRent;
    }

    public void setBike(Bike bike) {
        this.bike = bike;
    }

    public Bike getBike() {
        return bike;
    }

    public Booking getBooking() {
        return booking;
    }
}
