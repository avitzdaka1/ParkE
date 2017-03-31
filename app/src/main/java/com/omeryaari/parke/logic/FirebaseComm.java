package com.omeryaari.parke.logic;

import android.location.Address;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible of the communication between the app to the Firebase database.
 */
public class FirebaseComm {

    public static final String FIREBASE_PARKING_LOCATION = "Parkings";
    public static final String FIREBASE_PAID_PARKING_LOCATION = "Paid";
    public static final String FIREBASE_PAID_RESIDENTS_PARKING_LOCATION = "PaidResidents";
    public static final String FIREBASE_FREE_PARKING_LOCATION = "Free";
    public static final String FIREBASE_PARKING_LOT_PARKING_LOCATION = "ParkingLot";
    public static final String FIREBASE_EXCEPTION_PARKING_LOCATION = "Exception";
    private DatabaseReference database;
    private ArrayList<Parking> parkingList;

    public FirebaseComm() {
        database = FirebaseDatabase.getInstance().getReference();
        parkingList = new ArrayList<>();
    }

    /**
     * Returns a list of parking spots, given address, distance, and desired parking type.
     * @param addresses the address information.
     * @param type the parking type (free / paid / parking lot).
     * @return a list of parking spots.
     */
    public List<Parking> getParkings(List<Address> addresses, String type, boolean hasAreaLabel) {
        parkingList.clear();
        String country = addresses.get(0).getCountryName();
        String city = addresses.get(0).getLocality();
        String address = addresses.get(0).getAddressLine(0);
        switch (type) {
            case FIREBASE_FREE_PARKING_LOCATION:
                downloadFreeParkings(country, city);
                downloadBlueParkings(country, city);
                downloadSpecialParkings(country, city);
                if (hasAreaLabel)
                    downloadBlueResidentsParkings(country, city);
                break;
            case FIREBASE_PAID_PARKING_LOCATION:
                downloadBlueParkings(country, city);
                downloadBlueResidentsParkings(country, city);
                break;
            case FIREBASE_PARKING_LOT_PARKING_LOCATION:
                downloadParkingLots(country, city);
                break;
        }
        return parkingList;
    }

    /**
     * Saves a given parking in the Firebase database.
     * @param parking the given parking.
     * @param addresses the address information.
     * @return whether the operation succeeded.
     */
    public boolean saveParking(Parking parking, List<Address> addresses) {
        if (addresses != null && addresses.size() > 0) {
            String country = addresses.get(0).getCountryName();
            String city = addresses.get(0).getLocality();
            String parkingType = checkParkingType(parking);

            parking.setFirebaseKey(database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).child(parkingType).push().getKey());
            database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).child(parkingType).child(parking.getFirebaseKey()).setValue(parking);
            return true;
        }
        return false;
    }

    /**
     * Updates a given parking to have its reported flag set to true.
     * @param country parking's country.
     * @param city parking's city.
     * @param parking parking to report.
     */
    public void reportParking(String country, String city, Parking parking) {
        String parkingType = checkParkingType(parking);
        parking.setReported(true);
        database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).child(parkingType).child(parking.getFirebaseKey()).setValue(parking);
    }

    /**
     * Checks the class of the parking instance object.
     * @param parking the Parking object.
     * @return the Parking's class.
     */
    private String checkParkingType(Parking parking) {
        if (parking instanceof ParkingBlue)
            return FIREBASE_PAID_PARKING_LOCATION;
        else if (parking instanceof ParkingBlueResidents)
            return FIREBASE_PAID_RESIDENTS_PARKING_LOCATION;
        else if (parking instanceof ParkingLot)
            return FIREBASE_PARKING_LOT_PARKING_LOCATION;
        else if (parking instanceof ParkingWithException)
            return FIREBASE_EXCEPTION_PARKING_LOCATION;
        else
            return FIREBASE_FREE_PARKING_LOCATION;
    }

    /**
     * Downloads "regular" paid (blue) parking spots from the Firebase database.
     * @param country the given country to download parking spots from.
     * @param city the given city to download parking spots from.
     */
    private void downloadBlueParkings(String country, String city) {
        database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    ParkingBlue tempParking = snap.getValue(ParkingBlue.class);
                    parkingList.add(tempParking);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Downloads paid (blue residents) parking spots from the Firebase database.
     * @param country the given country to download parking spots from.
     * @param city the given city to download parking spots from.
     */
    private void downloadBlueResidentsParkings(String country, String city) {
        database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    ParkingBlueResidents tempParking = snap.getValue(ParkingBlueResidents.class);
                    parkingList.add(tempParking);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Downloads parking lots from the Firebase database.
     * @param country the given country to download parking lots from.
     * @param city the given city to download parking lots from.
     */
    private void downloadParkingLots(String country, String city) {
        database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    ParkingLot tempParking = snap.getValue(ParkingLot.class);
                    parkingList.add(tempParking);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Downloads special parking spots from the Firebase database.
     * @param country the given country to download parking spots from.
     * @param city the given city to download parking spots from.
     */
    private void downloadSpecialParkings(String country, String city) {
        database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    ParkingWithException tempParking = snap.getValue(ParkingWithException.class);
                    parkingList.add(tempParking);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Downloads free parking spots from the Firebase database.
     * @param country the given country to download parking spots from.
     * @param city the given city to download parking spots from.
     */
    private void downloadFreeParkings(String country, String city) {
        database.child(FIREBASE_PARKING_LOCATION).child(country).child(city).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap : dataSnapshot.getChildren()) {
                    Parking tempParking = snap.getValue(Parking.class);
                    parkingList.add(tempParking);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}