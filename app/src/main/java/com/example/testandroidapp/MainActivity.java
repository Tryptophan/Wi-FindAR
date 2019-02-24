package com.example.testandroidapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import java.sql.SQLSyntaxErrorException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> signals = new ArrayList<>();
    private String filterString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        final TextView dataField = findViewById(R.id.datafield);
        final EditText filter = findViewById(R.id.filter);
        setSupportActionBar(toolbar);

        if (!CameraPermissionsHelper.hasCameraPermission(this)) {
            //camera permissions are not there.
            CameraPermissionsHelper.requestCameraPermission(this);
            return;
        }

        Context context = getApplicationContext();
        final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

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

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerReceiver(wifiReceiver,
                        new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                wifiManager.startScan();
            }
        });

        filter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                System.out.println("String filter: " + s.toString());
                filterString = s.toString();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
    }

}
