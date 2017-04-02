package com.omeryaari.parke.logic;

import java.io.Serializable;

/**
 * General parking class, also used to hold free parking spots.
 */
public class Parking implements Serializable {

    private double longitude;
    private double latitude;
    private String firebaseKey;
    private String streetAddress;

    private boolean isReported = false;

    @Override
    public String toString() {
        return "Address:\nw ";
    }

    public Parking() {

    }

    public Parking(double latitude, double longitude, String streetAddress) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public boolean isReported() {
        return isReported;
    }

    public void setReported(boolean reported) {
        isReported = reported;
    }

    public String getFirebaseKey() {
        return firebaseKey;
    }

    public void setFirebaseKey(String firebaseKey) {
        this.firebaseKey = firebaseKey;
    }
}
