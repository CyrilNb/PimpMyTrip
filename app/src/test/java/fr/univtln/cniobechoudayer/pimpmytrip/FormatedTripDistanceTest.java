package fr.univtln.cniobechoudayer.pimpmytrip;

import org.junit.Test;

import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

import static org.junit.Assert.*;

/**
 * Test class
 * to check the formated distance of a trip
 */
public class FormatedTripDistanceTest {
    @Test
    public void formatTripDistance_isCorrect(){
        String exactDistance = "1.500kms";
        double testDistance = 1500;
        String formatedDistance = Utils.formatTripDistance(testDistance);
        assertEquals(exactDistance,formatedDistance);
    }
}
