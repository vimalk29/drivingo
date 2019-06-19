package com.example.drivingo.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.drivingo.Common.AlertMessage;
import com.example.drivingo.Common.SessionManagement;
import com.example.drivingo.R;

import java.util.Timer;
import java.util.TimerTask;

public class Splash extends AppCompatActivity {

    final int PERMISSION_ALL = 1;
    final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Timer timer  = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(hasPermissions(Splash.this,PERMISSIONS)){
                    openNextActivity();
                }else
                    requestPermission();
            }
        },2000);

    }

    public void openNextActivity(){
        SessionManagement sessionManagement = new SessionManagement(Splash.this);
        if(sessionManagement.checkLogin())
            startActivity(new Intent(Splash.this, MainActivity.class));
        else
            startActivity(new Intent(Splash.this, Login.class));
        finish();
    }

    public static boolean hasPermissions(Context context,String... permissions){
        if(context!=null && permissions!=null){
            for (String permission:permissions) {
                if(ActivityCompat.checkSelfPermission(context,permission)!=PackageManager.PERMISSION_GRANTED)
                    return false;
            }
        }
        return true;
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,PERMISSIONS, PERMISSION_ALL);
    }
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_ALL:{
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                );
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   openNextActivity();
                }
                else if(!showRationale){
                    notifyUser(true);
                }else{
                    // permission denied
                    notifyUser(false);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (!hasPermissions(this,PERMISSIONS)) {
            requestPermission();
        }else{
           openNextActivity();
        }
    }

    private void notifyUser(final boolean openSetting){
        AlertMessage.showMessageDialog(this, "Please provide permission to continue", "Ok", "Exit", new AlertMessage.YesNoListener() {
            @Override
            public void onDecision(boolean btnClicked) {
                if(btnClicked){
                    if(openSetting) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, 2);
                    }
                    else
                        requestPermission();
                }else
                    finish();
            }
        });
    }
}
