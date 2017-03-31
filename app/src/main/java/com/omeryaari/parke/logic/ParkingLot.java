package com.omeryaari.parke.logic;

/**
 * Parking lot, the price stands for a "whole day" parking price.
 */
public class ParkingLot extends Parking {

    private double price;

    public ParkingLot(double longitude, double latitude, String parkingImageURL, double price) {
        super(longitude, latitude, parkingImageURL);
        this.price = price;
    }

    public double getPrice() {
        return price;
    }
}
