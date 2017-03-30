package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Blue parking that allows everyone to park (paid) during the day, and allows only residents to park during the night.
 */
public class ParkingBlueResidents extends Parking implements Serializable {

    private ArrayList<ParkingRule> paidParkingRules;
    private ArrayList<ParkingRule> residentParkingRules;
    private int areaLabel;

    public ParkingBlueResidents(long id, double longitude, double latitude, String parkingImageURL, int areaLabel) {
        super(id, longitude, latitude, parkingImageURL);
        this.areaLabel = areaLabel;
        paidParkingRules = new ArrayList<>();
        residentParkingRules = new ArrayList<>();

    }

    public ArrayList<ParkingRule> getPaidParkingRules() {
        return paidParkingRules;
    }

    public ArrayList<ParkingRule> getResidentParkingRules() {
        return residentParkingRules;
    }

    public int getAreaLabel() {
        return areaLabel;
    }
}
