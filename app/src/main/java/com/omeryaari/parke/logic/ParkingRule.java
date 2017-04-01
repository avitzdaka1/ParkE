package com.omeryaari.parke.logic;

import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Holds information for a specific parking rule (day and time duration).
 */
public class ParkingRule implements Serializable, Comparable<ParkingRule> {

    private int parkingDay, parkingStartHour, parkingStartMinute, parkingEndHour, parkingEndMinute;

    public ParkingRule() {

    }

    public ParkingRule(int parkingDay, int parkingStartHour, int parkingStartMinute, int parkingEndHour, int parkingEndMinute) {
        this.parkingDay = parkingDay;
        this.parkingStartHour = parkingStartHour;
        this.parkingStartMinute = parkingStartMinute;
        this.parkingEndHour = parkingEndHour;
        this.parkingEndMinute = parkingEndMinute;

    }

    public int getParkingDay() {
        return parkingDay;
    }

    public int getParkingStartHour() {
        return parkingStartHour;
    }

    public int getParkingStartMinute() {
        return parkingStartMinute;
    }

    public int getParkingEndHour() {
        return parkingEndHour;
    }

    public int getParkingEndMinute() {
        return parkingEndMinute;
    }

    @Override
    public int compareTo(@Nullable ParkingRule parking) {
        if (parking != null) {
            if (this.parkingDay > parking.getParkingDay())
                return 1;
            else if (this.parkingDay == parking.getParkingDay()) {
                if (this.parkingStartHour == parking.getParkingStartHour()) {
                    if (this.parkingStartMinute > parking.getParkingStartMinute())
                        return 1;
                    else if (this.parkingStartMinute == parking.parkingStartMinute)
                        return 0;
                    else
                        return -1;
                }
                else if (this.parkingStartHour > parking.getParkingStartHour())
                    return 1;
                else
                    return -1;
            }
            else
                return -1;
        }
        else
            return 1;
    }
}
