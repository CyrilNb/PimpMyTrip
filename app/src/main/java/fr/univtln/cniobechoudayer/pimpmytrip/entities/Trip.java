package fr.univtln.cniobechoudayer.pimpmytrip.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.Exclude;

import java.util.Date;
import java.util.List;

/**
 * Class that represents trips
 * NB : BUILDER PATTERN IMPLEMENTED
 * Also Parcelable
 */

public class Trip implements Parcelable{

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

    /**
     * Constructor using builder
     * @param tripBuilder
     */

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

    /**
     * Used for parcelable
     * @param in
     */
    protected Trip(Parcel in) {
        id = in.readString();
        reference = in.readByte() != 0;
        name = in.readString();
        duration = in.readInt();
        distance = in.readInt();
        color = in.readString();
        creator = in.readString();
    }

    public static final Creator<Trip> CREATOR = new Creator<Trip>() {
        @Override
        public Trip createFromParcel(Parcel in) {
            return new Trip(in);
        }

        @Override
        public Trip[] newArray(int size) {
            return new Trip[size];
        }
    };

    /**
     * Getters and setters
     * @return
     */

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

    public List<Waypoint> getListWaypoints() {
        return listWaypoints;
    }

    public String getCreator() {
        return creator;
    }


    @Exclude
    public String getId() {
        return id;
    }

    @Exclude
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeByte((byte) (reference ? 1 : 0));
        dest.writeString(name);
        dest.writeInt(duration);
        dest.writeInt(distance);
        dest.writeString(color);
        dest.writeString(creator);
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
