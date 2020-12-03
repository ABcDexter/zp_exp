package com.e.purchasedeptapp;

public class ProductFromApp {
    int _id;
    String _key;
    String _name;
    String _stock_quantity;
    String _cost_price;
    String _regular_price;
    String _weight;

    public ProductFromApp() {
    }

    public ProductFromApp(int id, String key, String name) {
        this._id = id;
        this._key = key;
        this._name = name;
    }

    public ProductFromApp(String _key, String _stock_quantity, String _cost_price, String _regular_price, String _weight) {
        this._key = _key;
        this._stock_quantity = _stock_quantity;
        this._cost_price = _cost_price;
        this._regular_price = _regular_price;
        this._weight = _weight;
    }

    public int getID() {
        return this._id;
    }

    public void setID(int id) {
        this._id = id;
    }

    public String get_key() {
        return _key;
    }

    public void set_key(String _key) {
        this._key = _key;
    }

    public String getName() {
        return this._name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public String getStockQuantity() {
        return _stock_quantity;
    }

    public void setStockQuantity(String _stock_quantity) {
        this._stock_quantity = _stock_quantity;
    }

    public String getCostPrice() {
        return _cost_price;
    }

    public void setCostPrice(String _cost_price) {
        this._cost_price = _cost_price;
    }

    public String getRegularPrice() {
        return _regular_price;
    }

    public void setRegularPrice(String _regular_price) {
        this._regular_price = _regular_price;
    }

    public String getWeight() {
        return _weight;
    }

    public void setWeight(String _weight) {
        this._weight = _weight;
    }
}
