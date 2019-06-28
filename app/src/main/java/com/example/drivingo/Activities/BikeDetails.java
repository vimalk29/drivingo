package com.example.drivingo.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drivingo.Common.AlertMessage;
import com.example.drivingo.Common.SessionManagement;
import com.example.drivingo.Paytm.Transaction;
import com.example.drivingo.R;
import com.example.drivingo.model.Bike;
import com.example.drivingo.model.Booking;
import com.example.drivingo.Common.CommonValues;
import com.example.drivingo.model.Offer;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class BikeDetails extends AppCompatActivity implements View.OnClickListener {
    String amount, key, orderID,error,PROMO;
    TextView tvModel, tvBikeNo, tvDistance, tvLocation, tvRent, txtPromo;
    ImageView imageView;
    Button btnPay;
    Bike bike;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    DatabaseReference databaseReference, bookingReference, transactionReference, offersRef;
    public SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy hh:mm a", Locale.getDefault());
    public static SimpleDateFormat preBookTimeFormat = new SimpleDateFormat("dd/MM/yyyy-HH:mm:ss", Locale.getDefault());
    long TIME_OUT = 10 * 60000;
    SessionManagement sessionManagement;
    int REQUEST_CODE = 10,hrs;

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
        btnPay = findViewById(R.id.btnPay);
        txtPromo = findViewById(R.id.txtHavePromo);

        database = FirebaseDatabase.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = database.getReference("Bikes");
        bookingReference = database.getReference("Booking");
        transactionReference = database.getReference("Orders");
        offersRef = database.getReference("Offers");
        sessionManagement = new SessionManagement(this);

        txtPromo.setOnClickListener(this);
        btnPay.setOnClickListener(this);
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                int position = bundle.getInt("position");
                bike = CommonValues.CommonBikeList.get(position);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(bike.getModel());
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }

                double lat = bike.getLocation().get("latitude");
                double lng = bike.getLocation().get("longitude");
                long time = CommonValues.ToDate.getTime() - CommonValues.FromDate.getTime();
                //int days = (int)(time/(24*60*60*1000))+1;
                hrs = (int) (time / (60 * 60 * 1000)) + 1;
                CommonValues.rent = bike.getRent() * hrs;

                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);

                String City = addresses.get(0).getLocality();
                String model = String.format(Locale.getDefault(), "%-9s: %s", "Model", bike.getModel());
                String bikeNo = String.format(Locale.getDefault(), "%-9s: %s", "Bike No.", bike.getBikeNo());
                String distance = String.format(Locale.getDefault(), "%-9s: %.3f Km", "Distance", bike.getDistance());
                String rent = String.format(Locale.getDefault(), "%-9s: %d(%d*%d)", "Rent", CommonValues.rent, bike.getRent(), hrs);
                String Address = String.format(Locale.getDefault(), "%-9s: %s", "Address",
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void makeTransaction() {
        orderID = transactionReference.push().getKey();
        HashMap<String, String> transaction = new HashMap<>();
        if (orderID != null && firebaseAuth.getCurrentUser() != null) {
            transaction.put("userID", firebaseAuth.getCurrentUser().getUid());
            transaction.put("amount", String.valueOf(CommonValues.rent));
            transaction.put("bikeID", bike.getBikeNo());
            transaction.put("status", "initial");
            transactionReference.child(orderID).setValue(transaction).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Intent intent = new Intent(getApplicationContext(), Transaction.class);
                    intent.putExtra("orderId", orderID);
                    intent.putExtra("amount", CommonValues.rent + "");
                    startActivityForResult(intent, REQUEST_CODE);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    markFlagged(key, bike.getPreBookTime(), false);
                }
            });

        } else
            markFlagged(key, bike.getPreBookTime(), false);
    }

    private void bookBike(final String bikeID) {
        String Today = dateFormat.format(Calendar.getInstance().getTime());
        String from = dateFormat.format(CommonValues.FromDate.getTime());
        String to = dateFormat.format(CommonValues.ToDate.getTime());
        Booking booking = new Booking(firebaseAuth.getCurrentUser().getUid(), Today, from, to);
        bookingReference = database.getReference("Booking/" + bike.getBikeNo());
        String id = bookingReference.push().getKey();
        bookingReference.child(id).setValue(booking).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                databaseReference.child(bikeID).child("isAvailable").setValue(false).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(BikeDetails.this, "bike booked", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(BikeDetails.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        intent.putExtra("open_tab", 0);
                        startActivity(intent);
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(BikeDetails.this, "bike couldn't booked", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void markFlagged(final String key, String timeNow, final boolean makeTransaction) {
        this.key = key;
        databaseReference.child(key).child("preBookTime").setValue(timeNow).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                if (makeTransaction)
                    makeTransaction();
                else
                    btnPay.setEnabled(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
            }
        });
    }

    private void checkAvailability() {
        bookingReference = database.getReference("Booking/" + bike.getBikeNo());
        bookingReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean temp = true;
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Booking booking = snapshot.getValue(Booking.class);
                        if (booking != null)
                            try {
                                Date from = dateFormat.parse(booking.getFrom());
                                Date to = dateFormat.parse(booking.getTo());
                                if (!(CommonValues.ToDate.before(from) || CommonValues.FromDate.after(to))) {
                                    temp = false;
                                    break;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                    }
                }
                if (temp)
                    bookBike("");
                else
                    Toast.makeText(BikeDetails.this, "Please refresh", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void isAvailable() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    boolean temp = false;
                    for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                        Bike bike = postSnapshot.getValue(Bike.class);
                        if (bike != null && BikeDetails.this.bike.getBikeNo().equals(bike.getBikeNo()) && bike.isIsAvailable()) {
                            Date preBookTime = preBookTimeFormat.parse(bike.getPreBookTime());
                            Date timeNow = Calendar.getInstance().getTime();
                            long diff = timeNow.getTime() - preBookTime.getTime();
                            if (diff > TIME_OUT) {
                                temp = true;
                                markFlagged(postSnapshot.getKey(), preBookTimeFormat.format(timeNow), true);//before starting transaction marked it flag so other user not start transaction for the same
                                break;
                            }
                        }
                    }
                    if (!temp)
                        btnPay.setEnabled(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                btnPay.setEnabled(true);
            }
        });
    }


    private boolean isPermissionGranted() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
            return false;
        else
            return true;
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_SMS}, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_SMS);
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // makeTransaction();
                } else if (!showRationale)
                    notifyUser(true);
                else
                    notifyUser(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        }
        if (REQUEST_CODE == requestCode && resultCode == RESULT_OK && data != null) {
            int status = data.getIntExtra("status", 0);
            if (status == 1) {
                //transaction successful
                transactionReference.child(orderID).child("status").setValue("1");
                bookBike(key);
            } else if (status == -1) {
                //pending
                transactionReference.child(orderID).child("status").setValue("-1");
                Toast.makeText(this, "transaction pending", Toast.LENGTH_SHORT).show();
                databaseReference.child(key).child("isAvailable").setValue(false);
            } else {
                //failed
                transactionReference.child(orderID).child("status").setValue("0");
                Toast.makeText(this, "transaction failed", Toast.LENGTH_SHORT).show();
                markFlagged(key, bike.getPreBookTime(), false);
            }
        }
    }

    private void notifyUser(final boolean openSetting) {
        AlertMessage.showMessageDialog(this, "Please provide permission to continue", "OK", "Cancel", new AlertMessage.YesNoListener() {
            @Override
            public void onDecision(boolean btnClicked) {
                if (btnClicked) {//ok clicked
                    if (openSetting) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 2);
                    } else
                        requestPermission();
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnPay:
                btnPay.setEnabled(false);
                if (isPermissionGranted())
                    isAvailable();
                else
                    requestPermission();
                break;
            case R.id.txtHavePromo:
                getPromoCodeDialog();
                break;
        }
    }

    private void getPromoCodeDialog() {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(R.layout.promo_dialog, null, false);
        final EditText etPromo = view.findViewById(R.id.etPromo);
        final AVLoadingIndicatorView progressbar = view.findViewById(R.id.progressBar);
        progressbar.hide();

        final AlertDialog builder = new AlertDialog.Builder(this)
                .setView(view)
                .setCancelable(false)
                .setPositiveButton("Apply", null)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create();
        builder.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnOk = builder.getButton(AlertDialog.BUTTON_POSITIVE);
                btnOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final String promo = etPromo.getText().toString();
                        if (promo.length() < 6)
                            etPromo.setError("Enter a valid promo code");
                        else {
                            etPromo.setEnabled(false);
                            progressbar.show();
                            offersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.child(promo).exists()) {
                                        Offer offer = dataSnapshot.child(promo).getValue(Offer.class);
                                        if (offer != null) {
                                            try {
                                                Date currentDate = Calendar.getInstance().getTime();
                                                Date dateFrom = dateFormat1.parse(offer.getFrom());
                                                Date dateExpireOn = dateFormat1.parse(offer.getTo());
                                                 error = "";
                                                if(currentDate.before(dateFrom))
                                                    error="Offer didn't start yet";
                                                else if(currentDate.after(dateExpireOn))
                                                    error="Offer expired";
                                                else if(offer.isApplied())
                                                    error="Code used before";
                                                if(error.isEmpty()){
                                                    offer.setApplied(true);
                                                    final int discount = offer.getDiscount();
                                                    offersRef.child(promo).setValue(offer).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            CommonValues.rent -=discount;
                                                            String rent = String.format(Locale.getDefault(), "%-9s: %d(%d*%d-%d)", "Rent", CommonValues.rent, bike.getRent(), hrs,discount);

                                                            tvRent.setText(rent);
                                                            builder.dismiss();
                                                            Toast.makeText(BikeDetails.this, "Code applied", Toast.LENGTH_SHORT).show();
                                                            PROMO = promo;
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(BikeDetails.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                                                            builder.dismiss();
                                                        }
                                                    });
                                                }else{
                                                    Toast.makeText(BikeDetails.this, error, Toast.LENGTH_SHORT).show();
                                                    progressbar.hide();
                                                    etPromo.setEnabled(true);
                                                }

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                    } else{
                                        Toast.makeText(BikeDetails.this, "invalid", Toast.LENGTH_SHORT).show();
                                        progressbar.hide();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
            }
        });
        if (!this.isFinishing())
            builder.show();
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
