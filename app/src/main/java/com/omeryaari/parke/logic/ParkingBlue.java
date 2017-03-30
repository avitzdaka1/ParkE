package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Blue parking that allows everyone to park (paid) during the day, and allows everyone to park freely during the night.
 */
public class ParkingBlue extends Parking implements Serializable {

    private ArrayList<ParkingRule> paidParkingRules;

    public ParkingBlue(long id, double longitude, double latitude, String parkingImageURL) {
        super(id, longitude, latitude, parkingImageURL);
        paidParkingRules = new ArrayList<>();
    }

    public ArrayList<ParkingRule> getPaidParkingRules() {
        return paidParkingRules;
    }
}
