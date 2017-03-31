package com.omeryaari.parke.ui;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.omeryaari.parke.R;
import com.omeryaari.parke.logic.FirebaseComm;
import com.omeryaari.parke.logic.Parking;
import com.omeryaari.parke.service.AzimutService;
import com.omeryaari.parke.service.GPSTrackerService;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class ParkSearchActivity extends AppCompatActivity implements AzimutService.AzimutListener, GPSTrackerService.LocationChangeListener {

    public static final int TAG_CODE_PERMISSION_LOCATION = 2;
    public static final long MIN_TIME_BW_UPDATES = 500;
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    public static final float MAP_ZOOM_DEFAULT = 17.0f;

    private List<Parking> parkingList;
    private GoogleMap gMap;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private EditText editTextTimeStart;
    private EditText editTextTimeEnd;
    private EditText editTextAddress;
    private EditText editTextAreaLabel;
    private ImageButton imageButtonSearchFree;
    private ImageButton imageButtonSearchPaid;
    private ImageButton imageButtonSearchParkLot;
    private AzimutService azimutService;
    private GPSTrackerService gpsTrackerService;
    private FirebaseComm firebaseObject;

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
        if (actionBar != null)
            actionBar.hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_search);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        checkLocationPermissions();
        startAzimutService();
        initTimerPicker();
        setupButtons();
        editTextAddress = (EditText) findViewById(R.id.editext_search_address);
        editTextAreaLabel = (EditText) findViewById(R.id.editext_search_area_label);
        firebaseObject = new FirebaseComm();
    }

    /**
     * Initializes the timer pickers
     */
    private void initTimerPicker() {
        editTextTimeStart = (EditText) findViewById(R.id.editext_search_start_time);
        editTextTimeStart.setOnClickListener(new View.OnClickListener() {
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
                                editTextTimeStart.setText("0" + selectedHour + ":0" + selectedMinute);
                            else if (selectedHour < 10)
                                editTextTimeStart.setText("0" + selectedHour + ":" + selectedMinute);
                            else
                                editTextTimeStart.setText(selectedHour + ":0" + selectedMinute);
                        } else
                            editTextTimeStart.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        editTextTimeEnd = (EditText) findViewById(R.id.editext_search_end_time);
        editTextTimeEnd.setOnClickListener(new View.OnClickListener() {
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
                                editTextTimeEnd.setText("0" + selectedHour + ":0" + selectedMinute);
                            else if (selectedHour < 10)
                                editTextTimeEnd.setText("0" + selectedHour + ":" + selectedMinute);
                            else
                                editTextTimeEnd.setText(selectedHour + ":0" + selectedMinute);
                        } else
                            editTextTimeEnd.setText(selectedHour + ":" + selectedMinute);
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
            ActivityCompat.requestPermissions(this, new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    TAG_CODE_PERMISSION_LOCATION);
        } else {
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
        showMap();
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
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0);
            return info != null;
        } catch (PackageManager.NameNotFoundException e) {
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

    /**
     *
     * @param coords the latlng coordinate.
     * @return address information.
     */
    private List<Address> getAddressFromLatLng(LatLng coords) {
        Geocoder geocoder;
        geocoder = new Geocoder(this, Locale.getDefault());
        try {
            return geocoder.getFromLocation(coords.latitude, coords.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Returns a given address's coordinates.
     * @param strAddress the address.
     * @return the coordinates.
     */
    private LatLng getLatLngFromAddress(String strAddress) {
        Geocoder coder = new Geocoder(getApplicationContext());
        LatLng p1 = null;
        try {
            // May throw an IOException
            List<Address> address;
            address = coder.getFromLocationName(strAddress, 5);
            if (address == null || address.size() == 0) {
                return null;
            }
            Address location = address.get(0);
            location.getLatitude();
            location.getLongitude();

            p1 = new LatLng(location.getLatitude(), location.getLongitude());

        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return p1;
    }

    /**
     * Adds a marker that indicates the user's location to the google map
     */
    private void addUserMarker() {
        BitmapDescriptor currentLocationIcon = BitmapDescriptorFactory.fromResource(R.drawable.location_arrow_small);
        if (currentLocationMarker != null)
            currentLocationMarker.remove();
        if (currentLocation != null)
            currentLocationMarker = gMap.addMarker(new MarkerOptions()
                    .position(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()))
                    .rotation(0)
                    .icon(currentLocationIcon));
    }

    /**
     * Shows the google map with the current user's location.
     */
    private void showMap() {
        if (gpsTrackerService != null)
            currentLocation = gpsTrackerService.getLocation();
        if (isGoogleMapsInstalled()) {
            // Add the Google Maps fragment dynamically
            final FragmentTransaction transaction = getFragmentManager().beginTransaction();
            MapFragment mapFragment = MapFragment.newInstance();
            transaction.replace(R.id.fragment_park_search, mapFragment);
            transaction.commit();

            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (gpsTrackerService != null)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), MAP_ZOOM_DEFAULT));
                    gMap = googleMap;
                    addUserMarker();
                }
            });
        }
    }

    /**
     * Assigns listeners to the parking search buttons.
     */
    private void setupButtons() {
        imageButtonSearchFree = (ImageButton) findViewById(R.id.imagebutton_search_free);
        imageButtonSearchPaid = (ImageButton) findViewById(R.id.imagebutton_search_paid);
        imageButtonSearchParkLot = (ImageButton) findViewById(R.id.imagebutton_search_parking_lot);
        imageButtonSearchFree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInputEmpty()) {
                    LatLng addressLatLng = getLatLngFromAddress(editTextAddress.getText().toString());
                    if (addressLatLng == null)
                        Toast.makeText(ParkSearchActivity.this, R.string.search_address_error_toast_text, Toast.LENGTH_LONG).show();
                    else {
                        List<Address> addresses = getAddressFromLatLng(addressLatLng);
                        if (addresses != null) {
                            parkingList = firebaseObject.getParkings(addresses, FirebaseComm.FIREBASE_FREE_PARKING_LOCATION, false);
                            moveCameraToTarget(addressLatLng);
                        }
                    }
                }
            }
        });
        imageButtonSearchPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInputEmpty()) {
                    LatLng addressLatLng = getLatLngFromAddress(editTextAddress.getText().toString());
                    if (addressLatLng == null)
                        Toast.makeText(ParkSearchActivity.this, R.string.search_address_error_toast_text, Toast.LENGTH_LONG).show();
                    else {
                        List<Address> addresses = getAddressFromLatLng(addressLatLng);
                        if (addresses != null) {
                            if (TextUtils.isEmpty(editTextAreaLabel.getText().toString()))
                                parkingList = firebaseObject.getParkings(addresses, FirebaseComm.FIREBASE_PAID_PARKING_LOCATION, true);
                            else
                                parkingList = firebaseObject.getParkings(addresses, FirebaseComm.FIREBASE_PAID_PARKING_LOCATION, false);
                            moveCameraToTarget(addressLatLng);
                        }
                    }
                }
            }
        });
        imageButtonSearchParkLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isInputEmpty()) {
                    LatLng addressLatLng = getLatLngFromAddress(editTextAddress.getText().toString());
                    if (addressLatLng == null)
                        Toast.makeText(ParkSearchActivity.this, R.string.search_address_error_toast_text, Toast.LENGTH_LONG).show();
                    else {
                        List<Address> addresses = getAddressFromLatLng(addressLatLng);
                        if (addresses != null) {
                            parkingList = firebaseObject.getParkings(addresses, FirebaseComm.FIREBASE_PARKING_LOT_PARKING_LOCATION, false);
                            moveCameraToTarget(addressLatLng);
                        }
                    }
                }
            }
        });
    }

    /**
     * Checks if the user has entered all details.
     * @return whether the user has entered address, parking start time and parking end time.
     */
    private boolean isInputEmpty() {
        if (TextUtils.isEmpty(editTextAddress.getText().toString()) ||
                TextUtils.isEmpty(editTextTimeStart.getText().toString()) ||
                TextUtils.isEmpty(editTextTimeEnd.getText().toString())) {
            Toast.makeText(ParkSearchActivity.this, R.string.search_error_toast_text, Toast.LENGTH_LONG).show();
        }
        else {
            return false;
        }
        return true;
    }

    private void moveCameraToTarget(LatLng target) {
        if (gpsTrackerService != null)
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(target.latitude, target.longitude), MAP_ZOOM_DEFAULT));
    }

    @Override
    protected void onResume() {
        checkLocationPermissions();
        startListeningToAzimut();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopListeningToGps();
        stopListeningToAzimut();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }
}
