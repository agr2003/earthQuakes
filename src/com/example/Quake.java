package com.example;

import android.location.Location;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Admin
 * Date: 26.05.11
 * Time: 23:29
 * To change this template use File | Settings | File Templates.
 */
public class Quake {
    private Date date;
    private String details;
    private Location location;
    private double magnitude;
    private String link;

    public Quake( Date date, String details, Location location, double magnitude, String link ) {
        this.date = date;
        this.details = details;
        this.location = location;
        this.magnitude = magnitude;
        this.link = link;
    }

    public Date getDate() {
        return date;
    }

    public String getDetails() {
        return details;
    }

    public Location getLocation() {
        return location;
    }

    public double getMagnitude() {
        return magnitude;
    }

    public String getLink() {
        return link;
    }

    @Override
    public String toString(){
        SimpleDateFormat sdf = new SimpleDateFormat( "HH.mm" );
        String quakeDate = sdf.format( date );
        return String.format( "%s : %s %s", sdf.format( date ), magnitude, details );
    }
}
