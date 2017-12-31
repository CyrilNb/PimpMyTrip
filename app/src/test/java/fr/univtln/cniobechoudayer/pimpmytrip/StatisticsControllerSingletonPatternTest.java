package fr.univtln.cniobechoudayer.pimpmytrip;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import fr.univtln.cniobechoudayer.pimpmytrip.controllers.StatisticsController;
import fr.univtln.cniobechoudayer.pimpmytrip.controllers.TripController;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Position;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Trip;
import fr.univtln.cniobechoudayer.pimpmytrip.entities.Waypoint;

import static org.junit.Assert.*;

/**
 * Test class
 * to check the creation of a trip
 */

public class StatisticsControllerSingletonPatternTest {
    @Test
    public void statisticsControllerSingletonPatternTest_isCorrect() throws Exception{
        StatisticsController statsControllerTest = StatisticsController.getInstance();
        StatisticsController statisticsController2Test = StatisticsController.getInstance();
        assertEquals(statsControllerTest,statisticsController2Test);
    }
}
