package com.example.testandroidapp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.ar.core.ArCoreApk;

public class AR_View extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar__view);

        enableAR();
    }


    //https://developers.google.com/ar/develop/java/enable-arcore
    protected void enableAR() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);

        if (availability.isTransient()) {

            //
            // Re-query at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enableAR();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            //cool that's excellent move along.
        } else {
            //AR core isn't enabled so handle that somehow.
        }

        if (!CameraPermissionsHelper.hasCameraPermission(this)) {
            //camera permissions are not there.
            CameraPermissionsHelper.requestCameraPermission(this);
            return;
        }
        return;
    }
}
