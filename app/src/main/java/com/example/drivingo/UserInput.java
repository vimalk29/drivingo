package com.example.drivingo;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class UserInput extends AppCompatActivity implements View.OnClickListener {
    FusedLocationProviderClient client;
    double lat,lng;
    String City;
    Button btnSearch;
    ProgressBar progressBar;
    EditText etStartDate,etEndDate,etCity;
    ImageButton startDateBtn,endDateBtn,cityBtn;
    Calendar startCalendar,endCalendar;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_input);
        etCity = findViewById(R.id.etCity);
        cityBtn = findViewById(R.id.cityBtn);
        progressBar = findViewById(R.id.progressBar);
        btnSearch = findViewById(R.id.searchBtn);
        etStartDate = findViewById(R.id.etStartDate);
        etEndDate = findViewById(R.id.etEndDate);
        startDateBtn = findViewById(R.id.startDateBtn);
        endDateBtn = findViewById(R.id.endDateBtn);
        startCalendar= Calendar.getInstance();
        endCalendar = Calendar.getInstance();
        String date = dateFormat.format(Calendar.getInstance().getTime());
        etStartDate.setText(date);
        etEndDate.setText(date);

        btnSearch.setOnClickListener(this);
        cityBtn.setOnClickListener(this);
        startDateBtn.setOnClickListener(this);
        endDateBtn.setOnClickListener(this);
        getLocation();
    }

    private void updateVisibility(boolean b) {
        cityBtn.setEnabled(b);
        startDateBtn.setEnabled(b);
        endDateBtn.setEnabled(b);
        if(b)
            progressBar.setVisibility(View.GONE);
        else
            progressBar.setVisibility(View.VISIBLE);
    }

    DatePickerDialog.OnDateSetListener from_date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            startCalendar.set(Calendar.YEAR,year);
            startCalendar.set(Calendar.MONTH,month);
            startCalendar.set(Calendar.DAY_OF_MONTH,day);
            updateLabel();
        }
    };
    DatePickerDialog.OnDateSetListener to_date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            endCalendar.set(Calendar.YEAR,year);
            endCalendar.set(Calendar.MONTH,month);
            endCalendar.set(Calendar.DAY_OF_MONTH,day);
            updateLabel();
        }
    };
    private void updateLabel(){
        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
        if(startCalendar.getTime().after(endCalendar.getTime()))
            endCalendar = (Calendar)startCalendar.clone();
        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.startDateBtn:
                DatePickerDialog datePickerDialog =
                        new DatePickerDialog(UserInput.this,from_date,
                                startCalendar.get(Calendar.YEAR),
                                startCalendar.get(Calendar.MONTH),
                                startCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                datePickerDialog.show();
                break;
            case  R.id.endDateBtn:
                DatePickerDialog datePickerDialog1 =
                        new DatePickerDialog(UserInput.this,to_date,
                                endCalendar.get(Calendar.YEAR),
                                endCalendar.get(Calendar.MONTH),
                                endCalendar.get(Calendar.DAY_OF_MONTH));
                //Date date = dateFormat.parse(etStartDate.getText().toString());
                datePickerDialog1.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
                datePickerDialog1.show();
                break;
            case R.id.cityBtn:
                Intent intent = new Intent(UserInput.this,GetLocation.class);
                intent.putExtra("latitude",lat);
                intent.putExtra("longitude",lng);
                startActivityForResult(intent,3);
                break;
            case R.id.searchBtn:
                Intent intent1 = new Intent(UserInput.this,MainActivity.class);
                intent1.putExtra("latitude",lat);
                intent1.putExtra("longitude",lng);
                intent1.putExtra("from",etStartDate.getText().toString());
                intent1.putExtra("to",etEndDate.getText().toString());
                intent1.putExtra("city",City);
                startActivity(intent1);
                break;
        }
    }
    private void getLocation(){
        updateVisibility(false);
        client = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            updateVisibility(true);
            return;
        }
        client.getLastLocation().addOnSuccessListener(UserInput.this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try{
                    if(location !=null){
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Geocoder geocoder = new Geocoder(UserInput.this,Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
                        String Address = addresses.get(0).getAddressLine(0);
                        City = addresses.get(0).getLocality();
                        etCity.setText(City);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                updateVisibility(true);
            }
        });
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
                        Geocoder geocoder = new Geocoder(UserInput.this,Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(lat,lng,1);
                        String Address = addresses.get(0).getAddressLine(0);
                        City = addresses.get(0).getLocality();
                        etCity.setText(City);
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
