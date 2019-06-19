package com.example.drivingo.Activities;

import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.drivingo.Adapters.BikeAdapter;
import com.example.drivingo.R;
import com.example.drivingo.model.Bike;
import com.example.drivingo.model.Booking;
import com.example.drivingo.Common.CommonValues;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FindBikes extends AppCompatActivity {
    FusedLocationProviderClient client;
    TextView tvLocation;
    RecyclerView recyclerView;
    BikeAdapter adapter;
    double lat, lng;
    Date dateFrom, dateTo;
    List<Bike> bikeList = new ArrayList<>();
    List<Bike> bikeListFinal = new ArrayList<>();
    FirebaseDatabase database;
    DatabaseReference databaseReference, bookingReference;
    SwipeRefreshLayout swipeRefreshLayout;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault());
    SimpleDateFormat preBookTimeFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss", Locale.getDefault());
    long TIME_OUT = 10*60000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_bikes);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Select Bike");
        }
        tvLocation = findViewById(R.id.tvLocation);
        recyclerView = findViewById(R.id.recyclerView);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Bikes");
        bookingReference = database.getReference("Booking");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                lat = bundle.getDouble("latitude");
                lng = bundle.getDouble("longitude");
                dateFrom = dateFormat.parse(bundle.getString("from"));
                dateTo = dateFormat.parse(bundle.getString("to"));
                CommonValues.FromDate = dateFrom;
                CommonValues.ToDate = dateTo;
                String city = bundle.getString("city");
                tvLocation.setText(city);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        adapter = new BikeAdapter(bikeListFinal);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        swipeRefreshLayout = findViewById(R.id.findBikesLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(lat, lng);
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                loadData(lat, lng);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData(lat, lng);
    }

    private void loadData(final double lat,final double lng) {
        swipeRefreshLayout.setRefreshing(true);
        this.lat = lat;
        this.lng = lng;
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (bikeList.size() > 0)
                    bikeList.clear();
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Bike bike = postSnapshot.getValue(Bike.class);
                    try {
                        if (bike != null && bike.isIsAvailable()) {
                            Date preBookTime = preBookTimeFormat.parse(bike.getPreBookTime());
                            Date timeNow = Calendar.getInstance().getTime();
                            long diff = timeNow.getTime() - preBookTime.getTime();
                            if(diff>TIME_OUT){
                                double distance = calculateDistance(lat,lng,bike.getLocation().get("latitude"), bike.getLocation().get("longitude"));
                                bike.setDistance(distance);
                                bikeList.add(bike);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                //  finalBikeList();
                afterLoadingData();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public static double calculateDistance(double sLat,double sLng,Double dLatitude, Double dLongitude) {
        int Radius = 6371;// radius of earth in Km
        double lat2 = dLatitude;//bike location
        double lon2 = dLongitude;//bike location
        double dLat = Math.toRadians(lat2 - sLat);
        double dLon = Math.toRadians(lon2 - sLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(sLat))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return Radius * c;
    }

    private void afterLoadingData() {
        if (bikeListFinal.size() > 0)
            bikeListFinal.clear();
        bikeListFinal.addAll(bikeList);
        Collections.sort(bikeListFinal);//sorted in ascending order according to distance
        adapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void finalBikeList() {
        bookingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (bikeListFinal.size() > 0)
                    bikeListFinal.clear();
                for (Bike bike : bikeList) {
                    if (dataSnapshot.child(bike.getBikeNo()).exists()) {
                        boolean temp = true;
                        for (DataSnapshot postSnapShot : dataSnapshot.child(bike.getBikeNo()).getChildren()) {
                            Booking booking = postSnapShot.getValue(Booking.class);
                            if (booking != null)
                                try {
                                    Date from = dateFormat.parse(booking.getFrom());
                                    Date to = dateFormat.parse(booking.getTo());
                                    if (!(dateTo.before(from) || dateFrom.after(to))) {
                                        temp = false;
                                        break;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                        }
                        if (temp)
                            bikeListFinal.add(bike);
                    } else {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
