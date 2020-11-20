package com.client.deliver;

public class DeliveryListData {
    private String deliveryID;
    private String deliveryPrice;
    private String deliveryStatus;
    private String deliveryTip;
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

    public String getDeliveryTip() {
        return deliveryTip;
    }

    public String getDeliveryIType() {
        return deliveryIType;
    }

    public DeliveryListData(String deliveryID, String deliveryPrice, String deliveryStatus, String deliveryTip, String deliveryIType) {
        this.deliveryID = deliveryID;
        this.deliveryPrice = deliveryPrice;
        this.deliveryStatus = deliveryStatus;
        this.deliveryTip = deliveryTip;
        this.deliveryIType = deliveryIType;
    }
}
