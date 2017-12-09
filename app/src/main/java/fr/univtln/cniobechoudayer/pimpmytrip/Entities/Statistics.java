package fr.univtln.cniobechoudayer.pimpmytrip.Entities;

public class Statistics {

    private int nbMyTripsTravelled;
    private int totalDistance;
    private int totalDistanceBySUV;
    private int totalDistanceByWalk;
    private int nbTripsCreated;
    private int nbTripsWalkingCreated;
    private int nbTripsSUVCreated;
    private int totalTimeTravelled;
    private int totalTimeWalked;
    private int totalTimeDrove;


    public Statistics() {
    }

    public Statistics(int nbMyTripsTravelled, int totalDistance, int totalDistanceBySUV, int totalDistanceByWalk, int nbTripsCreated, int nbTripsWalkingCreated, int nbTripsSUVCreated, int totalTimeTravelled, int totalTimeWalked, int totalTimeDrove) {
        this.nbMyTripsTravelled = nbMyTripsTravelled;
        this.totalDistance = totalDistance;
        this.totalDistanceBySUV = totalDistanceBySUV;
        this.totalDistanceByWalk = totalDistanceByWalk;
        this.nbTripsCreated = nbTripsCreated;
        this.nbTripsWalkingCreated = nbTripsWalkingCreated;
        this.nbTripsSUVCreated = nbTripsSUVCreated;
        this.totalTimeTravelled = totalTimeTravelled;
        this.totalTimeWalked = totalTimeWalked;
        this.totalTimeDrove = totalTimeDrove;
    }

    public int getTotalDistanceBySUV() {
        return totalDistanceBySUV;
    }

    public void setTotalDistanceBySUV(int totalDistanceBySUV) {
        this.totalDistanceBySUV = totalDistanceBySUV;
    }

    public int getTotalDistanceByWalk() {
        return totalDistanceByWalk;
    }

    public void setTotalDistanceByWalk(int totalDistanceByWalk) {
        this.totalDistanceByWalk = totalDistanceByWalk;
    }

    public int getNbMyTripsTravelled() {
        return nbMyTripsTravelled;
    }

    public void setNbMyTripsTravelled(int nbMyTripsTravelled) {
        this.nbMyTripsTravelled = nbMyTripsTravelled;
    }

    public int getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(int totalDistance) {
        this.totalDistance = totalDistance;
    }

    public int getNbTripsCreated() {
        return nbTripsCreated;
    }

    public void setNbTripsCreated(int nbTripsCreated) {
        this.nbTripsCreated = nbTripsCreated;
    }

    public int getNbTripsWalkingCreated() {
        return nbTripsWalkingCreated;
    }

    public void setNbTripsWalkingCreated(int nbTripsWalkingCreated) {
        this.nbTripsWalkingCreated = nbTripsWalkingCreated;
    }

    public int getNbTripsSUVCreated() {
        return nbTripsSUVCreated;
    }

    public void setNbTripsSUVCreated(int nbTripsSUVCreated) {
        this.nbTripsSUVCreated = nbTripsSUVCreated;
    }

    public int getTotalTimeTravelled() {
        return totalTimeTravelled;
    }

    public void setTotalTimeTravelled(int totalTimeTravelled) {
        this.totalTimeTravelled = totalTimeTravelled;
    }

    public int getTotalTimeWalked() {
        return totalTimeWalked;
    }

    public void setTotalTimeWalked(int totalTimeWalked) {
        this.totalTimeWalked = totalTimeWalked;
    }

    public int getTotalTimeDrove() {
        return totalTimeDrove;
    }

    public void setTotalTimeDrove(int totalTimeDrove) {
        this.totalTimeDrove = totalTimeDrove;
    }
}
