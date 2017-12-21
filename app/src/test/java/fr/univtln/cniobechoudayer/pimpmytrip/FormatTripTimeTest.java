package fr.univtln.cniobechoudayer.pimpmytrip;

import org.junit.Test;

import fr.univtln.cniobechoudayer.pimpmytrip.utils.Utils;

import static org.junit.Assert.*;

/**
 * Test class
 * to check the formated distance of a trip
 */
public class FormatTripTimeTest {
    @Test
    public void formatTripTimeTest_isCorrect(){
        String exactTime = "50mins";
        double testTime = 50;
        String formatedTime = Utils.formatTripTime(testTime);
        assertEquals(exactTime,formatedTime);
    }
}
