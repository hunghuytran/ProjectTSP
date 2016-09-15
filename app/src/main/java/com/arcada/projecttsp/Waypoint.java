package com.arcada.projecttsp;


public class Waypoint
{
    private String location;
    private double latitude;
    private double longitude;

    public Waypoint(String location, double latitude, double longitude)
    {
        this.location = location;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getLocation() {
        return location;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}