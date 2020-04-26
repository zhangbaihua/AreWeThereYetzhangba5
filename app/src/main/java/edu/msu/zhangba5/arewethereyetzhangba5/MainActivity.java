package edu.msu.zhangba5.arewethereyetzhangba5;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private LocationManager locationManager = null;
    private SharedPreferences settings = null;
    private final static String TO = "to";
    private final static String TOLAT = "tolat";
    private final static String TOLONG = "tolong";
    private double latitude = 0;
    private double longitude = 0;
    private boolean valid = false;
    private ActiveListener activeListener = new ActiveListener();
    private double toLatitude = 0;
    private double toLongitude = 0;
    private String to = "";
    private Spinner spinner;
    private String[] ModeList;
    private ArrayAdapter<String> adapter;
    private int TMode = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = PreferenceManager.getDefaultSharedPreferences(this);
        to = settings.getString(TO, "2250 Engineering");

        toLatitude = Double.parseDouble(settings.getString(TOLAT, "42.724303"));
        toLongitude = Double.parseDouble(settings.getString(TOLONG, "-84.480507"));

        latitude = 42.731138;
        longitude = -84.487508;
        valid = true;
        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Force the screen to say on and bright
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //Spinner set up
        spinner = (Spinner) findViewById(R.id.Spinner);
        ModeList = getResources().getStringArray(R.array.Mode);


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, ModeList);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int pos, long id) {

                TMode = pos;//1 for Driving, 2 for Walking, 3 for Bicycling
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

    }

    private void registerListeners() {
        unregisterListeners();

        // Create a Criteria object
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(true);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(false);

        String bestAvailable = locationManager.getBestProvider(criteria, true);

        if (bestAvailable != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(bestAvailable, 500, 1, activeListener);
            TextView viewProvider = (TextView) findViewById(R.id.textProvider);
            viewProvider.setText(bestAvailable);
            Location location = locationManager.getLastKnownLocation(bestAvailable);
            onLocation(location);
        }
    }


    private void onLocation(Location location) {
        if (location == null) {
            return;
        }

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        valid = true;

        setUI();
    }

    private void unregisterListeners() {
        locationManager.removeUpdates(activeListener);
    }

    /**
     * Set all user interface components to the current state
     */
    private void setUI() {
        TextView temp = (TextView) findViewById(R.id.textTo);
        temp.setText(to);
        if (!valid) {
            TextView temp1 = (TextView) findViewById(R.id.textLatitude);
            temp1.setText(" ");
            TextView temp2 = (TextView) findViewById(R.id.textDistance);
            temp2.setText(" ");
            TextView temp3 = (TextView) findViewById(R.id.textLongitude);
            temp3.setText(" ");
        }
        if (valid) {
            TextView temp1 = (TextView) findViewById(R.id.textLatitude);
            temp1.setText(String.valueOf(latitude));
            TextView temp2 = (TextView) findViewById(R.id.textLongitude);
            temp2.setText(String.valueOf(longitude));


            Location locationA = new Location("A");

            locationA.setLatitude(latitude);
            locationA.setLongitude(longitude);

            Location locationB = new Location("B");

            locationB.setLatitude(toLatitude);
            locationB.setLongitude(toLongitude);

            float distance = locationA.distanceTo(locationB);

            TextView temp3 = (TextView) findViewById(R.id.textDistance);
            temp3.setText(String.format("%1$6.1fm", distance));
        }
    }


    /**
     * Called when this application becomes foreground again.
     */
    @Override
    protected void onResume() {
        super.onResume();

        TextView viewProvider = (TextView) findViewById(R.id.textProvider);
        viewProvider.setText("");

        setUI();
        registerListeners();
    }

    /**
     * Called when this application is no longer the foreground application.
     */
    @Override
    protected void onPause() {
        unregisterListeners();
        super.onPause();

    }

    public void onNew(View view) {

        EditText location = (EditText) findViewById(R.id.editLocation);
        final String address = location.getText().toString().trim();
        newAddress(address);
    }

    private void newAddress(final String address) {
        if (address.equals("")) {
            // Don't do anything if the address is blank
            return;
        }
        new Thread(new Runnable() {

            @Override
            public void run() {
                lookupAddress(address);

            }

        }).start();
    }

    private class ActiveListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            onLocation(location);
        }

        @Override
        public void onStatusChanged(String s, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {
            registerListeners();
        }
    }

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.itemSparty:
                newTo("Sparty", 42.731138, -84.487508);
                return true;

            case R.id.itemHome:
                newTo("home", 42.731138, -84.480541);
                return true;

            case R.id.item2250:
                newTo("2250 Engineering", 42.724303, -84.480507);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Handle setting a new "to" location.
     *
     * @param address Address to display
     * @param lat     latitude
     * @param lon     longitude
     */
    private void newTo(String address, double lat, double lon) {
        to = address;
        toLatitude = lat;
        toLongitude = lon;

        setUI();

        SharedPreferences.Editor editor = settings.edit();
        editor.putString(TOLAT, Double.toString(lat));
        editor.putString(TOLONG, Double.toString(lon));
        editor.putString(TO, address);
        editor.apply();
    }

    /**
     * Look up the provided address. This works in a thread!
     *
     * @param address Address we are looking up
     */
    private void lookupAddress(final String address) {

        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.US);
        boolean exception = false;
        List<Address> locations;
        try {
            locations = geocoder.getFromLocationName(address, 1);
        } catch (IOException ex) {
            // Failed due to I/O exception
            locations = null;
            exception = true;
        }

        final boolean tempException = exception;
        final List<Address> tempAddress = locations;

        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                newLocation(address, tempException, tempAddress);
            }
        });
    }

    private void newLocation(String address, boolean exception, List<Address> locations) {

        if (exception) {
            Toast.makeText(MainActivity.this, R.string.exception, Toast.LENGTH_SHORT).show();
        } else {
            if (locations == null || locations.size() == 0) {
                Toast.makeText(this, R.string.couldnotfind, Toast.LENGTH_SHORT).show();
                return;
            }

            EditText location = (EditText) findViewById(R.id.editLocation);
            location.setText("");

            // We have a valid new location
            Address a = locations.get(0);
            newTo(address, a.getLatitude(), a.getLongitude());

        }
    }

    public void onFavorite(View view) {

    }

    public void onRoute(View view){
        String Transportation_mode = "";
        if (TMode == 0){
            Transportation_mode = "d";
        }
        else if (TMode == 1){
            Transportation_mode = "w";
        }
        else if (TMode == 2){
            Transportation_mode = "b";
        }
        settings = PreferenceManager.getDefaultSharedPreferences(this);
        String lat = settings.getString(TOLAT,"42.731138");
        String lon = settings.getString(TOLONG,"-84.480507");
        // Create a Uri from an intent string. Use the result to create an Intent.
        Uri gmmIntentUri = Uri.parse("google.navigation:q="+lat+","+lon+"&mode="+Transportation_mode);

// Create an Intent from gmmIntentUri. Set the action to ACTION_VIEW
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
// Make the Intent explicit by setting the Google Maps package
        mapIntent.setPackage("com.google.android.apps.maps");

// Attempt to start an activity that can handle the Intent
        startActivity(mapIntent);
    }
}

