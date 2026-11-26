package com.esb.cosmetiqueshop.model;

public class OrderStatusHistory {
    private long id;
    private long orderId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;
    private long changedByUserId;
    private String note;
    private long createdAt;

    public OrderStatusHistory() {
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

    public OrderStatus getOldStatus() {
        return oldStatus;
    }

    public void setOldStatus(OrderStatus oldStatus) {
        this.oldStatus = oldStatus;
    }

    public OrderStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(OrderStatus newStatus) {
        this.newStatus = newStatus;
    }

    public long getChangedByUserId() {
        return changedByUserId;
    }

    public void setChangedByUserId(long changedByUserId) {
        this.changedByUserId = changedByUserId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}