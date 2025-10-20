package com.hacom.telecom.order_processing_service.model;

public class OrderItem {
    
    private String itemId;
    private String productName;
    private int quantity;
    private double price;

    public OrderItem() {
    }

    public OrderItem(String itemId, String productName, int quantity, double price) {
        this.itemId = itemId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
