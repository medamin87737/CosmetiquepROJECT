package com.esb.cosmetiqueshop.model;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private long id;
    private long userId;
    private OrderStatus status;
    private int subtotalMillimes;
    private int deliveryMillimes;
    private int totalMillimes;
    private String deliveryAddress;
    private String deliveryPhone;
    private String paymentMethod;
    private long createdAt;
    private long updatedAt;
    /** Nom client hydraté pour l’UI admin (non persisté). */
    private String clientName;
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
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

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public int getSubtotalMillimes() {
        return subtotalMillimes;
    }

    public void setSubtotalMillimes(int subtotalMillimes) {
        this.subtotalMillimes = subtotalMillimes;
    }

    public int getDeliveryMillimes() {
        return deliveryMillimes;
    }

    public void setDeliveryMillimes(int deliveryMillimes) {
        this.deliveryMillimes = deliveryMillimes;
    }

    public int getTotalMillimes() {
        return totalMillimes;
    }

    public void setTotalMillimes(int totalMillimes) {
        this.totalMillimes = totalMillimes;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public String getDeliveryPhone() {
        return deliveryPhone;
    }

    public void setDeliveryPhone(String deliveryPhone) {
        this.deliveryPhone = deliveryPhone;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items != null ? items : new ArrayList<>();
    }
}