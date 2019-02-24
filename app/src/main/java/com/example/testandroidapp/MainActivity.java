package com.example.testandroidapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.design.internal.BottomNavigationItemView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, ARViewFrag.OnFragmentInteractionListener {

    private SupportMapFragment mapFragment;
    private ARViewFrag arFragment;

    private FirebaseFirestore db;
    private GoogleMap map;

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

        // Render map fragment
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.container, this.mapFragment);
        fragmentTransaction.commit();

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
                fragment = this.mapFragment;
                break;
            case R.id.navigation_ar:
                fragment = this.arFragment;
                break;
        }

        replaceFragment(fragment);
    }

    public void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;

        // Get router locations from firebase and add pins to map
        CollectionReference routers = db.collection("routers");
        routers.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot snap) {
                for (QueryDocumentSnapshot router : snap) {
                    GeoPoint location = (GeoPoint) router.get("location");
                    LatLng marker = new LatLng(location.getLatitude(), location.getLongitude());
                    map.addMarker(new MarkerOptions().position(marker).title((String) router.get("ssid")));
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));
                }
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
