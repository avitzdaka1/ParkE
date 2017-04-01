package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * A free parking spot that has an exception (free at specific times).
 */
public class ParkingSpecial extends Parking implements Serializable {

    private ArrayList<ParkingRule> prohibitedParkingRules;

    public ParkingSpecial() {

    }

    public ParkingSpecial(double latitude, double longitude, ArrayList<ParkingRule> prohibitedParkingRules) {
        super(latitude, longitude);
        this.prohibitedParkingRules = prohibitedParkingRules;
    }

    public ArrayList<ParkingRule> getProhibitedParkingRules() {
        return prohibitedParkingRules;
    }

    @Override
    public String toString() {
        String string = "Prohibited Parking:\n";
        for(ParkingRule p : prohibitedParkingRules) {
            string += dayIntToString(p.getParkingDay()) + " - " + p.getParkingStartHour() + ":" + p.getParkingStartMinute() + " - " + p.getParkingEndHour() + ":" + p.getParkingEndMinute() + "\n";
        }
        string += "Address: ";
        return string;
    }

    private String dayIntToString(int day) {
        Calendar calendar = new GregorianCalendar();
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
    }
}
