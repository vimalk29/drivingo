package com.example.drivingo.model;

public class Offer {
    String by,from,to;
    int discount;
    boolean applied;

    public Offer() {
    }

    public Offer(String by, String from, String to, int discount,boolean applied) {
        this.by = by;
        this.from = from;
        this.to = to;
        this.discount = discount;
        this.applied = applied;
    }

    public boolean isApplied() {
        return applied;
    }

    public void setApplied(boolean applied) {
        this.applied = applied;
    }

    public String getBy() {
        return by;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public int getDiscount() {
        return discount;
    }
}
