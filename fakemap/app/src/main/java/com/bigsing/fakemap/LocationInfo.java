package com.bigsing.fakemap;

/**
 * Created by sing on 2018/3/29.
 */

public class LocationInfo {
    private String address;
    private Double Latitude;
    private Double LonTitude;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLonTitude() {
        return LonTitude;
    }

    public void setLonTitude(Double lonTitude) {
        LonTitude = lonTitude;
    }
}