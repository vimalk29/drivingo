package com.example.drivingo;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.drivingo.model.Bike;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient client;
    TextView tvLocation;
    RecyclerView recyclerView;
    BikeAdapter adapter;
    Button BtnRefresh;
    ProgressBar progressBar;
    LinearLayout LocationLayout;
    double lat,lng;
    List<Bike> bikeList = new ArrayList<>();
    FirebaseDatabase database;
    DatabaseReference databaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocation = findViewById(R.id.tvLocation);
        recyclerView = findViewById(R.id.recyclerView);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Bikes");
        BtnRefresh = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        LocationLayout = findViewById(R.id.layout_location);
        LocationLayout.setEnabled(false);
        LocationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "open Maps", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,GetLocation.class);
                intent.putExtra("latitude",lat);
                intent.putExtra("longitude",lng);
                startActivityForResult(intent,3);
            }
        });
        BtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getLocation();
            }
        });
        getLocation();

        adapter = new BikeAdapter(bikeList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
    private void requestPermission() {
        //denied with never ask again
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            notifyUser(true);
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 1:{
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                    // permission denied
                    notifyUser(false);
                }
            }
        }
    }

    private void getLocation() {
        BtnRefresh.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        LocationLayout.setEnabled(false);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            BtnRefresh.setEnabled(true);
            progressBar.setVisibility(View.GONE);
            return;
        }
        client.getLastLocation().addOnSuccessListener(MainActivity.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try{
                    if(location !=null){
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Geocoder geocoder = new Geocoder(MainActivity.this,Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
                        String Address = addresses.get(0).getAddressLine(0);
                        String City = addresses.get(0).getLocality();
                        tvLocation.setText(Address);
                        loadData(lat,lng);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
    }

    private void loadData(Double lat,Double lng) {
        this.lat = lat;
        this.lng = lng;
        if(bikeList.size()>0)
            bikeList.clear();
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Bike bike = postSnapshot.getValue(Bike.class);
                    if(bike !=null&& bike.isIsAvailable()){
                        double distance = calculateDistance(bike.getLocation().get("latitude"),bike.getLocation().get("longitude"));
                        bike.setDistance(distance);
                        bikeList.add(bike);
                    }
                }
                afterLoadingData();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                BtnRefresh.setEnabled(true);
                progressBar.setVisibility(View.GONE);
                LocationLayout.setEnabled(true);
            }
        });

    }

    private double calculateDistance(Double latitude, Double longitude) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = lat;//current location
        double lat2 = latitude;//bike location
        double lon1 = lng;//current location
        double lon2 = longitude;//bike location
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        /*double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                + " Meter   " + meterInDec);
        */
        return Radius * c;
    }

    private void afterLoadingData() {
        Collections.sort(bikeList);//sorted in ascending order according to distance
        adapter.notifyDataSetChanged();
        BtnRefresh.setEnabled(true);
        progressBar.setVisibility(View.GONE);
        LocationLayout.setEnabled(true);
    }

    private void notifyUser(final boolean openSetting){
        String msg="Please provide permission to continue";
        AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setMessage(msg)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(openSetting) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", getPackageName(), null);
                            intent.setData(uri);
                            startActivityForResult(intent, 2);
                        }
                        else
                            requestPermission();
                    }
                })
                .setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
        ;
        AlertDialog alert = builder.create();
        if(!this.isFinishing())
            alert.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==3){
            if(resultCode==RESULT_OK){
                if(data!=null){
                    try{
                        lat = data.getDoubleExtra("Latitude",0);
                        lng = data.getDoubleExtra("Longitude",0);
                        Geocoder geocoder = new Geocoder(MainActivity.this,Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
                        String Address = addresses.get(0).getAddressLine(0);
                        String City = addresses.get(0).getLocality();
                        tvLocation.setText(Address);
                        loadData(lat,lng);
                        }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            return;
        }

        getLocation();
    }
}
