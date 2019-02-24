package com.example.testandroidapp;

public class RouterInfo {
    private String SSID;
    private String BSSID;
    private String capabilities;
    private int level;
    private int signalLevel;

    public RouterInfo() {

    }
    public RouterInfo(String SSID, String BSSID, String capabilities, int level, int signalLevel) {
        this.SSID = SSID;
        this.BSSID = BSSID;
        this.capabilities = capabilities;
        this.level = level;
        this.signalLevel = signalLevel;
    }
    public void setSSID(String SSID) {
        this.SSID = SSID;
    }
    public String getSSID() {
        return SSID;
    }
    public void setBSSID(String BSSID) {
        this.BSSID = BSSID;
    }
    public String getBSSIED() {
        return BSSID;
    }
    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }
    public String getCapabilities() {
        return capabilities;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public int getLevel() {
        return level;
    }
    public void setSignalLevel(int signalLevel) {
        this.signalLevel = signalLevel;
    }
    public int getSignalLevel() {
        return signalLevel;
    }
}
