package com.example.drivingo.Activities;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drivingo.Common.CommonValues;
import com.example.drivingo.R;
import com.example.drivingo.model.Bike;
import com.example.drivingo.model.BookedBike;
import com.example.drivingo.model.Booking;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

public class BookedBikeDetails extends AppCompatActivity implements View.OnClickListener {
    TextView tvModel, tvBikeNo, tvTotalRent, tvRent, tvAddress, tvDistance, tvDateOfBooking, tvDateFrom, tvDateTo;
    ImageView imageView;
    Button btnDirection;
    double lat, lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booked_bike_detils);
        tvModel = findViewById(R.id.tvModel);
        tvBikeNo = findViewById(R.id.tvBikeNo);
        tvTotalRent = findViewById(R.id.tvTotalRent);
        tvDistance = findViewById(R.id.tvDistance);
        tvRent = findViewById(R.id.tvRent);
        tvAddress = findViewById(R.id.tvAddress);
        imageView = findViewById(R.id.imageView);
        tvDateOfBooking = findViewById(R.id.tvDateOfBooking);
        tvDateFrom = findViewById(R.id.tvBookedFrom);
        tvDateTo = findViewById(R.id.tvBookedTo);
        btnDirection = findViewById(R.id.btnDirection);
        btnDirection.setOnClickListener(this);
        btnDirection.setEnabled(false);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            try {
                int position = bundle.getInt("position");
                BookedBike bookedBike = CommonValues.commonBookedList.get(position);
                Bike bike = bookedBike.getBike();
                Booking booking = bookedBike.getBooking();
                lat = bike.getLocation().get("latitude");
                lng = bike.getLocation().get("longitude");
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(bike.getModel().toUpperCase());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                String City = addresses.get(0).getLocality();
                String model = String.format(Locale.getDefault(), "%-9s: %s", "Model", bike.getModel());
                String bikeNo = String.format(Locale.getDefault(), "%-9s: %s", "Bike No.", bike.getBikeNo());
                String distance = String.format(Locale.getDefault(), "%-9s: %.3f Km", "Distance", bike.getDistance());
                String totalRent = String.format(Locale.getDefault(), "%-9s: %s", "Total Rent", bookedBike.getTotalRent());
                String rent = String.format(Locale.getDefault(), "%-9s: %d", "Rent/Hrs", bike.getRent());
                String Address = String.format(Locale.getDefault(), "%-9s: %s", "Address",
                        addresses.get(0).getAddressLine(0));
                String dateOfBooking = String.format(Locale.getDefault(), "%-9s: %s", "Booked On", booking.getDate());
                String From = String.format(Locale.getDefault(), "%-9s: %s", "From", booking.getFrom());
                String To = String.format(Locale.getDefault(), "%-9s: %s", "To", booking.getTo());

                tvModel.setText(model);
                tvBikeNo.setText(bikeNo);
                Picasso.with(this).load(bike.getImage())
                        .placeholder(R.drawable.icon_bike)
                        .fit()
                        .into(imageView);
                tvTotalRent.setText(totalRent);
                tvRent.setText(rent);
                tvDistance.setText(distance);
                tvAddress.setText(Address);
                tvDateOfBooking.setText(dateOfBooking);
                tvDateFrom.setText(From);
                tvDateTo.setText(To);
                btnDirection.setEnabled(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnDirection:
                getDirectionInMaps();
                break;
        }
    }

    public void getDirectionInMaps() {
        String uri = String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=%f,%f (%s)", lat, lng, "Where the bike is");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//        intent.setPackage("com.google.android.apps.maps");
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            this.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
