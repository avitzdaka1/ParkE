package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Blue parking that allows everyone to park (paid) during the day, and allows everyone to park freely during the night.
 */
public class ParkingBlue extends Parking implements Serializable {

    private ArrayList<ParkingRule> paidParkingRules;

    public ParkingBlue() {

    }

    public ParkingBlue(double latitude, double longitude, ArrayList<ParkingRule> paidParkingRules) {
        super(latitude, longitude);
        this.paidParkingRules = paidParkingRules;
    }

    public ArrayList<ParkingRule> getPaidParkingRules() {
        return paidParkingRules;
    }

    @Override
    public String toString() {
        String string = "Parking rules:\n";
        for(ParkingRule p : paidParkingRules) {
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
