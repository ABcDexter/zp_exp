package com.clientzp;

public class HubListData {

    private final String hubName;
    private final String latitude;
    private final String longitude;
    private final String idPlace;

    public String getHubName() {
        return hubName;
    }

    public String getIdPlace() {
        return idPlace;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }


    public HubListData(String hubName, String idPlace, String latitude, String longitude) {
        this.hubName = hubName;
        this.idPlace = idPlace;
        this.latitude = latitude;
        this.longitude = longitude;
    }


}
