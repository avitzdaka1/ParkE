package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Blue parking that allows everyone to park (paid) during the day, and allows everyone to park freely during the night.
 */
public class ParkingBlue extends Parking implements Serializable {

    private ArrayList<ParkingRule> paidParkingRules;

    public ParkingBlue() {

    }

    public ParkingBlue(double latitude, double longitude, String streetAddress, ArrayList<ParkingRule> paidParkingRules) {
        super(latitude, longitude, streetAddress);
        this.paidParkingRules = paidParkingRules;
    }

    public ArrayList<ParkingRule> getPaidParkingRules() {
        return paidParkingRules;
    }

    @Override
    public String toString() {
        String string = "Parking rules:\n";
        Collections.sort(paidParkingRules);
        for(ParkingRule p : paidParkingRules) {
            string += dayIntToString(p.getParkingDay()) + " - " + p.getParkingStartHour() + ":" + p.getParkingStartMinute() + " - " + p.getParkingEndHour() + ":" + p.getParkingEndMinute() + "\n";
        }
        string += "Address:\n";
        return string;
    }

    private String dayIntToString(int day) {
        Calendar calendar = new GregorianCalendar();
        calendar.set(Calendar.DAY_OF_WEEK, day);
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US);
    }
}
