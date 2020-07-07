package com.client;

public class HubListData {

    private String hubName;
  /*  private String latitude;
    private String longitude;
*/private String idPlace;

  public String getHubName() {
        return hubName;
    }

    public String getIdPlace() {
        return idPlace;
    }
/* public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }*/


    public HubListData(String hubName, String idPlace/*, String latitude, String longitude*/) {
        this.hubName = hubName;
        this.idPlace = idPlace;
        /*this.latitude = latitude;
        this.longitude = longitude;*/
    }


}
