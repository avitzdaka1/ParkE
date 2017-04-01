package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Blue parking that allows everyone to park (paid) during the day, and allows only residents to park during the night.
 */
public class ParkingBlueResidents extends Parking implements Serializable {

    private ArrayList<ParkingRule> paidParkingRules;
    private ArrayList<ParkingRule> residentParkingRules;
    private int areaLabel;

    public ParkingBlueResidents() {

    }

    public ParkingBlueResidents(double latitude, double longitude, int areaLabel, ArrayList<ParkingRule> paidParkingRules, ArrayList<ParkingRule> residentParkingRules) {
        super(latitude, longitude);
        this.areaLabel = areaLabel;
        this.paidParkingRules = paidParkingRules;
        this.residentParkingRules = residentParkingRules;

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

    @Override
    public String toString() {
        String string = "Paid Parking:\n";
        for(ParkingRule p : paidParkingRules) {
            string += dayIntToString(p.getParkingDay()) + " - " + p.getParkingStartHour() + ":" + p.getParkingStartMinute() + " - " + p.getParkingEndHour() + ":" + p.getParkingEndMinute() + "\n";
        }
        string += "Paid residents parking:\nLabel: " + areaLabel + "\n";
        for(ParkingRule p : residentParkingRules) {
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
