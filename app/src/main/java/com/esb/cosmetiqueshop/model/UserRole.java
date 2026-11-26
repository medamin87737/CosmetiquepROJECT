package com.esb.cosmetiqueshop.model;

/** Rôles applicatifs : admin back-office ou client boutique. */
public enum UserRole {
    ADMIN,
    CLIENT;

    public static UserRole fromString(String value) {
        if (value == null) {
            return CLIENT;
        }
        try {
            return UserRole.valueOf(value);
        } catch (IllegalArgumentException e) {
            // Ancien rôle "DELIVERY" ou valeur inconnue → client
            return CLIENT;
        }
    }
}
