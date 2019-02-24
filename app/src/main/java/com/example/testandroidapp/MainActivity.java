package com.example.testandroidapp;

import android.os.Bundle;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback {

    private SupportMapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!CameraPermissionsHelper.hasCameraPermission(this)) {
            //camera permissions are not there.
            CameraPermissionsHelper.requestCameraPermission(this);
            return;
        }

        // Init firebase app
        FirebaseApp.initializeApp(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // TODO: Initialize firebase app

        // Add map fragment
        this.mapFragment = new SupportMapFragment();
        this.mapFragment.getMapAsync(this);

        // Render map fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, this.mapFragment);
        fragmentTransaction.commit();

        final BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    List<ScanResult> scanResults = wifiManager.getScanResults();
                    // add your logic here
                    signals.clear();
                    String dataString = "";
                    for (ScanResult result : scanResults) {
                        // SSID = AP SSID, BSSID = MAC Address, level = signal strength
                        String signal = result.SSID + ", " + result.BSSID + ": " + result.level + result.capabilities + wifiManager.calculateSignalLevel(result.level, 5)  + "\n";
                        RouterInfo routerInfo = new RouterInfo(result.SSID, result.BSSID, result.capabilities, result.level, WifiManager.calculateSignalLevel(result.level, 5));
                        signals.add(signal);
                        if (signal.toLowerCase().contains(filterString.toLowerCase())) {
                            dataString += signal;
                        }
                    }
                    dataField.setText(dataString);
                }
            }
        };

        // Navigation button listeners
        BottomNavigationItemView mapButton = findViewById(R.id.navigation_map);
        BottomNavigationItemView arButton = findViewById(R.id.navigation_ar);

        mapButton.setOnClickListener(this);
        arButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.navigation_map:
                fragment =this.mapFragment;
                break;
            // TODO: AR fragment case
        }

        replaceFragment(fragment);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    public void onMapReady(GoogleMap googleMap) {

    }

}
