package com.example.testandroidapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class WifiList extends Fragment {

    private ArrayList<String> signals = new ArrayList<>();
    private List<ScanResult> wifiList;

    public WifiList() {}

    @Override
    public View onCreateView(LayoutInflater inflator, ViewGroup container, Bundle savedInstanceState) {
        View view = inflator.inflate(R.layout.fragment_wifi_list, container, false);
        wifiList = ((MainActivity)getActivity()).getList();
        signals.clear();
        String dataString = "";
        for (ScanResult result : wifiList) {
            String signal = result.SSID + ", " + result.BSSID + ": " + result.level + "\n";
            signals.add(signal);
        }

        ListView viewList= (ListView) view.findViewById(R.id.list);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, signals);
        viewList.setAdapter(arrayAdapter);

        // register onClickListener to handle click events on each item
        viewList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            // argument position gives the index of item which is clicked
            public void onItemClick(AdapterView<?> arg0, View v,int position, long arg3)
            {
                String selectedwifi=signals.get(position);
                Intent newActivityIntent=new Intent(getActivity(), MainActivity.class);
                newActivityIntent.putExtra("TextView",selectedwifi);
                startActivity(newActivityIntent);
            }
        });
        return view;
    }
}
