package com.junjunguo.pocketmaps.model;


import org.oscim.core.GeoPoint;

public class GroupMember {

    private String UID = null;
    private GeoPoint location = null;

    public GroupMember() {
        // Do nothing.
    }

    public String getUID() {
        return UID;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public String setUID (String UID) {
        this.UID = UID;
        return this.UID;
    }

    public GeoPoint setLocation (GeoPoint location) {
        this.location = location;
        return this.location;
    }
}
