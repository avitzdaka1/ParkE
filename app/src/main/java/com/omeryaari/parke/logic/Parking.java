package com.omeryaari.parke.logic;

import java.io.Serializable;

/**
 * General parking class, also used to hold free parking spots.
 */
public class Parking implements Serializable {

    private long id;
    private double longitude;
    private double latitude;
    private String parkingImageURL;

    public Parking(long id, double longitude, double latitude, String parkingImageURL) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.parkingImageURL = parkingImageURL;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public String getParkingImageURL() {
        return parkingImageURL;
    }

    public long getId() {
        return id;
    }
}
