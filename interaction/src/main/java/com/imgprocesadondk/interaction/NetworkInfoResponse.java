package com.imgprocesadondk.interaction;

public class NetworkInfoResponse extends Response {

    private String ssid;
    private String linkSpeed;
    private String rssi;

    public NetworkInfoResponse() {
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getLinkSpeed() {
        return linkSpeed;
    }

    public void setLinkSpeed(String linkSpeed) {
        this.linkSpeed = linkSpeed;
    }

    public String getRssi() {
        return rssi;
    }

    public void setRssi(String rssi) {
        this.rssi = rssi;
    }
}
