package com.example.drivingo.model;

public class Booking {
    private String by,date,from,to;

    public  Booking(){}
    public Booking(String by, String date, String from, String to) {
        this.by = by;
        this.date = date;
        this.from = from;
        this.to = to;
    }

    public String getBy() {
        return by;
    }

    public String getDate() {
        return date;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
