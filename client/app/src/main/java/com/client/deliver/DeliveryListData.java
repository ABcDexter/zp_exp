package com.client.deliver;

public class DeliveryListData {
    private String deliveryID;
    private String deliveryPrice;
    private String deliveryStatus;
    private String deliveryIType;


    public String getDeliveryID() {
        return deliveryID;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public String getDeliveryIType() {
        return deliveryIType;
    }

    public DeliveryListData(String deliveryID,String deliveryStatus, String deliveryPrice, String deliveryIType) {
        this.deliveryID = deliveryID;
        this.deliveryPrice = deliveryPrice;
        this.deliveryStatus = deliveryStatus;
        this.deliveryIType = deliveryIType;
    }
}
