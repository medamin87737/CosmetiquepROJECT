package com.esb.cosmetiqueshop.model;

public class CartItem {
    private long id;
    private long userId;
    private long productId;
    private int quantity;
    /** Produit hydraté pour l'UI (optionnel). */
    private Product product;

    public CartItem() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getLineTotalMillimes() {
        if (product == null) {
            return 0;
        }
        return product.getEffectivePriceMillimes() * quantity;
    }
}