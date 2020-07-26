package com.client.deliver;

public class DeliveryListData {
    private String deliveryID;
    private String deliveryPrice;
    private String deliveryStatus;


    public String getDeliveryID() {
        return deliveryID;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public DeliveryListData(String deliveryID, String deliveryPrice, String deliveryStatus) {
        this.deliveryID = deliveryID;
        this.deliveryPrice = deliveryPrice;
        this.deliveryStatus = deliveryStatus;
    }
}
