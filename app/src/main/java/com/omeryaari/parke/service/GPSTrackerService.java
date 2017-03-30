package com.omeryaari.parke.service;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;

public class GPSTrackerService extends Service implements LocationListener {

    private Location location;
    private GPSServiceBinder gpsServiceBinder;
    private LocationChangeListener locationChangeListener;
    private long minTimeUpdate;
    private float minDistanceUpdate;
    private boolean useNetwork;
    protected LocationManager locationManager;

    public class GPSServiceBinder extends Binder {
        public GPSTrackerService getService() {
            return GPSTrackerService.this;
        }
    }

    public interface LocationChangeListener {
        void onNewLocation(Location location);
    }

    public Location getLocation() {
        return location;
    }

    //  Starts listening to location changes and updates location with the last known location.
    public void startListening() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            }
            else {
                //  If useNetwork is true, service will also use the location updates from the network the user is connected to.
                if (useNetwork)
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTimeUpdate, minDistanceUpdate, this);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTimeUpdate, minDistanceUpdate, this);
                if (locationManager != null) {
                    location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if (location == null)
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  Stops using the location services.
    public void stopListening() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

        }
        else {
            if (locationManager != null) {
                locationManager.removeUpdates(GPSTrackerService.this);
            }
        }
    }

    public void setLocationChangeListener(LocationChangeListener locationChangeListener) {
        this.locationChangeListener = locationChangeListener;
    }

    //  Sets the initial settings to be used by the service.
    public void setSettings(long minTimeUpdate, float minDistanceUpdate, boolean useNetwork) {
        this.minTimeUpdate = minTimeUpdate;
        this.minDistanceUpdate = minDistanceUpdate;
        this.useNetwork = useNetwork;
    }

    @Override
    public IBinder onBind(Intent intent) {
        gpsServiceBinder = new GPSServiceBinder();
        return gpsServiceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        stopListening();
        return super.onUnbind(intent);
    }

    @Override
    public void onLocationChanged(Location location) {
        if (locationChangeListener != null)
            locationChangeListener.onNewLocation(location);
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
