package fr.univtln.cniobechoudayer.pimpmytrip;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Waypoint;

import static org.junit.Assert.*;

/**
 * Test class
 * to check the creation of a trip
 */

public class InsertTripTest {
    @Test
    public void tripInsert_isCorrect() throws Exception{
        List<Position> positionList = new ArrayList<>();
        List<Waypoint> waypointList = new ArrayList<>();
        Trip testTrip = new Trip.TripBuilder("id1","trip test").color("#000000").creator("idCreator").distance(1000).listPositions(positionList).listWaypoints(waypointList).build();
        Trip checkTrip = TripController.getInstance().insertTrip("id1",false,positionList,waypointList,"#000000","trip test",1000,"idCreator");
        assertEquals(testTrip,checkTrip);
    }
}
