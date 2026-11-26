package com.esb.cosmetiqueshop.model;

public class Product {
    private long id;
    private long categoryId;
    private String name;
    private String brand;
    private String skinType;
    private String description;
    private String ingredients;
    private String tips;
    /** Prix catalogue en millimes (sans promo). */
    private int priceMillimes;
    /** Prix promo en millimes ; 0 ou négatif = pas de promo. */
    private int promoPriceMillimes;
    private int stock;
    private int alertThreshold = 10;
    private boolean active;
    private long createdAt;
    /** Nom de catégorie (jointure optionnelle pour l'affichage). */
    private String categoryName;
    /** Première image (chargée optionnellement). */
    private String primaryImageUrl;

    public Product() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSkinType() {
        return skinType;
    }

    public void setSkinType(String skinType) {
        this.skinType = skinType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getPriceMillimes() {
        return priceMillimes;
    }

    public void setPriceMillimes(int priceMillimes) {
        this.priceMillimes = priceMillimes;
    }

    public int getPromoPriceMillimes() {
        return promoPriceMillimes;
    }

    public void setPromoPriceMillimes(int promoPriceMillimes) {
        this.promoPriceMillimes = promoPriceMillimes;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(int alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    /** Prix effectif à la vente (promo si applicable). */
    public int getEffectivePriceMillimes() {
        if (promoPriceMillimes > 0 && promoPriceMillimes < priceMillimes) {
            return promoPriceMillimes;
        }
        return priceMillimes;
    }

    public boolean hasPromo() {
        return promoPriceMillimes > 0 && promoPriceMillimes < priceMillimes;
    }

    public String getPrimaryImageUrl() {
        return primaryImageUrl;
    }

    public void setPrimaryImageUrl(String primaryImageUrl) {
        this.primaryImageUrl = primaryImageUrl;
    }
}