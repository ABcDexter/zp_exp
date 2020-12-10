package com.e.purchasedeptapp;

import java.util.List;

public class Product {
    //int id;
    String id, keyy, name, category, stock_qnt, cost_price, regular_price, weight;

    private List<String> list;

   public List<String> getList() {
       return list;
   }

    public Product() {
    }

    public Product(String id, String keyy, String name, String category, String stock_qnt, String cost_price, String regular_price, String weight) {
        this.id = id;
        this.keyy = keyy;
        this.name = name;
        this.category = category;
        this.stock_qnt = stock_qnt;
        this.cost_price = cost_price;
        this.regular_price = regular_price;
        this.weight = weight;
    }
    public Product(String name,String keyy, String stock_qnt, String cost_price, String regular_price, String weight) {

        this.keyy = keyy;
        this.name = name;
        this.stock_qnt = stock_qnt;
        this.cost_price = cost_price;
        this.regular_price = regular_price;
        this.weight = weight;
    }
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getKeyy() {
        return keyy;
    }

    public String getCategory() {
        return category;
    }

    public String getStockQnt() {
        return stock_qnt;
    }

    public String getCostPrice() {
        return cost_price;
    }

    public String getRegularPrice() {
        return regular_price;
    }

    public String getWeight() {
        return weight;
    }
}