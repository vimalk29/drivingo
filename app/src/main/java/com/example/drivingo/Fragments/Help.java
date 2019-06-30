package com.example.drivingo.Fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.drivingo.Adapters.FaqAdapter;
import com.example.drivingo.R;
import com.example.drivingo.model.FaqPojo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;

public class Help extends Fragment implements View.OnClickListener {
    private OnFragmentInteractionListener mListener;
    Button bikeRenting, bikeBooking, walletRefund, liveAssistance, dealsDiscount, paymentCharges;
    RelativeLayout contactMeLayout;
    ArrayList<FaqPojo> arrayList;
    TextView contactNo;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    private AVLoadingIndicatorView progressbar;
    ListView faqList;
    FaqAdapter faqAdapter;
    public Help() {
        // Required empty public constructor
    }

    public static Help newInstance(String param1, String param2) {
        Help fragment = new Help();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment =  inflater.inflate(R.layout.fragment_help, container, false);
        bikeBooking = myFragment.findViewById(R.id.bikeBookingButton);
        bikeRenting = myFragment.findViewById(R.id.bikeRentingButton);
        walletRefund = myFragment.findViewById(R.id.walletRefundButton);
        liveAssistance = myFragment.findViewById(R.id.liveAssistanceButton);
        dealsDiscount = myFragment.findViewById(R.id.dealsAndDiscountButton);
        paymentCharges = myFragment.findViewById(R.id.paymentAndChargesButton);
        contactMeLayout = myFragment.findViewById(R.id.contactMeLayout);
        contactNo = myFragment.findViewById(R.id.contactMeNo);
        faqList = myFragment.findViewById(R.id.faqList);
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("FAQs/Common");
        arrayList = new ArrayList<FaqPojo>();
        progressbar = myFragment.findViewById(R.id.progressBar);

        bikeBooking.setOnClickListener(this);
        bikeRenting.setOnClickListener(this);
        walletRefund.setOnClickListener(this);
        liveAssistance.setOnClickListener(this);
        dealsDiscount.setOnClickListener(this);
        paymentCharges.setOnClickListener(this);
        contactMeLayout.setOnClickListener(this);
        contactNo.setText("9694540876");
        progressbar.show();

        faqAdapter = new FaqAdapter(getContext(), R.layout.faq_list_item, arrayList);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                arrayList.clear();
                for (DataSnapshot dataSnapshot : dS.getChildren()){
                    arrayList.add(dataSnapshot.getValue(FaqPojo.class));
                }
                faqAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("12345", "onCancelled: "+ databaseError.getMessage()+ databaseError.getDetails());
            }
        });

        faqList.setAdapter(faqAdapter);
        progressbar.hide();
        return myFragment;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(4);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bikeRentingButton :
                loadAdapter("FAQs/BikeRenting");
                break;
            case R.id.bikeBookingButton :
                loadAdapter("FAQs/BikeBooking");
                break;
            case R.id.paymentAndChargesButton :
                loadAdapter("FAQs/PaymentCharges");
                break;
            case R.id.walletRefundButton :
                loadAdapter("FAQs/WalletRefund");
                break;
            case R.id.dealsAndDiscountButton :
                loadAdapter("FAQs/DealsDiscounts");
                break;
            case R.id.liveAssistanceButton :
                loadAdapter("FAQs/LiveAssistance");
                break;
            case R.id.contactMeLayout :
                    String number = contactNo.getText().toString();
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:"+number));
                    startActivity(callIntent);
                break;
        }
    }

    private void loadAdapter(String referencePath) {
        faqAdapter = new FaqAdapter(getContext(), R.layout.faq_list_item, arrayList);

        databaseReference = firebaseDatabase.getReference(referencePath);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dS) {
                arrayList.clear();
                for (DataSnapshot dataSnapshot : dS.getChildren()){
                    arrayList.add(dataSnapshot.getValue(FaqPojo.class));
                }
                faqAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("12345", "onCancelled: "+ databaseError.getMessage()+ databaseError.getDetails());
            }
        });
        faqList.setAdapter(faqAdapter);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int code);
    }
}