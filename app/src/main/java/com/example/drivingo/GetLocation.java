package com.example.drivingo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GetLocation extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Location mLastLocation;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private Marker marker;
    private Button btnSet;
    private double lat,lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            lat = bundle.getDouble("latitude");
            lng = bundle.getDouble("longitude");
        }
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        btnSet = findViewById(R.id.setButton);
        btnSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                data.putExtra("Latitude",marker.getPosition().latitude);
                data.putExtra("Longitude",marker.getPosition().longitude);
                setResult(RESULT_OK,data);
                finish();
            }
        });
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        getDeviceLocation();
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                marker.setPosition(latLng);
            }
        });

    }

    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            /*mFusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    mLastLocation = location;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(mLastLocation.getLatitude(),
                                    mLastLocation.getLongitude()), 17));
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(new LatLng(mLastLocation.getLatitude(),
                            mLastLocation.getLongitude()));
                    marker = mMap.addMarker(markerOptions);
                }
            });*/
            LatLng latLng = new LatLng(lat,lng);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    latLng, 17));
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(latLng);
            marker = mMap.addMarker(markerOptions);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
