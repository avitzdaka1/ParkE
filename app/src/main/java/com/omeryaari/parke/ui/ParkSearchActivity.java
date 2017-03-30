package com.omeryaari.parke.ui;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.omeryaari.parke.R;
import com.omeryaari.parke.service.AzimutService;
import com.omeryaari.parke.service.GPSTrackerService;

import java.util.Calendar;

public class ParkSearchActivity extends AppCompatActivity implements AzimutService.AzimutListener, GPSTrackerService.LocationChangeListener {

    public static final int TAG_CODE_PERMISSION_LOCATION = 2;
    public static final long MIN_TIME_BW_UPDATES = 500;
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private EditText editTextTime;
    private EditText editTextAddress;
    private AzimutService azimutService;
    private GPSTrackerService gpsTrackerService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder instanceof AzimutService.AzimutServiceBinder)
                setAzimutService(((AzimutService.AzimutServiceBinder) binder).getService());
            else if (binder instanceof GPSTrackerService.GPSServiceBinder)
                setGpsTrackerService(((GPSTrackerService.GPSServiceBinder) binder).getService());

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        initTimerPicker();
        checkLocationPermissions();
        startAzimutService();
    }

    /**
     * Initializes the timer picker
     */
    private void initTimerPicker() {
        editTextTime = (EditText) findViewById(R.id.editext_search_time);
        editTextTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                //  Opens up a time picker dialog for the user to choose desired parking time.
                mTimePicker = new TimePickerDialog(ParkSearchActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (selectedHour < 10 || selectedMinute < 10) {
                            if (selectedHour < 10 && selectedMinute < 10)
                                editTextTime.setText( "0" + selectedHour + ":0" + selectedMinute);
                            else if (selectedHour < 10)
                                editTextTime.setText( "0" + selectedHour + ":" + selectedMinute);
                            else
                                editTextTime.setText( selectedHour + ":0" + selectedMinute);
                        }
                        else
                            editTextTime.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
    }

    private void checkLocationPermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION },
                    TAG_CODE_PERMISSION_LOCATION);
        }
        else {
            if (gpsTrackerService == null)
                bindService(new Intent(ParkSearchActivity.this, GPSTrackerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            startListeningToGps();
        }
    }

    private void startAzimutService() {
        bindService(new Intent(ParkSearchActivity.this, AzimutService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void setGpsTrackerService(GPSTrackerService gpsTrackerService) {
        if (gpsTrackerService != null) {
            this.gpsTrackerService = gpsTrackerService;
            gpsTrackerService.setLocationChangeListener(this);
        }
        startListeningToGps();
    }

    private void setAzimutService(AzimutService azimutService) {
        if (azimutService != null) {
            this.azimutService = azimutService;
            azimutService.setListener(this);
        }
        startListeningToAzimut();
    }

    //  Checks if google maps is installed.
    public boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return info != null;
        }
        catch(PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void startListeningToGps() {
        if (gpsTrackerService != null) {
            gpsTrackerService.setSettings(MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, false);
            gpsTrackerService.startListening();
        }
    }

    private void stopListeningToGps() {
        if (gpsTrackerService != null)
            gpsTrackerService.stopListening();
    }

    private void startListeningToAzimut() {
        if (azimutService != null)
            azimutService.startListening();
    }

    private void stopListeningToAzimut() {
        if (azimutService != null)
            azimutService.stopListening();
    }

    @Override
    public void onRotationEvent(float rotation) {
        if (currentLocationMarker != null)
            currentLocationMarker.setRotation(rotation);
    }

    @Override
    public void onNewLocation(Location location) {
        if (currentLocationMarker != null)
            currentLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
    }
}
