package com.example.drivingo.Fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TimePicker;

import com.example.drivingo.Activities.GetLocation;
import com.example.drivingo.Activities.FindBikes;
import com.example.drivingo.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class UserInputs extends Fragment implements View.OnClickListener{

    private FusedLocationProviderClient client;
    private double lat,lng;
    private String City;
    private Button btnSearch;
    private AVLoadingIndicatorView progressBar;
    private EditText etStartDate,etEndDate,etCity;
    private ImageButton startDateBtn,endDateBtn,cityBtn;
    private Calendar startCalendar,endCalendar;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault());
    private OnFragmentInteractionListener mListener;
    private View myFragment;
    private Context context;

    public UserInputs() {
        // Required empty public constructor
    }

    public static UserInputs newInstance() {
        return new UserInputs();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragment= inflater.inflate(R.layout.fragment_user_inputs,container,false);
        etCity = myFragment.findViewById(R.id.etCity);
        cityBtn = myFragment.findViewById(R.id.cityBtn);
        progressBar = myFragment.findViewById(R.id.progressBar);
        btnSearch = myFragment.findViewById(R.id.searchBtn);
        etStartDate = myFragment.findViewById(R.id.etStartDate);
        etEndDate = myFragment.findViewById(R.id.etEndDate);
        startDateBtn = myFragment.findViewById(R.id.startDateBtn);
        endDateBtn = myFragment.findViewById(R.id.endDateBtn);
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
        return myFragment;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.onFragmentInteraction(1);
        }
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
        public void onDateSet(DatePicker datePicker, final int year, final int month, final int day) {
            new TimePickerDialog(context
                    , new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                    startCalendar.set(Calendar.YEAR,year);
                    startCalendar.set(Calendar.MONTH,month);
                    startCalendar.set(Calendar.DAY_OF_MONTH,day);
                    startCalendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                    startCalendar.set(Calendar.MINUTE,minute);
                    updateLabel();
                }
            },startCalendar.get(Calendar.HOUR_OF_DAY),startCalendar.get(Calendar.MINUTE),false).show();
        }
    };
    DatePickerDialog.OnDateSetListener to_date = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker,final int year, final int month,final int day) {
            new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int hour, int minute) {
                    endCalendar.set(Calendar.YEAR,year);
                    endCalendar.set(Calendar.MONTH,month);
                    endCalendar.set(Calendar.DAY_OF_MONTH,day);
                    endCalendar.set(Calendar.HOUR_OF_DAY,hour);
                    endCalendar.set(Calendar.MINUTE,minute);
                    updateLabel();
                }
            },endCalendar.get(Calendar.HOUR_OF_DAY),endCalendar.get(Calendar.MINUTE),false).show();
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
                        new DatePickerDialog(context,from_date,
                                startCalendar.get(Calendar.YEAR),
                                startCalendar.get(Calendar.MONTH),
                                startCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());
                datePickerDialog.show();
                break;
            case  R.id.endDateBtn:
                DatePickerDialog datePickerDialog1 =
                        new DatePickerDialog(context,to_date,
                                endCalendar.get(Calendar.YEAR),
                                endCalendar.get(Calendar.MONTH),
                                endCalendar.get(Calendar.DAY_OF_MONTH));
                //Date date = dateFormat.parse(etStartDate.getText().toString());
                datePickerDialog1.getDatePicker().setMinDate(startCalendar.getTimeInMillis());
                datePickerDialog1.show();
                break;
            case R.id.cityBtn:
                Intent intent = new Intent(context, GetLocation.class);
                intent.putExtra("latitude",lat);
                intent.putExtra("longitude",lng);
                startActivityForResult(intent,3);
                break;
            case R.id.searchBtn:
                Intent intent1 = new Intent(context, FindBikes.class);
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
        client = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            updateVisibility(true);
            return;
        }
        if(getActivity()!=null)
        client.getLastLocation().addOnSuccessListener(getActivity(),new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                try{
                    if(location !=null){
                        lat = location.getLatitude();
                        lng = location.getLongitude();
                        Geocoder geocoder = new Geocoder(context,Locale.getDefault());
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
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==3){
            if(resultCode==RESULT_OK){
                if(data!=null){
                    try{
                        lat = data.getDoubleExtra("Latitude",0);
                        lng = data.getDoubleExtra("Longitude",0);
                        Geocoder geocoder = new Geocoder(context,Locale.getDefault());
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

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int code);
    }
}
