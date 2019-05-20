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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.drivingo.model.Bike;
import com.example.drivingo.model.Booking;
import com.example.drivingo.model.CommonValues;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    FusedLocationProviderClient client;
    TextView tvLocation;
    RecyclerView recyclerView;
    BikeAdapter adapter;
    Button BtnRefresh;
    ProgressBar progressBar;
    double lat,lng;
    Date dateFrom,dateTo;
    List<Bike> bikeList = new ArrayList<>();
    List<Bike> bikeListFinal = new ArrayList<>();
    FirebaseDatabase database;
    DatabaseReference databaseReference,bookingReference;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvLocation = findViewById(R.id.tvLocation);
        recyclerView = findViewById(R.id.recyclerView);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Bikes");
        bookingReference = database.getReference("Booking");
        BtnRefresh = findViewById(R.id.button);
        progressBar = findViewById(R.id.progressBar);
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            try{
                lat = bundle.getDouble("latitude");
                lng = bundle.getDouble("longitude");
                dateFrom = dateFormat.parse(bundle.getString("from"));
                dateTo = dateFormat.parse(bundle.getString("to"));
                CommonValues.FromDate = dateFrom;
                CommonValues.ToDate = dateTo;
                String city = bundle.getString("city");
                tvLocation.setText(city);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        BtnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadData(lat,lng);
            }
        });
       // loadData(lat,lng);
        adapter = new BikeAdapter(bikeListFinal);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(lat,lng);
    }

    private void loadData(Double lat, Double lng) {
        BtnRefresh.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        this.lat = lat;
        this.lng = lng;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(bikeList.size()>0)
                    bikeList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Bike bike = postSnapshot.getValue(Bike.class);
                    if(bike !=null&& bike.isIsAvailable()){
                        double distance = calculateDistance(bike.getLocation().get("latitude"),bike.getLocation().get("longitude"));
                        bike.setDistance(distance);
                        bikeList.add(bike);
                    }
                }
                finalBikeList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                BtnRefresh.setEnabled(true);
                progressBar.setVisibility(View.GONE);
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
        return Radius * c;
    }

    private void afterLoadingData() {
        Collections.sort(bikeListFinal);//sorted in ascending order according to distance
        adapter.notifyDataSetChanged();
        BtnRefresh.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }
    private void finalBikeList(){
        bookingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(bikeListFinal.size()>0)
                    bikeListFinal.clear();
                for(Bike bike:bikeList){
                    if(dataSnapshot.child(bike.getBikeNo()).exists()){
                        boolean temp=true;
                        for(DataSnapshot postSnapShot:dataSnapshot.child(bike.getBikeNo()).getChildren()){
                            Booking booking = postSnapShot.getValue(Booking.class);
                            if(booking!=null)
                                try{
                                    Date from = dateFormat.parse(booking.getFrom());
                                    Date to = dateFormat.parse(booking.getTo());
                                    if(!(dateTo.before(from)||dateFrom.after(to))){
                                        temp=false;
                                        break;
                                    }
                                }catch (Exception e){
                                    e.printStackTrace();
                                }
                        }
                        if(temp)
                            bikeListFinal.add(bike);
                    }else{
                        bikeListFinal.add(bike);
                    }
                }
                afterLoadingData();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
