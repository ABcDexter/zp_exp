package com.zpclient.deliver;

public class DeliveryListData {
    private final String deliveryID;
    private final String deliveryPrice;
    private final String deliveryStatus;
    private final String deliveryIType;


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
