package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

import java.util.Date;
import java.util.List;

public class Trip {

    private boolean reference;
    private String name;
    private Date creationDate;
    private int durationInSeconds;
    private int distanceInMeters;
    private String color;
    private List<Position> listPositions;
    private List<Waypoint> listWaypoints;
    private String creator;

    public Trip() {
    }

    //TODO BUILDER
    public Trip(String color, Date creationDate, String name, boolean isReference, List<Position> listPositions, List<Waypoint> listMarkers, int distance, String creatorId) {

        this.color = color;
        this.creationDate = creationDate;
        this.name = name;
        this.reference = isReference;
        this.listPositions = listPositions;
        this.listWaypoints = listMarkers;
        this.creator = creatorId;
        this.distanceInMeters = distance;
    }

    public boolean isReference() {
        return reference;
    }

    public void setIsReference(boolean isReference) {
        this.reference = isReference;
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

    public void setReference(boolean reference) {
        this.reference = reference;
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

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<Position> getListPositions() {
        return listPositions;
    }

    public void setListPositions(List<Position> listPositions) {
        this.listPositions = listPositions;
    }

    public List<Waypoint> getListWaypoints() {
        return listWaypoints;
    }

    public void setListWaypoints(List<Waypoint> listWaypoints) {
        this.listWaypoints = listWaypoints;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }


}
