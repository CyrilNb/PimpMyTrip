package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Position implements Parcelable{

    private double coordX;
    private double coordY;

    public Position() {
    }

    public Position(double coordX, double coordY) {
        this.coordX = coordX;
        this.coordY = coordY;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(coordX);
        dest.writeDouble(coordY);
    }
}
