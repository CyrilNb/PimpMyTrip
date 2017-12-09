package fr.univtln.cniobechoudayer.pimpmytrip.entities;

import android.graphics.Color;

public class Waypoint {

    private int idWayPoint;
    private TypeWaypoint type;
    private String label;
    private Position position;
    private Color color;

    public Waypoint(Position position, String label, TypeWaypoint type) {
        this.position = position;
        this.label = label;
        this.type = type;
    }

    public Waypoint() {
    }

    public int getIdWayPoint() {
        return idWayPoint;
    }

    public void setIdWayPoint(int idWayPoint) {
        this.idWayPoint = idWayPoint;
    }

    public TypeWaypoint getType() {
        return type;
    }

    public void setType(TypeWaypoint type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
