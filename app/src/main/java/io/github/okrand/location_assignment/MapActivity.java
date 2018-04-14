package io.github.okrand.location_assignment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.location.LocationListener;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener, OnMapReadyCallback {
    private static String TAG = "MAP";
    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Location loc;

    final AppDatabase db = Room.databaseBuilder(this,
            AppDatabase.class, "checkins").allowMainThreadQueries().build();
    List<Loc> theList = new ArrayList<>();
    Loc L = new Loc();
    Button addButton;
    Button saveButton;
    Button discButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        theList = db.locDao().getAll();
        addButton=findViewById(R.id.add_marker);
        saveButton=findViewById(R.id.save_marker);
        discButton=findViewById(R.id.discard_marker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_SHORT).show();
        updateLocation(location);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        UiSettings uiSettings = mMap.getUiSettings();
        uiSettings.setMyLocationButtonEnabled(true);
        uiSettings.setAllGesturesEnabled(true);
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setCompassEnabled(true);

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                LatLng newPos = marker.getPosition();
                L.setLatitude(newPos.latitude);
                L.setLongitude(newPos.longitude);
            }
        });

        populateMap();
        locationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                loc = location;
                updateLocation(location);
                locationManager.removeUpdates(this);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"SET MY LOCATION ENABLED");
            mMap.setMyLocationEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }
        Criteria criteria = new Criteria();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String bestProvider = locationManager.getBestProvider(criteria, true);
        locationManager.requestLocationUpdates(bestProvider, 400, 0, locationListener);
        loc = locationManager.getLastKnownLocation(bestProvider);
        if(loc != null) {
            updateLocation(loc);
        }else{
            Toast.makeText(this, "CURRENT LOCATION UNAVAILABLE", Toast.LENGTH_SHORT).show();
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
                discButton.setVisibility(View.GONE);
                Toast.makeText(MapActivity.this, L.getName() + "\" added", Toast.LENGTH_SHORT).show();
                addLoc();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButton.setVisibility(View.GONE);
                saveButton.setVisibility(View.VISIBLE);
                discButton.setVisibility(View.VISIBLE);
                LatLng myLatLng = new LatLng(loc.getLatitude(),loc.getLongitude());
                PopUpNew(myLatLng);
            }
        });
        discButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
                discButton.setVisibility(View.GONE);
                L = new Loc();
            }
        });
    }
    public void updateLocation(Location location){
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
    }

    public void populateMap(){
        mMap.clear();
        if(theList.isEmpty()){
            return;
        }
        for(int i = 0; i < theList.size(); i++){
            LatLng latlng = new LatLng(theList.get(i).getLatitude(), theList.get(i).getLongitude());
            mMap.addMarker(new MarkerOptions().position(latlng)).setTitle(theList.get(i).getName());
        }
    }

    @SuppressLint("ResourceAsColor")
    public void PopUpNew(final LatLng latLng){
        //Get Location name from user as an alert
        AlertDialog.Builder alert = new AlertDialog.Builder(MapActivity.this,android.R.style.Theme_Material_Dialog_Alert);
        alert.setTitle("Location Name:");

        final EditText input = new EditText(this);
        input.setBackgroundColor(getResources().getColor(R.color.white, null));
        input.requestFocus();
        alert.setView(input);
        alert.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                L.setName(input.getText().toString());
                addMarker(latLng);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int button) {
                addButton.setVisibility(View.VISIBLE);
                saveButton.setVisibility(View.GONE);
                discButton.setVisibility(View.GONE);
            }
        });
        alert.show();
    }

    private void startIntentService(Location location) {
        AddressResultReceiver mResultReceiver = new AddressResultReceiver(new Handler());
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location);
        startService(intent);
    }

    public void addLoc(){
        db.locDao().insertAll(L);
        theList.add(L);
        L = new Loc();
        populateMap();
    }

    public void addMarker(LatLng latLng){
        mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)).draggable(true)).setTitle(L.getName());
        L.setLatitude(latLng.latitude);
        L.setLongitude(latLng.longitude);
        Location location = new Location("");
        location.setLongitude(latLng.longitude);
        location.setLatitude(latLng.latitude);
        startIntentService(location);
    }

    /**
     * ResultReceiver for the current address.
     */
    private class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                String addr = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
                L.setAddress(addr);
            }
        }
    }

}
