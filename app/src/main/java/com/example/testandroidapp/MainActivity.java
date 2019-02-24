package com.example.testandroidapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.firestore.model.ResourcePath;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, ARViewFrag.OnFragmentInteractionListener {

    private SupportMapFragment mapFragment;
    private ARViewFrag arFragment;

    private FirebaseFirestore db;
    private GoogleMap map;

    private FusedLocationProviderClient fusedLocationClient;

    private WifiManager wifiManager;
    private List<ScanResult> scanResults;
    private BroadcastReceiver wifiReceiver;

    private LocationListener locationListener;
    private LocationManager locationManager;
    private Location location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Request Camera and Location permissions from user
        if (!PermissionsHelper.hasWhichPermission(this)) {
            //location permissions are not there.
            PermissionsHelper.requestAllPermissions(this);
        }

        // Init firebase app
        FirebaseApp.initializeApp(this);
        this.db = FirebaseFirestore.getInstance();

        // Add map fragment
        this.mapFragment = new SupportMapFragment();
        this.mapFragment.getMapAsync(this);

        // Add AR fragment
        this.arFragment = new ARViewFrag();

        // Set location client
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Render map fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, this.mapFragment);
        fragmentTransaction.commit();

        // Navigation button listeners
        BottomNavigationItemView mapButton = findViewById(R.id.navigation_map);
        BottomNavigationItemView arButton = findViewById(R.id.navigation_ar);

        mapButton.setOnClickListener(this);
        arButton.setOnClickListener(this);

        // Set polling timer
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        pollWiFi();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location loc) {
                location = loc;
                System.out.println("curr loc: " + location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        // Set callback for received wifi signals
        wifiReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    scanResults = wifiManager.getScanResults();
                    System.out.println("wifi: " + scanResults);
                    for (ScanResult router : scanResults) {
                        if (router.SSID.equals("CometNet")) {
                            Map<String, String> map = new HashMap<>();
                            map.put("ssid", router.SSID);
                            map.put("mac", router.BSSID);
                            db.collection("routers").document(router.BSSID).set(map);

                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                            // Push heat to firebase
                            addHeatAtCurrentLocation(router);
                        }
                    }
                }
            }
        };
    }

    private void addHeatAtCurrentLocation(ScanResult router) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (location != null) {
            String key = router.BSSID + ":" + location.getLatitude() + ":" + location.getLongitude();
            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
            Map<String, Object> map = new HashMap<>();
            map.put("location", geoPoint);
            map.put("weight", WifiManager.calculateSignalLevel(router.level, 5));
            map.put("mac", router.BSSID);
            map.put("ssid", router.SSID);
            db.collection("locations").document(key).set(map);

            // Set heatmap with location refs
            Map<String, Object> heatmap = new HashMap<>();
            DocumentReference ref = DocumentReference.forPath(ResourcePath.fromString("/locations/" + key), db);
            heatmap.put("locations", Arrays.asList(ref));
            db.collection("heatmaps").document(router.BSSID).set(heatmap);
        }
    }

    @Override
    public void onClick(View view) {
        Fragment fragment = null;
        switch (view.getId()) {
            case R.id.navigation_map:
                fragment = this.mapFragment;
                break;
            case R.id.navigation_ar:
                fragment = this.arFragment;
                break;
        }

        replaceFragment(fragment);
    }

    public void pollWiFi() {
        Timer timer = new java.util.Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Poll wifi");
                registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            }
        }, 5000, 5000);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        this.fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));
                    }
                });

        // Get router locations from firebase and add pins to map
        CollectionReference routers = db.collection("routers");
        routers.get().addOnSuccessListener(snap -> {
            for (QueryDocumentSnapshot router : snap) {

                // Mark location of router
                GeoPoint location = (GeoPoint) router.get("location");

                if (location == null) {
                    return;
                }

                LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
                map.addMarker(new MarkerOptions().position(marker).title((String) router.get("ssid")));
            }
        });

        // Get reference to heapmap of router
        CollectionReference locations = db.collection("locations");
        locations.get().addOnSuccessListener(snap -> {
            // ArrayList to store heat map points
            ArrayList<WeightedLatLng> weightedHeatMap = new ArrayList<>();

            System.out.println("Location: " + snap.getDocuments());
            for (DocumentSnapshot location : snap.getDocuments()) {

                // Get geopoint from location
                GeoPoint geoPoint = (GeoPoint) location.get("location");
                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                // Add to arraylist
                WeightedLatLng weightedLatLng = new WeightedLatLng(latLng, ((Long) location.get("weight")).doubleValue());
                weightedHeatMap.add(weightedLatLng);
            }
        });
        locations.addSnapshotListener((snap, e) -> {

            // ArrayList to store heat map points
            ArrayList<WeightedLatLng> weightedHeatMap = new ArrayList<>();

            System.out.println("Location: " + snap.getDocuments());
            for (DocumentSnapshot location : snap.getDocuments()) {

                // Get geopoint from location
                GeoPoint geoPoint = (GeoPoint) location.get("location");
                LatLng latLng = new LatLng(geoPoint.getLatitude(), geoPoint.getLongitude());

                // Add to arraylist
                WeightedLatLng weightedLatLng = new WeightedLatLng(latLng, ((Long) location.get("weight")).doubleValue());
                weightedHeatMap.add(weightedLatLng);
            }
            overlayHeatmap(weightedHeatMap);
        });
    }

    public void overlayHeatmap(ArrayList<WeightedLatLng> weightedHeatMap) {

        System.out.println("list: " + weightedHeatMap);

        // Create the gradient
        int[] colors = {
                Color.rgb(255, 0, 0),
                Color.rgb(102, 225, 0)
        };

        float[] startPoints = {
                0.2f, 1.0f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        // Create the tile provider
        HeatmapTileProvider provider = new HeatmapTileProvider.Builder()
                .weightedData(weightedHeatMap)
                .gradient(gradient)
                .radius(50)
                .opacity(0.5)
                .build();

        // Add the tile overlay to the map
        TileOverlay overlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(provider));
    }

    public List<ScanResult> getList() {
        return this.scanResults;
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
