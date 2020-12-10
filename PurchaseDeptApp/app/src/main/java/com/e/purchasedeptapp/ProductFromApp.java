package com.e.purchasedeptapp;

import java.util.List;

public class ProductFromApp {
    int _id;
    String _key, _name, _stock_quantity, _cost_price, _regular_price, _weight;

    public ProductFromApp() {
    }

    private List<String> list;

    public List<String> getList() {
        return list;
    }

    public ProductFromApp(int id, String key, String name) {
        this._id = id;
        this._key = key;
        this._name = name;
    }

    public ProductFromApp(String _name, String _key, String _stock_quantity, String _cost_price, String _regular_price, String _weight) {
        this._name = _name;
        this._key = _key;
        this._stock_quantity = _stock_quantity;
        this._cost_price = _cost_price;
        this._regular_price = _regular_price;
        this._weight = _weight;
    }

    public ProductFromApp(String _name, String _stock_quantity, String _cost_price, String _regular_price, String _weight) {
        this._name = _name;
        this._stock_quantity = _stock_quantity;
        this._cost_price = _cost_price;
        this._regular_price = _regular_price;
        this._weight = _weight;
    }

    public int getID() {
        return this._id;
    }

    public String get_key() {
        return _key;
    }

    public String get_name() {
        return this._name;
    }

    public String get_stock_quantity() {
        return _stock_quantity;
    }

    public String get_cost_price() {
        return _cost_price;
    }

    public String get_regular_price() {
        return _regular_price;
    }

    public String get_weight() {
        return _weight;
    }

}
