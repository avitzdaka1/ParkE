package com.omeryaari.parke.ui;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.TimePickerDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.omeryaari.parke.R;
import com.omeryaari.parke.logic.FirebaseComm;
import com.omeryaari.parke.logic.FirebaseParkingDownloadListener;
import com.omeryaari.parke.logic.Parking;
import com.omeryaari.parke.logic.ParkingBlue;
import com.omeryaari.parke.logic.ParkingBlueResidents;
import com.omeryaari.parke.logic.ParkingLot;
import com.omeryaari.parke.logic.ParkingRule;
import com.omeryaari.parke.logic.ParkingSpecial;
import com.omeryaari.parke.service.AzimutService;
import com.omeryaari.parke.service.GPSTrackerService;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ParkSearchActivity extends AppCompatActivity implements GPSTrackerService.LocationChangeListener, FirebaseParkingDownloadListener {

    public static final int TAG_CODE_PERMISSION_LOCATION = 2;
    public static final long MIN_TIME_BW_UPDATES = 500;
    public static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    public static final float MAP_ZOOM_DEFAULT = 17.0f;

    private final int MARKER_SIZE = 200;
    private final double PARKING_RADIUS = 300;
    private String currentRequest;
    private List<Parking> parkingList;
    private GoogleMap gMap;
    private ArrayList<Marker> parkingMarkerList;
    private ArrayList<String> parkingSpinnerDays;
    private Location currentLocation;
    private Spinner spinnerParkingDays;
    private EditText editTextTimeStart, editTextTimeEnd, editTextAddress, editTextAreaLabel;
    private ImageButton imageButtonSearchFree, imageButtonSearchPaid, imageButtonSearchParkLot;
    private GPSTrackerService gpsTrackerService;
    private FirebaseComm firebaseObject;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            if (binder instanceof GPSTrackerService.GPSServiceBinder)
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
        initTimePicker();
        initParkingDaysSpinner();
        setupButtons();
        parkingMarkerList = new ArrayList<>();
        editTextAddress = (EditText) findViewById(R.id.editext_search_address);
        editTextAreaLabel = (EditText) findViewById(R.id.editext_search_area_label);
        parkingList = new ArrayList<>();
        firebaseObject = new FirebaseComm();
        firebaseObject.setListener(this);
    }


    /**
     * Initializes the parking rules spinner.
     */
    private void initParkingDaysSpinner() {
        spinnerParkingDays = (Spinner) findViewById(R.id.spinner_parking_mark_day);

        parkingSpinnerDays = new ArrayList<>();
        parkingSpinnerDays.add(ParkMarkActivity.DAY_SUNDAY);
        parkingSpinnerDays.add(ParkMarkActivity.DAY_MONDAY);
        parkingSpinnerDays.add(ParkMarkActivity.DAY_TUESDAY);
        parkingSpinnerDays.add(ParkMarkActivity.DAY_WEDNESDAY);
        parkingSpinnerDays.add(ParkMarkActivity.DAY_THURSDAY);
        parkingSpinnerDays.add(ParkMarkActivity.DAY_FRIDAY);
        parkingSpinnerDays.add(ParkMarkActivity.DAY_SATURDAY);

        ArrayAdapter<String> parkingDaysAdapter = new ArrayAdapter<>(this, R.layout.spinner_text_layout, parkingSpinnerDays);
        spinnerParkingDays.setAdapter(parkingDaysAdapter);
    }

    /**
     * Initializes the time pickers
     */
    private void initTimePicker() {
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

    /**
     * Checks location permissions and if the user has given permission, the function will start listening to the location services.
     */
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


    @Override
    public void onNewLocation(Location location) {
        if (currentLocation != null) {
            currentLocation = location;
        }

    }

    /**
     *
     * @param coords the latlng coordinate.
     * @return address information.
     */
    private List<Address> getAddressFromLatLng(LatLng coords) {
        Locale english = new Locale("en");
        Geocoder geocoder = new Geocoder(this, english);
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
        Locale english = new Locale("en");
        Geocoder geocoder = new Geocoder(this, english);
        LatLng p1 = null;
        try {
            // May throw an IOException
            List<Address> address;
            address = geocoder.getFromLocationName(strAddress, 5);
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
                    if (gpsTrackerService != null && currentLocation != null)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), MAP_ZOOM_DEFAULT));
                    gMap = googleMap;
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                        @Override
                        public View getInfoWindow(Marker marker) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {
                            View myContentView = getLayoutInflater().inflate(R.layout.marker_parking, null);
                            TextView snippet = (TextView) myContentView.findViewById(R.id.textview_marker_information);
                            snippet.setText(marker.getSnippet());
                            return myContentView;
                        }
                    });
                    gMap.setMyLocationEnabled(true);
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
                buttonAction(FirebaseComm.FIREBASE_FREE_PARKING_LOCATION);
            }
        });
        imageButtonSearchPaid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonAction(FirebaseComm.FIREBASE_PAID_PARKING_LOCATION);
            }
        });
        imageButtonSearchParkLot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonAction(FirebaseComm.FIREBASE_PARKING_LOT_PARKING_LOCATION);
            }
        });
    }

    /**
     * Sets a button's on click listener.
     * @param type the parking type.
     */
    private void buttonAction(String type) {
        if (!isInputEmpty()) {
            LatLng coords = getLatLngFromAddress(editTextAddress.getText().toString());
            moveCameraToTarget(coords);
            final String finalType = type;
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    parkingList.clear();
                    downloadParkings(finalType);
                    currentRequest = finalType;
                }
            });
            thread.start();
            removeMarkersFromMap();
        }
    }

    /**
     * Checks the class of the parking instance object.
     * @param parking the Parking object.
     * @return the Parking's class.
     */
    private String checkParkingType(Parking parking) {
        if (parking instanceof ParkingBlue)
            return ParkMarkActivity.PARKING_BLUE;
        else if (parking instanceof ParkingBlueResidents)
            return ParkMarkActivity.PARKING_BLUE_RESIDENTS;
        else if (parking instanceof ParkingLot)
            return ParkMarkActivity.PARKING_PARK_LOT;
        else if (parking instanceof ParkingSpecial)
            return ParkMarkActivity.PARKING_SPECIAL;
        else
            return ParkMarkActivity.PARKING_FREE;
    }

    /**
     *  Adds free parking spots to the map.
     */
    private void addFreeParkings() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LatLng addressCoors = getLatLngFromAddress(editTextAddress.getText().toString());
                LatLng parkingCoords;
                for(Parking park : parkingList) {
                    parkingCoords = new LatLng(park.getLatitude(), park.getLongitude());
                    if (calcDistanceBetween2GeoPoints(addressCoors, parkingCoords) < PARKING_RADIUS) {
                        String type = checkParkingType(park);
                        switch (type) {
                            case ParkMarkActivity.PARKING_BLUE:
                                ParkingBlue parkingBlue = (ParkingBlue) park;
                                if (canParkFree(parkingBlue.getPaidParkingRules()))
                                    addCustomMarker(R.drawable.parking_blue, parkingCoords, parkingBlue);
                                break;
                            case ParkMarkActivity.PARKING_BLUE_RESIDENTS:
                                ParkingBlueResidents parkingBlueResidents = (ParkingBlueResidents) park;
                                if (Integer.parseInt(editTextAreaLabel.getText().toString()) == parkingBlueResidents.getAreaLabel())
                                    addCustomMarker(R.drawable.parking_blue_residents, parkingCoords, parkingBlueResidents);
                                break;
                            case ParkMarkActivity.PARKING_SPECIAL:
                                ParkingSpecial parkingSpecial = (ParkingSpecial) park;
                                if (canParkFree(parkingSpecial.getProhibitedParkingRules()))
                                    addCustomMarker(R.drawable.parking_special, parkingCoords, parkingSpecial);
                                break;
                            case ParkMarkActivity.PARKING_FREE:
                                addCustomMarker(R.drawable.parking_free, parkingCoords, park);
                                break;
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Adds paid parking spots to the map.
     */
    private void addPaidParkings() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LatLng addressCoors = getLatLngFromAddress(editTextAddress.getText().toString());
                LatLng parkingCoords;
                for (Parking park : parkingList) {
                    parkingCoords = new LatLng(park.getLatitude(), park.getLongitude());
                    if (calcDistanceBetween2GeoPoints(addressCoors, parkingCoords) < PARKING_RADIUS) {
                        String type = checkParkingType(park);
                        switch (type) {
                            case ParkMarkActivity.PARKING_BLUE:
                                ParkingBlue parkingBlue = (ParkingBlue) park;
                                if (canParkPaid(parkingBlue.getPaidParkingRules()))
                                    addCustomMarker(R.drawable.parking_blue, parkingCoords, parkingBlue);
                                break;
                            case ParkMarkActivity.PARKING_BLUE_RESIDENTS:
                                ParkingBlueResidents parkingBlueResidents = (ParkingBlueResidents) park;
                                if (canParkPaid(parkingBlueResidents.getPaidParkingRules()))
                                    addCustomMarker(R.drawable.parking_blue, parkingCoords, parkingBlueResidents);
                                break;
                        }
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Adds parking lot spots to the map.
     */
    private void addParkingLots() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LatLng addressCoors = getLatLngFromAddress(editTextAddress.getText().toString());
                LatLng parkingCoords;
                for(Parking park : parkingList) {
                    parkingCoords = new LatLng(park.getLatitude(), park.getLongitude());
                    if (calcDistanceBetween2GeoPoints(addressCoors, parkingCoords) < PARKING_RADIUS) {
                        ParkingLot parkingLot = (ParkingLot) park;
                        addCustomMarker(R.drawable.parking_lot, parkingCoords, parkingLot);
                    }
                }
            }
        });
        thread.start();
    }

    /**
     * Given an array of rules, checks if the user can park in paid parkings.
     * @param rules the arraylist of parking rules.
     * @return whether the user can park in the given parking rules.
     */
    private boolean canParkPaid(List<ParkingRule> rules) {
        boolean canPark = false;
        int parkingDay = ParkMarkActivity.determineDayOfWeek(spinnerParkingDays.getSelectedItem().toString());
        try {
            Date selectedStartTime = new SimpleDateFormat("HH:mm").parse(editTextTimeStart.getText().toString());
            Calendar calendarSelectedStartTime = Calendar.getInstance();
            calendarSelectedStartTime.setTime(selectedStartTime);

            Date selectedEndTime = new SimpleDateFormat("HH:mm").parse(editTextTimeEnd.getText().toString());
            Calendar calendarSelectedEndTime = Calendar.getInstance();
            calendarSelectedEndTime.setTime(selectedEndTime);
            for(ParkingRule rule : rules) {
                if (rule.getParkingDay() == parkingDay) {
                    Date parkingStartTime = new SimpleDateFormat("HH:mm").parse(rule.getParkingStartHour() + ":" + rule.getParkingStartMinute());
                    Calendar calendarParkingStartTime = Calendar.getInstance();
                    calendarParkingStartTime.setTime(parkingStartTime);

                    Date parkingEndTime = new SimpleDateFormat("HH:mm").parse(rule.getParkingEndHour() + ":" + rule.getParkingEndMinute());
                    Calendar calendarParkingEndTime = Calendar.getInstance();
                    calendarParkingEndTime.setTime(parkingEndTime);

                    if (((calendarSelectedStartTime.after(calendarParkingStartTime) || calendarSelectedStartTime.equals(calendarParkingStartTime)) &&
                            (calendarSelectedEndTime.before(calendarParkingEndTime) || calendarSelectedEndTime.equals(calendarParkingEndTime)))) {
                        canPark = true;
                    }
                }
            }
        }
        catch (ParseException ex) {
            ex.printStackTrace();
            canPark = false;
        }
        return canPark;
    }

    /**
     * Given an array of rules, checks if the user can park freely.
     * @param rules the arraylist of parking rules.
     * @return whether the user can park in the given parking rules.
     */
    private boolean canParkFree(List<ParkingRule> rules) {
        boolean canPark = false;
        int parkingDay = ParkMarkActivity.determineDayOfWeek(spinnerParkingDays.getSelectedItem().toString());
        try {
            Date selectedStartTime = new SimpleDateFormat("HH:mm").parse(editTextTimeStart.getText().toString());
            Calendar calendarSelectedStartTime = Calendar.getInstance();
            calendarSelectedStartTime.setTime(selectedStartTime);

            Date selectedEndTime = new SimpleDateFormat("HH:mm").parse(editTextTimeEnd.getText().toString());
            Calendar calendarSelectedEndTime = Calendar.getInstance();
            calendarSelectedEndTime.setTime(selectedEndTime);

            for(ParkingRule rule : rules) {
                if (rule.getParkingDay() == parkingDay) {
                    Date parkingStartTime = new SimpleDateFormat("HH:mm").parse(rule.getParkingStartHour() + ":" + rule.getParkingStartMinute());
                    Calendar calendarParkingStartTime = Calendar.getInstance();
                    calendarParkingStartTime.setTime(parkingStartTime);

                    Date parkingEndTime = new SimpleDateFormat("HH:mm").parse(rule.getParkingEndHour() + ":" + rule.getParkingEndMinute());
                    Calendar calendarParkingEndTime = Calendar.getInstance();
                    calendarParkingEndTime.setTime(parkingEndTime);
                    if (((calendarSelectedStartTime.after(calendarParkingEndTime) || calendarSelectedStartTime.equals(calendarParkingEndTime)) && calendarSelectedEndTime.after(calendarSelectedStartTime) ||
                            (calendarSelectedEndTime.before(calendarParkingStartTime) || calendarSelectedEndTime.equals(calendarParkingStartTime)) && calendarSelectedStartTime.before(calendarSelectedEndTime))) {
                        canPark = true;
                    }
                    else if (calendarSelectedStartTime.after(calendarSelectedEndTime)) {
                        for(ParkingRule rule2 : rules) {
                            if (rule2.getParkingDay() == parkingDay + 1 || (parkingDay == 7 && rule2.getParkingDay() == 1)) {
                                Date parkingStartTime2 = new SimpleDateFormat("HH:mm").parse(rule.getParkingStartHour() + ":" + rule.getParkingStartMinute());
                                Calendar calendarParkingStartTime2 = Calendar.getInstance();
                                calendarParkingStartTime2.setTime(parkingStartTime2);

                                Date parkingEndTime2 = new SimpleDateFormat("HH:mm").parse(rule.getParkingEndHour() + ":" + rule.getParkingEndMinute());
                                Calendar calendarParkingEndTime2 = Calendar.getInstance();
                                calendarParkingEndTime2.setTime(parkingEndTime2);

                                if (((calendarSelectedStartTime.after(calendarParkingEndTime) || calendarSelectedStartTime.equals(calendarParkingEndTime)) &&
                                        (calendarSelectedEndTime.before(calendarParkingStartTime2) || calendarSelectedEndTime.equals(calendarParkingStartTime2)))) {
                                    canPark = true;
                                }
                                else
                                    return false;
                            }
                            else
                                canPark = true;
                        }
                    }
                    else
                        return false;
                }
                else
                    canPark = true;
            }
        }
        catch (ParseException ex) {
            ex.printStackTrace();
            canPark = false;
        }
        return canPark;
    }

    /**
     *  Starts navigation to the desired address using waze / google navigation app.
     */
    private void startNavigation(LatLng address) {
        String str;
        try {
            str = "waze://?ll=" + address.latitude + "," + address.longitude + "&navigate=yes";
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
            startActivity(navigationIntent);
        }
        catch (ActivityNotFoundException ex) {
            str = "http://maps.google.com/maps?saddr=" + currentLocation.getLatitude() + "," +
                    currentLocation.getLongitude() + "&daddr=" + address.latitude + "," + address.longitude;
            Intent navigationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(str));
            startActivity(navigationIntent);
        }
    }

    /**
     * Downloads parking spots according to the user's request.
     * @param type the type of parking spots the program will download.
     */
    private void downloadParkings(String type) {
        LatLng addressLatLng = getLatLngFromAddress(editTextAddress.getText().toString());
        if (addressLatLng == null)
            Toast.makeText(ParkSearchActivity.this, R.string.search_address_error_toast_text, Toast.LENGTH_LONG).show();
        else {
            List<Address> addresses = getAddressFromLatLng(addressLatLng);
            if (addresses != null) {
                if (!TextUtils.isEmpty(editTextAreaLabel.getText().toString()))
                    firebaseObject.getParkings(addresses, type, true);
                else
                    firebaseObject.getParkings(addresses, type, false);
            }
        }
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

    /**
     * Moves camera to the target coordinates.
     * @param target the target in latlng.
     */
    private void moveCameraToTarget(LatLng target) {
        if (gpsTrackerService != null)
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(target.latitude, target.longitude), MAP_ZOOM_DEFAULT));
    }

    /**
     * Removes existing markers from map and clears the markers ArrayList.
     */
    private void removeMarkersFromMap() {
        if (parkingMarkerList.size() > 0) {
            for(Marker m : parkingMarkerList)
                m.remove();
        }
        parkingMarkerList.clear();
    }

    /**
     * Adds a custom marker to the map.
     * @param drawable the marker's drawable address.
     * @param location the location to add the marker at.
     */
    private void addCustomMarker(int drawable, LatLng location, Parking parking) {
        final int finalDrawable = drawable;
        final LatLng finalLocation = location;
        final Parking finalParking = parking;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Bitmap b = BitmapFactory.decodeResource(getResources(), finalDrawable);
                Bitmap smallMarker = Bitmap.createScaledBitmap(b, MARKER_SIZE, MARKER_SIZE, false);
                Marker m = gMap.addMarker(new MarkerOptions().
                        position(finalLocation).
                        icon(BitmapDescriptorFactory.fromBitmap(smallMarker)).
                        snippet(finalParking.toString() + finalParking.getStreetAddress()));
                final LatLng markerLocation = finalLocation;
                gMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        startNavigation(markerLocation);
                    }
                });
                parkingMarkerList.add(m);
            }
        });
    }

    /**
     * Calculates the distance between two geo points
     * @param source the address the user is searching.
     * @param destination the given parking's location.
     * @return the distance.
     */
    private double calcDistanceBetween2GeoPoints(LatLng source, LatLng destination) {
        Location loc1 = new Location("");
        loc1.setLatitude(source.latitude);
        loc1.setLongitude(source.longitude);

        Location loc2 = new Location("");
        loc2.setLatitude(destination.latitude);
        loc2.setLongitude(destination.longitude);

        return loc1.distanceTo(loc2);
    }

    @Override
    protected void onResume() {
        checkLocationPermissions();
        super.onResume();
    }

    @Override
    protected void onPause() {
        stopListeningToGps();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unbindService(serviceConnection);
        super.onDestroy();
    }

    @Override
    public void parkingDownloaded(List<Parking> parkingList, String type) {
        if (parkingList != null) {
            this.parkingList.addAll(parkingList);
            switch (type) {
                case FirebaseComm.FIREBASE_FREE_PARKING_LOCATION:
                    addFreeParkings();
                    break;
                case FirebaseComm.FIREBASE_PAID_PARKING_LOCATION:
                    if (currentRequest.equals(FirebaseComm.FIREBASE_FREE_PARKING_LOCATION))
                        addFreeParkings();
                    else
                        addPaidParkings();
                    break;
                case FirebaseComm.FIREBASE_PAID_RESIDENTS_PARKING_LOCATION:
                    if (currentRequest.equals(FirebaseComm.FIREBASE_FREE_PARKING_LOCATION))
                        addFreeParkings();
                    else
                        addPaidParkings();
                    break;
                case FirebaseComm.FIREBASE_EXCEPTION_PARKING_LOCATION:
                    addFreeParkings();
                    break;
                case FirebaseComm.FIREBASE_PARKING_LOT_PARKING_LOCATION:
                    addParkingLots();
                    break;
            }
        }
    }
}