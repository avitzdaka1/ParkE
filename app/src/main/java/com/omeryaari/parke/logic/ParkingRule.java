package com.omeryaari.parke.logic;

import android.support.annotation.Nullable;
import java.io.Serializable;
import java.util.Calendar;

/**
 * Holds information for a specific parking rule (day and time duration).
 */
public class ParkingRule implements Serializable, Comparable<ParkingRule> {

    private int parkingDay;
    private Calendar parkingStart;
    private Calendar parkingEnd;

    public int getParkingDay() {
        return parkingDay;
    }

    public Calendar getParkingStart() {
        return parkingStart;
    }

    public Calendar getParkingEnd() {
        return parkingEnd;
    }

    public ParkingRule(int parkingDay, Calendar parkingStart, Calendar parkingEnd) {
        this.parkingDay = parkingDay;
        this.parkingStart = parkingStart;
        this.parkingEnd = parkingEnd;

    }

    @Override
    public int compareTo(@Nullable ParkingRule parking) {
        if (parking != null) {
            if (this.getParkingDay() > parking.getParkingDay())
                return 1;
            else if (this.getParkingDay() == parking.getParkingDay())
                return 0;
            else
                return -1;
        }
        else
            return 1;
    }
}
