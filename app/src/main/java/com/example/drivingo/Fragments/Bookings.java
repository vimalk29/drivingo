package com.example.drivingo.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.drivingo.Activities.FindBikes;
import com.example.drivingo.Adapters.BookedBikeAdapter;
import com.example.drivingo.R;
import com.example.drivingo.model.Bike;
import com.example.drivingo.model.BookedBike;
import com.example.drivingo.model.Booking;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Bookings extends Fragment implements View.OnClickListener{
    private View myFragment;
    private Context context;
    private OnFragmentInteractionListener mListener;
    private RecyclerView recyclerView;
    private BookedBikeAdapter adapter;
    private List<BookedBike> bikeList = new ArrayList<>();
    private List<Bike> allBikes = new ArrayList<>();
    private FirebaseUser user;
    private String UID;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference, bookingReference;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FusedLocationProviderClient client;
    private LinearLayout noDataFoundContainer;
    private Button btnBookABike;
    private double lat, lng;

    public Bookings() {
        // Required empty public constructor
    }

    public static Bookings newInstance() {
        return new Bookings();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragment = inflater.inflate(R.layout.fragment_booked_bikes, container, false);
        recyclerView = myFragment.findViewById(R.id.bookedRecycler);
        btnBookABike = myFragment.findViewById(R.id.btnBookABike);
        noDataFoundContainer = myFragment.findViewById(R.id.no_data_found_container);
        database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)
            UID = user.getUid();
        databaseReference = database.getReference("Bikes");
        bookingReference = database.getReference("Booking");
        noDataFoundVisibility(false);
        btnBookABike.setOnClickListener(this);
        adapter = new BookedBikeAdapter(context, bikeList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        swipeRefreshLayout = myFragment.findViewById(R.id.bookedContainer);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getLocation();
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                getLocation();
            }
        });
        return myFragment;
    }

    private void noDataFoundVisibility(boolean b) {
        noDataFoundContainer.setVisibility(b?View.VISIBLE:View.GONE);
        recyclerView.setVisibility(b?View.GONE:View.VISIBLE);
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction(1);
        }
    }

    public void loadData() {
        if (bikeList.size() > 0)
            bikeList.clear();
        if (allBikes.size() > 0)
            allBikes.clear();
        swipeRefreshLayout.setRefreshing(true);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot post : dataSnapshot.getChildren()) {
                    Bike bike = post.getValue(Bike.class);
                    if (bike != null)
                        bike.setDistance(FindBikes.calculateDistance(lat, lng, bike.getLocation().get("latitude"), bike.getLocation().get("longitude")));
                    allBikes.add(bike);
                }
                bookingReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (final DataSnapshot postSnap : dataSnapshot.getChildren()) {
                            for (DataSnapshot bookingSnap : postSnap.getChildren()) {
                                final Booking booking = bookingSnap.getValue(Booking.class);
                                if (booking != null && booking.getBy().equals(UID)) {
                                    bikeList.add(new BookedBike(getBike(postSnap.getKey()), booking));
                                }
                            }
                        }
                        swipeRefreshLayout.setRefreshing(false);
                        adapter.notifyDataSetChanged();
                        noDataFoundVisibility(bikeList.size() <= 0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public Bike getBike(String BikeNo) {
        for (Bike bike : allBikes) {
            if (bike.getBikeNo().equals(BikeNo))
                return bike;
        }
        return new Bike();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnBookABike:
                if (mListener != null) {
                    mListener.onFragmentInteraction(2);
                }
                break;
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int code);
    }

    private void getLocation() {
        client = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (getActivity() != null)
            client.getLastLocation().addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    try {
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            loadData();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
    }
}
