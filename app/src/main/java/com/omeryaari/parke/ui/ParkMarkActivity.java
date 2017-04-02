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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import com.omeryaari.parke.logic.ParkingBlue;
import com.omeryaari.parke.logic.ParkingBlueResidents;
import com.omeryaari.parke.logic.ParkingLot;
import com.omeryaari.parke.logic.ParkingRule;
import com.omeryaari.parke.logic.ParkingSpecial;
import com.omeryaari.parke.service.AzimutService;
import com.omeryaari.parke.service.GPSTrackerService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class ParkMarkActivity extends AppCompatActivity implements AzimutService.AzimutListener, GPSTrackerService.LocationChangeListener {

    public static final int TAG_CODE_PERMISSION_LOCATION = 3;

    public static final String PARKING_FREE = "Free";
    public static final String PARKING_BLUE = "Blue";
    public static final String PARKING_BLUE_RESIDENTS = "Blue (residents)";
    public static final String PARKING_PARK_LOT = "ParkLot";
    public static final String PARKING_SPECIAL = "Special";

    public static final String DAY_SUNDAY = "Sunday";
    public static final String DAY_MONDAY = "Monday";
    public static final String DAY_TUESDAY = "Tuesday";
    public static final String DAY_WEDNESDAY = "Wednesday";
    public static final String DAY_THURSDAY = "Thursday";
    public static final String DAY_FRIDAY = "Friday";
    public static final String DAY_SATURDAY = "Saturday";

    private ImageButton imageButtonAddParking;
    private ImageButton imageBbuttonMarkParking;
    private EditText editTextStartTime;
    private EditText editTextEndTime;
    private EditText editTextPrice;
    private EditText editTextLabel;
    private Spinner spinnerParkingType;
    private Spinner spinnerParkingRules;
    private Spinner spinnerParkingDays;
    private CheckBox checkboxResidents;
    private GoogleMap gMap;
    private Marker currentLocationMarker;
    private Location currentLocation;
    private AzimutService azimutService;
    private GPSTrackerService gpsTrackerService;
    private FirebaseComm firebaseObject;
    //  Regular parking rules
    private ArrayList<ParkingRule> parkingRules1;
    //  Extra array for residents' rules
    private ArrayList<ParkingRule> parkingRules2;
    private ArrayList<String> parkingSpinnerRules;
    private ArrayList<String> parkingSpinnerDays;

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
        setContentView(R.layout.activity_park_mark);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        checkLocationPermissions();
        startAzimutService();
        imageButtonAddParking = (ImageButton) findViewById(R.id.imagebutton_mark_add);
        imageBbuttonMarkParking = (ImageButton) findViewById(R.id.imagebutton_parking_mark);
        checkboxResidents = (CheckBox) findViewById(R.id.parking_mark_residents_checkbox);
        firebaseObject = new FirebaseComm();
        parkingRules1 = new ArrayList<>();
        parkingRules2 = new ArrayList<>();
        initTimePicker();
        initParkingSpinners();
        initParkingRuleAddButton();
        initParkingMarkButton();
    }

    /**
     * Initializes the time pickers
     */
    private void initTimePicker() {
        editTextLabel = (EditText) findViewById(R.id.editext_mark_label);
        editTextPrice = (EditText) findViewById(R.id.editext_mark_price);
        editTextStartTime = (EditText) findViewById(R.id.editext_mark_start_time);
        editTextStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                //  Opens up a time picker dialog for the user to choose desired parking time.
                mTimePicker = new TimePickerDialog(ParkMarkActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (selectedHour < 10 || selectedMinute < 10) {
                            if (selectedHour < 10 && selectedMinute < 10)
                                editTextStartTime.setText("0" + selectedHour + ":0" + selectedMinute);
                            else if (selectedHour < 10)
                                editTextStartTime.setText("0" + selectedHour + ":" + selectedMinute);
                            else
                                editTextStartTime.setText(selectedHour + ":0" + selectedMinute);
                        } else
                            editTextStartTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        editTextEndTime = (EditText) findViewById(R.id.editext_mark_end_time);
        editTextEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                TimePickerDialog mTimePicker;
                //  Opens up a time picker dialog for the user to choose desired parking time.
                mTimePicker = new TimePickerDialog(ParkMarkActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (selectedHour < 10 || selectedMinute < 10) {
                            if (selectedHour < 10 && selectedMinute < 10)
                                editTextEndTime.setText("0" + selectedHour + ":0" + selectedMinute);
                            else if (selectedHour < 10)
                                editTextEndTime.setText("0" + selectedHour + ":" + selectedMinute);
                            else
                                editTextEndTime.setText(selectedHour + ":0" + selectedMinute);
                        } else
                            editTextEndTime.setText(selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
    }

    /**
     * Returns full address as a string.
     * @param address the address information.
     * @return full address string.
     */
    private String getFullAddress(List<Address> address) {
        if (address != null && address.size() > 0) {
            String string = new String();
            string += address.get(0).getAddressLine(0) + ",\n";
            string += address.get(0).getLocality() + ",\n";
            string += address.get(0).getCountryName();
            return string;
        }
        return null;
    }

    /**
     * Initializes the parking mark button.
     */
    private void initParkingMarkButton() {
        imageBbuttonMarkParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentLocation != null) {
                    Parking parking = null;
                    List<Address> addresses = getAddressFromLatLng(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
                    switch (spinnerParkingType.getSelectedItem().toString()) {
                        case PARKING_FREE:
                            parking = new Parking(currentLocation.getLatitude(), currentLocation.getLongitude(), getFullAddress(addresses));
                            firebaseObject.saveParking(parking, addresses);
                            Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_marked_toast_text, Toast.LENGTH_LONG).show();
                            break;
                        case PARKING_BLUE:
                            parking = new ParkingBlue(currentLocation.getLatitude(), currentLocation.getLongitude(), getFullAddress(addresses), parkingRules1);
                            if (parkingRules1.size() > 0) {
                                firebaseObject.saveParking(parking, addresses);
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_marked_toast_text, Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_add_rules_toast_text, Toast.LENGTH_LONG).show();
                            break;
                        case PARKING_BLUE_RESIDENTS:
                            if (!TextUtils.isEmpty(editTextLabel.getText().toString())) {
                                parking = new ParkingBlueResidents(currentLocation.getLatitude(), currentLocation.getLongitude(), getFullAddress(addresses),
                                        Integer.parseInt(editTextLabel.getText().toString()), parkingRules1, parkingRules2);
                                if (parkingRules1.size() > 0 || parkingRules2.size() > 0) {
                                    firebaseObject.saveParking(parking, addresses);
                                    Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_marked_toast_text, Toast.LENGTH_LONG).show();
                                }
                                else
                                    Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_add_rules_toast_text, Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_enter_label_toast_text, Toast.LENGTH_LONG).show();
                            break;
                        case PARKING_PARK_LOT:
                            if (!TextUtils.isEmpty(editTextPrice.getText().toString())) {
                                parking = new ParkingLot(currentLocation.getLatitude(), currentLocation.getLongitude(), getFullAddress(addresses), Double.parseDouble(editTextPrice.getText().toString()));
                                firebaseObject.saveParking(parking, addresses);
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_marked_toast_text, Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_enter_price_toast_text, Toast.LENGTH_LONG).show();
                            break;
                        case PARKING_SPECIAL:
                            parking = new ParkingSpecial(currentLocation.getLatitude(), currentLocation.getLongitude(), getFullAddress(addresses), parkingRules1);
                            if (parkingRules1.size() > 0) {
                                firebaseObject.saveParking(parking, addresses);
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_marked_toast_text, Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_add_rules_toast_text, Toast.LENGTH_LONG).show();
                            break;
                    }

                }
            }
        });
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
     * Determines the day of week.
     * @param day the day, in string.
     * @return the day, in int.
     */
    public static int determineDayOfWeek(String day) {
        switch (day) {
            case DAY_SUNDAY:
                return Calendar.SUNDAY;
            case DAY_MONDAY:
                return Calendar.MONDAY;
            case DAY_TUESDAY:
                return Calendar.TUESDAY;
            case DAY_WEDNESDAY:
                return Calendar.WEDNESDAY;
            case DAY_THURSDAY:
                return Calendar.THURSDAY;
            case DAY_FRIDAY:
                return Calendar.FRIDAY;
            case DAY_SATURDAY:
                return Calendar.SATURDAY;
        }
        return -1;
    }

    /**
     * Generates a Calendar object from a given time string.
     * @param time the time, in string format.
     * @return a calendar object.
     */
    private Calendar generateCalendarWithTime(String time) {
        Calendar calendar = new GregorianCalendar();
        String[] timeArr = time.split(":");
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeArr[0]));
        calendar.set(Calendar.MINUTE, Integer.parseInt(timeArr[1]));
        return calendar;
    }

    /**
     * Initializes the parking rule add button
     */
    private void initParkingRuleAddButton() {
        imageButtonAddParking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<ParkingRule> parkingRules = null;
                switch (spinnerParkingType.getSelectedItem().toString()) {
                    case PARKING_BLUE:
                        if (!TextUtils.isEmpty(editTextStartTime.getText().toString()) && !TextUtils.isEmpty(editTextEndTime.getText().toString())) {
                            parkingRules = createParkingRule();
                            if (parkingRules != null) {
                                if (parkingRules.size() > 0)
                                    parkingRules1.addAll(parkingRules);
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_rule_added_toast_text, Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                    case PARKING_BLUE_RESIDENTS:
                        if (!TextUtils.isEmpty(editTextStartTime.getText().toString()) && !TextUtils.isEmpty(editTextEndTime.getText().toString()) &&
                                !TextUtils.isEmpty(editTextLabel.getText().toString())) {
                            parkingRules = createParkingRule();
                            if (parkingRules != null) {
                                if (parkingRules.size() > 0) {
                                    if (checkboxResidents.isChecked())
                                        parkingRules2.addAll(parkingRules);
                                    else
                                        parkingRules1.addAll(parkingRules);
                                    Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_rule_added_toast_text, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                        break;
                    case PARKING_SPECIAL:
                        if (!TextUtils.isEmpty(editTextStartTime.getText().toString()) && !TextUtils.isEmpty(editTextEndTime.getText().toString())) {
                            parkingRules = createParkingRule();
                            if (parkingRules != null) {
                                if (parkingRules.size() > 0)
                                    parkingRules1.addAll(parkingRules);
                                Toast.makeText(ParkMarkActivity.this, R.string.mark_parking_rule_added_toast_text, Toast.LENGTH_LONG).show();
                            }
                        }
                        break;
                }
                if (parkingRules != null) {
                    if (parkingRules.size() > 0) {
                        for(ParkingRule parkingRule : parkingRules) {
                            String parkingRuleString = parkingRule.getParkingDay() + ": " + parkingRule.getParkingStartHour() + ":" +
                                    parkingRule.getParkingStartMinute() + " " + parkingRule.getParkingEndHour() + ":" + parkingRule.getParkingEndMinute();
                            parkingSpinnerRules.add(parkingRuleString);
                        }
                    }
                }
            }
        });
    }

    /**
     * Creates a parking rule, using the input the user has entered.
     * @return a ParkingRule object.
     */
    private ArrayList<ParkingRule> createParkingRule() {
        ArrayList<ParkingRule> parkingRules = new ArrayList<>();
        int parkingDay = determineDayOfWeek(spinnerParkingDays.getSelectedItem().toString());
        Calendar startingTime = generateCalendarWithTime(editTextStartTime.getText().toString());
        Calendar endingTime = generateCalendarWithTime(editTextEndTime.getText().toString());
        if (startingTime.before(endingTime)) {
            parkingRules.add(new ParkingRule(parkingDay, startingTime.get(Calendar.HOUR_OF_DAY), startingTime.get(Calendar.MINUTE),
                    endingTime.get(Calendar.HOUR_OF_DAY), endingTime.get(Calendar.MINUTE)));
        }
        else {
            Calendar day2start = generateCalendarWithTime("00:00");
            Calendar day1end = generateCalendarWithTime("23:59");
            int day2;
            if (parkingDay != 7)
                day2 = parkingDay + 1;
            else
                day2 = 1;
            parkingRules.add(new ParkingRule(parkingDay, startingTime.get(Calendar.HOUR_OF_DAY), startingTime.get(Calendar.MINUTE),
                    day1end.get(Calendar.HOUR_OF_DAY), day1end.get(Calendar.MINUTE)));
            parkingRules.add(new ParkingRule(day2, day2start.get(Calendar.HOUR_OF_DAY), day2start.get(Calendar.MINUTE),
                    endingTime.get(Calendar.HOUR_OF_DAY), endingTime.get(Calendar.MINUTE)));
        }
        return parkingRules;
    }

    /**
     * Initializes spinners.
     */
    private void initParkingSpinners() {
        initParkingDaysSpinner();
        initParkingTypeSpinner();
        initParkingRulesSpinner();
    }

    /**
     * Initializes the parking rules spinner.
     */
    private void initParkingDaysSpinner() {
        spinnerParkingDays = (Spinner) findViewById(R.id.spinner_parking_mark_day);

        parkingSpinnerDays = new ArrayList<>();
        parkingSpinnerDays.add(DAY_SUNDAY);
        parkingSpinnerDays.add(DAY_MONDAY);
        parkingSpinnerDays.add(DAY_TUESDAY);
        parkingSpinnerDays.add(DAY_WEDNESDAY);
        parkingSpinnerDays.add(DAY_THURSDAY);
        parkingSpinnerDays.add(DAY_FRIDAY);
        parkingSpinnerDays.add(DAY_SATURDAY);

        ArrayAdapter<String> parkingDaysAdapter = new ArrayAdapter<>(this, R.layout.spinner_text_layout, parkingSpinnerDays);
        spinnerParkingDays.setAdapter(parkingDaysAdapter);
    }

    /**
     * Initializes the parking type spinner.
     */
    private void initParkingTypeSpinner() {
        spinnerParkingType = (Spinner) findViewById(R.id.spinner_parking_mark_type);

        ArrayList<String> parkingTypes = new ArrayList<>();
        parkingTypes.add(PARKING_FREE);
        parkingTypes.add(PARKING_BLUE);
        parkingTypes.add(PARKING_BLUE_RESIDENTS);
        parkingTypes.add(PARKING_PARK_LOT);
        parkingTypes.add(PARKING_SPECIAL);

        ArrayAdapter<String> parkingTypeAdapter = new ArrayAdapter<>(this, R.layout.spinner_text_layout, parkingTypes);
        spinnerParkingType.setAdapter(parkingTypeAdapter);
        spinnerParkingType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String item = parent.getItemAtPosition(position).toString();
                LinearLayout layoutBelowType = (LinearLayout) findViewById(R.id.linearlayout_mark_below_type);
                LinearLayout layoutPrice = (LinearLayout) findViewById(R.id.linearlayout_mark_price);
                LinearLayout layoutDay = (LinearLayout) findViewById(R.id.linearlayout_mark_day);
                LinearLayout layoutStartTime = (LinearLayout) findViewById(R.id.linearlayout_mark_start_time);
                LinearLayout layoutEndTime = (LinearLayout) findViewById(R.id.linearlayout_mark_end_time);
                LinearLayout layoutRules = (LinearLayout) findViewById(R.id.linearlayout_mark_rules);
                LinearLayout layoutLabel = (LinearLayout) findViewById(R.id.linearlayout_mark_label);
                switch (item) {
                    case PARKING_FREE:
                        enableView(layoutBelowType, false);
                        enableView(layoutDay, false);
                        enableView(layoutStartTime, false);
                        enableView(layoutEndTime, false);
                        enableView(layoutRules, false);
                        enableView(imageButtonAddParking, false);
                        enableView(checkboxResidents, false);
                        enableView(layoutPrice, false);
                        enableView(layoutLabel, false);
                        break;
                    case PARKING_BLUE:
                        parkingRules1.clear();
                        parkingSpinnerRules.clear();
                        enableView(layoutBelowType, true);
                        enableView(layoutDay, true);
                        enableView(layoutStartTime, true);
                        enableView(layoutEndTime, true);
                        enableView(layoutRules, true);
                        enableView(imageButtonAddParking, true);
                        enableView(layoutPrice, false);
                        enableView(checkboxResidents, false);
                        enableView(layoutLabel, false);
                        break;
                    case PARKING_BLUE_RESIDENTS:
                        parkingRules1.clear();
                        parkingRules2.clear();
                        parkingSpinnerRules.clear();
                        enableView(layoutBelowType, true);
                        enableView(layoutDay, true);
                        enableView(layoutStartTime, true);
                        enableView(layoutEndTime, true);
                        enableView(layoutRules, true);
                        enableView(imageButtonAddParking, true);
                        enableView(layoutPrice, false);
                        enableView(checkboxResidents, true);
                        enableView(layoutLabel, true);
                        break;
                    case PARKING_PARK_LOT:
                        enableView(layoutBelowType, true);
                        enableView(layoutDay, false);
                        enableView(layoutStartTime, false);
                        enableView(layoutEndTime, false);
                        enableView(layoutRules, false);
                        enableView(imageButtonAddParking, false);
                        enableView(checkboxResidents, false);
                        enableView(layoutPrice, true);
                        enableView(layoutLabel, false);
                        break;
                    case PARKING_SPECIAL:
                        parkingRules1.clear();
                        parkingSpinnerRules.clear();
                        enableView(layoutBelowType, true);
                        enableView(layoutDay, true);
                        enableView(layoutStartTime, true);
                        enableView(layoutEndTime, true);
                        enableView(layoutRules, true);
                        enableView(imageButtonAddParking, true);
                        enableView(layoutPrice, false);
                        enableView(checkboxResidents, false);
                        enableView(layoutLabel, false);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    /**
     * Sets a given view's visibility and enabled / disables it.
     * @param view the view.
     * @param bool the boolean value.
     */
    private void enableView(View view, boolean bool) {
        view.setEnabled(bool);
        if (bool)
            view.setVisibility(View.VISIBLE);
        else
            view.setVisibility(View.INVISIBLE);
    }

    /**
     * Initializes the parking rules spinner.
     */
    private void initParkingRulesSpinner() {
        spinnerParkingRules = (Spinner) findViewById(R.id.spinner_parking_mark_rules);

        parkingSpinnerRules = new ArrayList<>();

        ArrayAdapter<String> parkingRulesAdapter = new ArrayAdapter<>(this, R.layout.spinner_text_layout, parkingSpinnerRules);
        spinnerParkingRules.setAdapter(parkingRulesAdapter);
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
                bindService(new Intent(ParkMarkActivity.this, GPSTrackerService.class), serviceConnection, Context.BIND_AUTO_CREATE);
            startListeningToGps();
        }
    }

    private void startAzimutService() {
        bindService(new Intent(ParkMarkActivity.this, AzimutService.class), serviceConnection, Context.BIND_AUTO_CREATE);
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
            gpsTrackerService.setSettings(ParkSearchActivity.MIN_TIME_BW_UPDATES, ParkSearchActivity.MIN_DISTANCE_CHANGE_FOR_UPDATES, false);
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
        if (currentLocationMarker != null) {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            currentLocationMarker.setPosition(latLng);
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, ParkSearchActivity.MAP_ZOOM_DEFAULT));
        }
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
            transaction.replace(R.id.fragment_park_mark_map, mapFragment);
            transaction.commit();
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    if (gpsTrackerService != null)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), ParkSearchActivity.MAP_ZOOM_DEFAULT));
                    gMap = googleMap;
                    addUserMarker();
                }
            });
        }
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
