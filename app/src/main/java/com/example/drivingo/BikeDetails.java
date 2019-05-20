package com.example.drivingo;

import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drivingo.model.Bike;
import com.example.drivingo.model.Booking;
import com.example.drivingo.model.CommonValues;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BikeDetails extends AppCompatActivity {
    TextView tvModel,tvBikeNo,tvDistance,tvLocation,tvRent;
    ImageView imageView;
    Button btnPlay;
    Bike bike;
    FirebaseDatabase database;
    DatabaseReference databaseReference,bookingReference;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_details);
        tvModel = findViewById(R.id.tvModel);
        tvBikeNo = findViewById(R.id.tvBikeNo);
        tvLocation = findViewById(R.id.tvAddress);
        tvDistance = findViewById(R.id.tvDistance);
        imageView = findViewById(R.id.imageView);
        tvRent = findViewById(R.id.tvRent);
        btnPlay = findViewById(R.id.btnPay);
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("Bikes");
        bookingReference = database.getReference("Booking");

        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnPlay.setEnabled(false);
                checkAvailability();
            }
        });
        try{
        Bundle bundle = getIntent().getExtras();
        if(bundle!=null){
            int position = bundle.getInt("position");
            bike = CommonValues.CommonBikeList.get(position);

            double lat = bike.getLocation().get("latitude");
            double lng = bike.getLocation().get("longitude");
            long time = CommonValues.ToDate.getTime() - CommonValues.FromDate.getTime();
            int days = (int)(time/(24*60*60*1000))+1;
            CommonValues.rent = bike.getRent()*days;

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat,lng,1);

            String City = addresses.get(0).getLocality();
            String model =  String.format(Locale.getDefault(),"%-9s: %s","Model",bike.getModel());
            String bikeNo =  String.format(Locale.getDefault(),"%-9s: %s","Bike No.",bike.getBikeNo());
            String distance =  String.format(Locale.getDefault(),"%-9s: %.3f Km","Distance",bike.getDistance());
            String rent = String.format(Locale.getDefault(),"%-9s: %d(%d*%d)","Rent",CommonValues.rent,bike.getRent(),days);
            String Address = String.format(Locale.getDefault(),"%-9s: %s","Address",
                    addresses.get(0).getAddressLine(0));

            tvModel.setText(model);
            tvBikeNo.setText(bikeNo);
            Picasso.with(this).load(bike.getImage())
                    .placeholder(R.drawable.icon_bike)
                    .fit()
                    .into(imageView);
            tvDistance.setText(distance);
            tvRent.setText(rent);
            tvLocation.setText(Address);
        }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    private void bookBike() {
        String Today = dateFormat.format(Calendar.getInstance().getTime());
        String from  = dateFormat.format(CommonValues.FromDate.getTime());
        String to = dateFormat.format(CommonValues.ToDate.getTime());
        Booking booking = new Booking("User",Today,from,to);
        bookingReference = database.getReference("Booking/"+bike.getBikeNo());
        String id= bookingReference.push().getKey();
        bookingReference.child(id).setValue(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(BikeDetails.this, "bike booked", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAvailability() {
        bookingReference = database.getReference("Booking/"+bike.getBikeNo());
        bookingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean temp=true;
                if(dataSnapshot.exists()){
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        Booking booking =snapshot.getValue(Booking.class);
                        if(booking!=null)
                            try{
                                Date from = dateFormat.parse(booking.getFrom());
                                Date to = dateFormat.parse(booking.getTo());
                                if(!(CommonValues.ToDate.before(from)||CommonValues.FromDate.after(to))){
                                    temp=false;
                                    break;
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                    }
                }
                if(temp)
                    bookBike();
                else
                    Toast.makeText(BikeDetails.this, "Please refresh", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
