package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

import android.graphics.Color;

import java.util.Date;

public class Trip {

    private int idTrip;
    private boolean isReference;
    private String name;
    private Date creationDate;
    private int durationInSeconds;
    private int distanceInMeters;
    private Color color;

    public Trip(Color color, Date creationDate, String name, boolean isReference) {
        this.color = color;
        this.creationDate = creationDate;
        this.name = name;
        this.isReference = isReference;
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

    public int getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(int durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public int getDistanceInMeters() {
        return distanceInMeters;
    }

    public void setDistanceInMeters(int distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
