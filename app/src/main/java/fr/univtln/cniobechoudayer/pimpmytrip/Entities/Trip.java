package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.List;

public class Trip {

    @Exclude
    private String id; //exclude this member from being serialized and inserted in firebase database
    private boolean reference;
    private String name;
    private Date creationDate;
    private int duration;
    private int distance;
    private String color;
    private List<Position> listPositions;
    private List<Waypoint> listWaypoints;
    private String creator;

    public Trip() {
    }

    private Trip(TripBuilder tripBuilder){
        this.id = tripBuilder.id;
        this.color = tripBuilder.color;
        this.creationDate = tripBuilder.creationDate;
        this.name = tripBuilder.name;
        this.reference = tripBuilder.reference;
        this.listPositions = tripBuilder.listPositions;
        this.listWaypoints = tripBuilder.listWaypoints;
        this.creator = tripBuilder.creator;
        this.distance = tripBuilder.distance;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
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

    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Builder of Trip
     */
    public static class TripBuilder{
        private final String id;
        private boolean reference;
        private String name;
        private Date creationDate;
        private int duration; //seconds
        private int distance; //meters
        private String color;
        private List<Position> listPositions;
        private List<Waypoint> listWaypoints;
        private String creator;

        public TripBuilder(String id, String name){
            this.id = id; //required
            this.name = name; //required
        }

        public TripBuilder reference(boolean reference){
            this.reference = reference;
            return this;
        }

        public TripBuilder creationDate(Date creationDate){
            this.creationDate = creationDate;
            return this;
        }

        public TripBuilder duration(int duration){
            this.duration = duration;
            return this;
        }

        public TripBuilder distance(int distance){
            this.distance = distance;
            return this;
        }

        public TripBuilder color(String color){
            this.color = color;
            return this;
        }

        public TripBuilder listPositions(List<Position> listPositions){
            this.listPositions = listPositions;
            return this;
        }

        public TripBuilder listWaypoints(List<Waypoint> listWaypoints){
            this.listWaypoints = listWaypoints;
            return this;
        }

        public TripBuilder creator(String creator){
            this.creator = creator;
            return this;
        }

        public Trip build(){
            return new Trip(this);
        }
    }
}
