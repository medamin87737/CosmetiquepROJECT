package com.esb.cosmetiqueshop.model;

public enum UserRole {
    ADMIN,
    CLIENT,
    DELIVERY;

    public static UserRole fromString(String value) {
        if (value == null) {
            return CLIENT;
        }
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException e) {
            return CLIENT;
        }
    }
}