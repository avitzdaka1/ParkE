package com.omeryaari.parke.logic;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Parking lot, the price stands for a "whole day" parking price.
 */
public class ParkingLot extends Parking implements Serializable {

    private double price;

    public ParkingLot() {

    }

    public ParkingLot(double latitude, double longitude, double price) {
        super(latitude, longitude);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toString() {
        String string = "Price: " + price + "\nAddress: ";
        return string;
    }
}
