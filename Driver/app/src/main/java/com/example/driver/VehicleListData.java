package com.example.driver;

public class VehicleListData {

    private String regnVehicle;
    private String typeVehicle;
    private String anVehicle;

    public String getRegnVehicle() {
        return regnVehicle;
    }

    public String getAnVehicle() {
        return anVehicle;
    }

    public String getTypeVehicle() {
        return typeVehicle;
    }

    public VehicleListData( String anVehicle, String regnVehicle, String typeVehicle) {
        this.regnVehicle = regnVehicle;
        this.typeVehicle = typeVehicle;
        this.anVehicle = anVehicle;
    }


}
