package com.client.deliver;

public class DeliveryListData {
    private String deliveryID;
    private String deliveryPrice;
    private String deliveryStatus;
    private String deliveryTip;


    public String getDeliveryID() {
        return deliveryID;
    }

    public String getDeliveryPrice() {
        return deliveryPrice;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }
    public String getDeliveryTip() {
        return deliveryTip;
    }

    public DeliveryListData(String deliveryID, String deliveryPrice, String deliveryStatus, String deliveryTip) {
        this.deliveryID = deliveryID;
        this.deliveryPrice = deliveryPrice;
        this.deliveryStatus = deliveryStatus;
        this.deliveryTip = deliveryTip;
    }
}
