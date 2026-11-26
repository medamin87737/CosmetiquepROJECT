package com.esb.cosmetiqueshop.model;

public class OrderItem {
    private long id;
    private long orderId;
    private long productId;
    private String productName;
    /** Image produit hydratée pour l’UI (non persistée dans order_items). */
    private String productImageUrl;
    private int quantity;
    private int unitPriceMillimes;

    public OrderItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getUnitPriceMillimes() {
        return unitPriceMillimes;
    }

    public void setUnitPriceMillimes(int unitPriceMillimes) {
        this.unitPriceMillimes = unitPriceMillimes;
    }

    public int getLineTotalMillimes() {
        return unitPriceMillimes * quantity;
    }
}