package io.github.okrand.location_assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 12;
    double latitude;
    double longitude;
    String addresss;
    ArrayAdapter<MinLoc> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        while (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_FINE_LOCATION);
        }
        startLocationUpdates();

        //Create DB
        final AppDatabase db = Room.databaseBuilder(this,
                AppDatabase.class, "checkins").allowMainThreadQueries().build();

//        while(db.locDao().getAll().size() > 0)
//             db.locDao().delete(db.locDao().getAll().get(0));   //Empty database. Used for testing

        final List<Loc> theList = db.locDao().getAll();
        final List<MinLoc> theMinList = new ArrayList<>();

        //populate MinLoc list from the actual locations
        //MinLoc is a simpler class that contains an entry for each check in
        for (Loc l : theList) {
            MinLoc m1 = new MinLoc();
            m1.name = l.getName();
            m1.address = l.getAddress();
            m1.time = l.getTime();
            theMinList.add(m1);
            if (l.getChecklats() != null && !l.getChecklats().equals("")) {
                List<String> checkLats = Arrays.asList(l.getChecklats().split(","));
                List<String> checkLons = Arrays.asList(l.getChecklons().split(","));
                List<String> checkTimes = Arrays.asList(l.getChecktimes().split(","));
                int siz = checkLats.size();
                for (int i = 0; i < siz; i++) {
                    MinLoc m = new MinLoc();
                    m.name = l.getName();
                    m.address = l.getAddress();
                    m.lat = checkLats.get(i);
                    m.lon = checkLons.get(i);
                    m.time = checkTimes.get(i);
                    theMinList.add(m);
                }
            }
        }

        final Button checkin = findViewById(R.id.button_checkin);
        checkin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean addToExisting = false;
                EditText name = findViewById(R.id.edit_checkin);
                Loc checkthis = new Loc();
                MinLoc minthis = new MinLoc();
                checkthis.setLatitude(latitude);
                checkthis.setLongitude(longitude);
                minthis.lat = String.valueOf(latitude);
                minthis.lon = String.valueOf(longitude);
                String t = Calendar.getInstance().getTime().toString();
                Log.d("TIME", t);
                minthis.time = t;
                for (Loc l : theList) {
                    if (l.equals(checkthis)) {
                        addToExisting = true;
                        l.addChecklat(String.valueOf(latitude));
                        l.addChecklon(String.valueOf(longitude));
                        l.addChecktime(t);
                        minthis.name = l.getName();
                        minthis.address = l.getAddress();
                        db.locDao().updateLoc(l);
                        break;
                    }
                }
                if (!addToExisting){
                    checkthis.setName(name.getText().toString());
                    checkthis.setAddress(addresss);
                    checkthis.setTime(t);
                    minthis.address = checkthis.getAddress();
                    minthis.name = checkthis.getName();
                    //insert into database
                    db.locDao().insertAll(checkthis);
                    theList.add(checkthis);
                }
                theMinList.add(minthis);
                name.setText(""); //clear data entered
                adapter.notifyDataSetChanged();
            }
        });

        //update list
        adapter = new ArrayAdapter<MinLoc>(this, R.layout.list_item_location, R.id.item_name, theMinList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final View view = super.getView(position, convertView, parent);
                TextView nam = view.findViewById(R.id.item_name);
                TextView addr = view.findViewById(R.id.item_address);
                TextView tim = view.findViewById(R.id.item_time);
                MinLoc l = theMinList.get(position);
                nam.setText(l.name);
                addr.setText(l.address);
                tim.setText(l.time);
                return view;
            }
        };
        ListView listView = findViewById(R.id.listview_checks);
        listView.setAdapter(adapter);

        Button goToMap = findViewById(R.id.button_map);
        goToMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                Bundle bundle = new Bundle();
                //bundle.putParcelableList("theList", theList);
                //intent.putExtras(bundle);
                startActivity(intent);
            }
        });
    }

    public void newLocation(Location location) {
        TextView lat = findViewById(R.id.lat);
        TextView lon = findViewById(R.id.lon);
        TextView address = findViewById(R.id.address);
        longitude = location.getLongitude();
        latitude = location.getLatitude();
        String lontext = "Longitude: " + Double.toString(longitude);
        String lattext = "Latitude: " + Double.toString(latitude);
        lon.setText(lontext);
        lat.setText(lattext);

        //Geocoder
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            Address addr = geocoder.getFromLocation(latitude, longitude, 1).get(0);
            addresss = addr.getAddressLine(0);
            String addrtext = "Address: " + addresss;
            address.setText(addrtext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // Trigger new location updates at interval
    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {
        // Acquire a reference to the system Location Manager
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        final LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Log.d("NEW LOCATION", location.toString());
                // Called when a new location is found by the network location provider.
                newLocation(location);
                Log.d("ACCURACY", String.valueOf(location.getAccuracy()));
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 0, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
    }
}
