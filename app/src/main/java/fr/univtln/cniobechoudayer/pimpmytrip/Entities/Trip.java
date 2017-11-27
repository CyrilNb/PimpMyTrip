package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

import android.graphics.Color;

import java.util.Date;

public class Trip {

    private int idTrip;
    private boolean isReference;
    private String name;
    private Date creationDate;
    private int duration;
    private double distance;
    private String color;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public Trip(){}

    //TODO builder
    public Trip(String color, Date creationDate, String name, boolean isReference) {
        this.color = color;
        this.creationDate = creationDate;
        this.name = name;
        this.isReference = isReference;
    }

    public Trip(String color, String name, boolean isReference, double distance) {
        this.color = color;
        this.name = name;
        this.isReference = isReference;
        this.distance = distance;
    }

    public int getIdTrip() {
        return idTrip;
    }

    public void setIdTrip(int idTrip) {
        this.idTrip = idTrip;
    }

    public boolean isReference() {
        return isReference;
    }

    public void setIsReference(boolean isReference) {
        this.isReference = isReference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }


    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "idTrip=" + idTrip +
                ", isReference=" + isReference +
                ", name='" + name + '\'' +
                ", duration=" + duration +
                ", distance=" + distance +
                '}';
    }
}
