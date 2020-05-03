package com.example.pothole;

import java.util.Comparator;

public class Loc implements Comparator {
    Object latitude;
    Object longitude;
    String sublocaloty;

    @Override
    public int compare(Object obj1, Object obj2) {
        Loc l1 = (Loc) obj1;
        Loc l2 = (Loc) obj2;
        Double lat1 = (Double) l1.latitude;
        Double lat2 = (Double) l2.latitude;
        Double long1 = (Double)l1.longitude;
        Double long2 = (Double)l2.longitude;


        if (lat1 > lat2) {
            return 1;
        } else if (lat1 < lat2) {
            return -1;
        } else {
            if (long1 > long2) {
                return 1;
            } else if (long1 < long2) {
                return -1;
            } else {
                return 0;
            }
        }

    }

    public String getSublocaloty() {
        return sublocaloty;
    }

    public void setSublocaloty(String sublocaloty) {
        this.sublocaloty = sublocaloty;
    }

    public Loc(Object latitude, Object longitude, String sublocaloty) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.sublocaloty = sublocaloty;
    }

    public Loc(Object latitude, Object longitude) {
        this.latitude = latitude;
        this.longitude = longitude;

    }



    @Override
    public String toString() {
        return "Loc{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

    public Object getLatitude() {
        return latitude;
    }

    public void setLatitude(Object latitude) {
        this.latitude = latitude;
    }

    public Object getLongitude() {
        return longitude;
    }

    public void setLongitude(Object longitude) {
        this.longitude = longitude;
    }
}
