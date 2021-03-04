package com.zpclient.ride;

public class RideListData {
    private final String rideID;
    private final String rideStatus;
    private final String rideDate;
    private final String rideVtype;
    private final String rideSrc;
    private final String rideDst;

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

    public String getRideSrc() {
        return rideSrc;
    }

    public String getRideDst() {
        return rideDst;
    }

    public RideListData(String rideID, String rideStatus, String rideDate, String rideVtype, String rideSrc, String rideDst) {
        this.rideID = rideID;
        this.rideStatus = rideStatus;
        this.rideDate = rideDate;
        this.rideVtype = rideVtype;
        this.rideSrc = rideSrc;
        this.rideDst = rideDst;
    }

}
