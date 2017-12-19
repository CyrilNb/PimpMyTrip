package fr.univtln.cniobechoudayer.pimpmytrip.entities;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Position class to manage easily positions of markers and users on map
 */

public class Position implements Parcelable{

    private double coordX;
    private double coordY;

    public Position(double coordX, double coordY) {
        this.coordX = coordX;
        this.coordY = coordY;
    }

    //Default constructor required by Firebase
    public Position(){

    }

    protected Position(Parcel in) {
        coordX = in.readDouble();
        coordY = in.readDouble();
    }

    public static final Creator<Position> CREATOR = new Creator<Position>() {
        @Override
        public Position createFromParcel(Parcel in) {
            return new Position(in);
        }

        @Override
        public Position[] newArray(int size) {
            return new Position[size];
        }
    };

    public double getCoordX() {
        return coordX;
    }

    public void setCoordX(float coordX) {
        this.coordX = coordX;
    }

    public double getCoordY() {
        return coordY;
    }

    public void setCoordY(float coordY) {
        this.coordY = coordY;
    }

    /**
     * Implementing methods to make it parcelable
     */

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(coordX);
        dest.writeDouble(coordY);
    }

    @Override
    public String toString() {
        return "Position{" +
                "coordX=" + coordX +
                ", coordY=" + coordY +
                '}';
    }
}
