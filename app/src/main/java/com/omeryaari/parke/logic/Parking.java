package com.omeryaari.parke.logic;

import java.io.Serializable;

/**
 * General parking class, also used to hold free parking spots.
 */
public class Parking implements Serializable {

    private double longitude;
    private double latitude;
    private String parkingImageURL;
    private String firebaseKey;

    private boolean isReported = false;

    public Parking(double longitude, double latitude, String parkingImageURL) {
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
