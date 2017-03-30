package com.omeryaari.parke.logic;

import java.util.ArrayList;

/**
 * A free parking spot that has an exception (free at specific times).
 */
public class ParkingWithException extends Parking {

    private ArrayList<ParkingRule> prohibitedParkingRules;

    public ParkingWithException(long id, double longitude, double latitude, String parkingImageURL) {
        super(id, longitude, latitude, parkingImageURL);
        prohibitedParkingRules = new ArrayList<>();
    }

    public ArrayList<ParkingRule> getProhibitedParkingRules() {
        return prohibitedParkingRules;
    }
}
