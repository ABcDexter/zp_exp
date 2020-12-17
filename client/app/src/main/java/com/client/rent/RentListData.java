package com.client.rent;

public class RentListData {
    private String rentID, rentStatus, rentDate, rentVtype;

    public RentListData(String rentID, String rentStatus, String rentDate, String rentVtype) {
        this.rentID = rentID;
        this.rentStatus = rentStatus;
        this.rentDate = rentDate;
        this.rentVtype = rentVtype;
    }

    public String getRentID() {
        return rentID;
    }

    public String getRentStatus() {
        return rentStatus;
    }

    public String getRentDate() {
        return rentDate;
    }

    public String getRentVtype() {
        return rentVtype;
    }
}
