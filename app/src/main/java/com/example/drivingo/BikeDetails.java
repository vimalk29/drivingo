package com.example.drivingo;

import android.location.Address;
import android.location.Geocoder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.drivingo.model.Bike;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BikeDetails extends AppCompatActivity {
    TextView tvModel,tvBikeNo,tvDistance,tvLocation;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bike_details);
        tvModel = findViewById(R.id.tvModel);
        tvBikeNo = findViewById(R.id.tvBikeNo);
        tvLocation = findViewById(R.id.tvAddress);
        tvDistance = findViewById(R.id.tvDistance);
        imageView = findViewById(R.id.imageView);
        try{
        Bundle bundle = new Bundle();
        bundle = getIntent().getExtras();
        if(bundle!=null){
            String model =  "Model    : "+bundle.getString("model");
            String bikeNo = "Bike No. :"+bundle.getString("bikeNo");
            String image = bundle.getString("image");
            String distance = "Distance : "+String.format(Locale.getDefault(),"%.3f",bundle.getDouble("distance"))+" Km";
            HashMap<String,Double> Location= (HashMap<String, Double>) bundle.getSerializable("location");

            tvModel.setText(model);
            tvBikeNo.setText(bikeNo);
            Picasso.with(this).load(image)
                    .placeholder(R.drawable.icon_bike)
                    .fit()
                    .into(imageView);
            tvDistance.setText(distance);

            double lat = Location.get("latitude");
            double lng = Location.get("longitude");

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
            String Address = addresses.get(0).getAddressLine(0);
            String City = addresses.get(0).getLocality();

            tvLocation.setText("Address : "+Address);
            Log.d("debug...",distance);
            Log.d("debug...",Address);
        }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}
