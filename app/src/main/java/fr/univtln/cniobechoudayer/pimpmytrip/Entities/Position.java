package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

public class Position {

    private float coordX;
    private float coordY;

    public Position(float coordX, float coordY) {
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public float getCoordX() {
        return coordX;
    }

    public void setCoordX(float coordX) {
        this.coordX = coordX;
    }

    public float getCoordY() {
        return coordY;
    }

    public void setCoordY(float coordY) {
        this.coordY = coordY;
    }
}
