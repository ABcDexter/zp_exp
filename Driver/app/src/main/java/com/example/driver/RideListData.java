package com.example.driver;

public class RideListData {
    private String rideID, rideStatus, rideDate, rideVtype;

    public String getRideVtype() {
        return rideVtype;
    }

    public String getRideID() {
        return rideID;
    }


    public String getRideStatus() {
        return rideStatus;
    }


    public String getRideDate() {
        return rideDate;
    }

    public RideListData(String rideID, String rideStatus, String rideDate, String rideVtype) {
        this.rideID = rideID;
        this.rideStatus = rideStatus;
        this.rideDate = rideDate;
        this.rideVtype = rideVtype;
    }
}
