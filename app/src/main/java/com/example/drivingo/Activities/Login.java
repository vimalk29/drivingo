package com.example.drivingo.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.drivingo.Common.AlertMessage;
import com.example.drivingo.Common.SessionManagement;
import com.example.drivingo.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity implements View.OnClickListener {
    Button btnAction;
    EditText etMobile, etOtp;
    String mobile, otp, otpVerificationID;
    AVLoadingIndicatorView progressbar;
    SessionManagement sessionManagement;
    boolean btnAsSentOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btnAsSentOTP = true;
        progressbar = findViewById(R.id.progressBar);
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);
        btnAction = findViewById(R.id.btnAction);
        btnAction.setOnClickListener(this);
        progressbar.hide();
        sessionManagement = new SessionManagement(this);
        enterMobile();
    }

    @Override
    public void onClick(View view) {
        if (btnAsSentOTP) {
            mobile = etMobile.getText().toString();
            if (validMobileNo(mobile)) {
                phoneAuthentication();
            } else
                Toast.makeText(this, "Enter valid mobile number", Toast.LENGTH_SHORT).show();
        }else{//button as login
            if(otpVerificationID==null||otpVerificationID.isEmpty()){
                enterMobile();
                return;
            }
            otp = etOtp.getText().toString();
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(otpVerificationID,otp);
            signInWithPhoneAuthCredential(credential);
        }
    }

    private void phoneAuthentication() {
        progressbar.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber("+91" + mobile, 10L, TimeUnit.SECONDS, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                progressbar.hide();
                enterOTP();
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                progressbar.hide();
                AlertMessage.showMessageDialog(Login.this, "Otp could sent to this mobile number", new AlertMessage.OkListener() {
                    @Override
                    public void onOkClicked() {
                        enterMobile();
                    }
                });
            }

            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                otpVerificationID = s;
                etOtp.setVisibility(View.VISIBLE);
                etOtp.setEnabled(false);
                Toast.makeText(Login.this, "OTP sent", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        btnAction.setEnabled(false);
        etOtp.setEnabled(false);
        etMobile.setEnabled(false);
        progressbar.show();
        final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        progressbar.hide();
                        if(firebaseAuth.getCurrentUser()!=null){
                            sessionManagement.createLoginSession(firebaseAuth.getCurrentUser().getUid(), mobile);
                            startActivity(new Intent(Login.this, MainActivity.class));
                            finish();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressbar.hide();
                        AlertMessage.showMessageDialog(Login.this, "Invalid OTP", "Retype OTP", "change Mobile no", new AlertMessage.YesNoListener() {
                            @Override
                            public void onDecision(boolean btnClicked) {
                                if(btnClicked){//retype otp
                                    enterOTP();
                                }else{//type another mobile no
                                    enterMobile();
                                }
                            }
                        });
                    }
                });
    }

    private boolean validMobileNo(String mobile) {
        return android.util.Patterns.PHONE.matcher(mobile).matches();
    }

    public void enterOTP(){
        etMobile.setEnabled(false);
        etOtp.setVisibility(View.VISIBLE);
        etOtp.setEnabled(true);
        btnAsSentOTP = false;
        btnAction.setText("Login");
        btnAction.setEnabled(true);
    }
    public void enterMobile(){
        etOtp.setText("");
        etOtp.setVisibility(View.GONE);
        etMobile.setEnabled(true);
        btnAction.setText("Send OTP");
        btnAction.setEnabled(true);
        btnAsSentOTP = true;
    }

}
