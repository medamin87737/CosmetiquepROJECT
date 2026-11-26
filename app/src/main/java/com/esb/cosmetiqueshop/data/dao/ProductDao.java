package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.model.ProductImage;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ProductDao {

    private final DatabaseHelper dbHelper;

    public ProductDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Product> findAllActive() {
        return queryProducts("p." + DbContract.Products.COL_IS_ACTIVE + "=?", new String[]{"1"}, "name ASC");
    }

    /** Admin : tous les produits, actifs ou masqués. */
    public List<Product> findAllForAdmin() {
        return queryProducts(null, null, "name ASC");
    }

    public List<Product> findByCategory(long categoryId) {
        return queryProducts("p." + DbContract.Products.COL_CATEGORY_ID + "=? AND "
                        + "p." + DbContract.Products.COL_IS_ACTIVE + "=?",
                new String[]{String.valueOf(categoryId), "1"}, DbContract.Products.COL_NAME);
    }

    public Product findById(long id) {
        List<Product> list = queryProducts("p." + DbContract.Products.COL_ID + "=?",
                new String[]{String.valueOf(id)}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<Product> searchByName(String query) {
        String like = "%" + query.trim() + "%";
        return queryProducts("p." + DbContract.Products.COL_NAME + " LIKE ? AND "
                        + "p." + DbContract.Products.COL_IS_ACTIVE + "=?",
                new String[]{like, "1"}, DbContract.Products.COL_NAME);
    }

    public long insert(Product product) {
        ContentValues v = toValues(product);
        v.put(DbContract.Products.COL_CREATED_AT, DateUtils.now());
        return dbHelper.getWritableDatabase().insert(DbContract.Products.TABLE, null, v);
    }

    public boolean update(Product product) {
        ContentValues v = toValues(product);
        int rows = dbHelper.getWritableDatabase().update(DbContract.Products.TABLE, v,
                DbContract.Products.COL_ID + "=?", new String[]{String.valueOf(product.getId())});
        return rows > 0;
    }

    /** Ajuste le stock (delta positif ou négatif). */
    public boolean adjustStock(SQLiteDatabase db, long productId, int delta) {
        Product p = findByIdInTransaction(db, productId);
        if (p == null) {
            return false;
        }
        int newStock = p.getStock() + delta;
        if (newStock < 0) {
            return false;
        }
        ContentValues v = new ContentValues();
        v.put(DbContract.Products.COL_STOCK, newStock);
        int rows = db.update(DbContract.Products.TABLE, v,
                DbContract.Products.COL_ID + "=?", new String[]{String.valueOf(productId)});
        return rows > 0;
    }

    public Product findByIdInTransaction(SQLiteDatabase db, long id) {
        try (Cursor c = db.query(DbContract.Products.TABLE, null,
                DbContract.Products.COL_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null)) {
            if (c.moveToFirst()) {
                return mapProduct(c, null);
            }
        }
        return null;
    }

    public List<ProductImage> findImagesForProduct(long productId) {
        List<ProductImage> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.ProductImages.TABLE, null,
                DbContract.ProductImages.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(productId)}, null, null,
                DbContract.ProductImages.COL_SORT_ORDER)) {
            while (c.moveToNext()) {
                ProductImage img = new ProductImage();
                img.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.ProductImages.COL_ID)));
                img.setProductId(productId);
                img.setImageUrl(c.getString(c.getColumnIndexOrThrow(DbContract.ProductImages.COL_IMAGE_URL)));
                img.setSortOrder(c.getInt(c.getColumnIndexOrThrow(DbContract.ProductImages.COL_SORT_ORDER)));
                list.add(img);
            }
        }
        return list;
    }

    public int countActive() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Products.TABLE
                + " WHERE " + DbContract.Products.COL_IS_ACTIVE + "=1", null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public int countLowStock(int threshold) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Products.TABLE
                + " WHERE " + DbContract.Products.COL_STOCK + " <= ?", new String[]{String.valueOf(threshold)})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    /** Met à jour ou crée l'image principale (sort_order 0). */
    public void replacePrimaryImage(long productId, String url) {
        if (url == null || url.trim().isEmpty()) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String trimmed = url.trim();
        try (Cursor c = db.query(DbContract.ProductImages.TABLE, null,
                DbContract.ProductImages.COL_PRODUCT_ID + "=? AND "
                        + DbContract.ProductImages.COL_SORT_ORDER + "=?",
                new String[]{String.valueOf(productId), "0"}, null, null, null)) {
            ContentValues v = new ContentValues();
            v.put(DbContract.ProductImages.COL_IMAGE_URL, trimmed);
            if (c.moveToFirst()) {
                long imageId = c.getLong(c.getColumnIndexOrThrow(DbContract.ProductImages.COL_ID));
                db.update(DbContract.ProductImages.TABLE, v,
                        DbContract.ProductImages.COL_ID + "=?", new String[]{String.valueOf(imageId)});
            } else {
                v.put(DbContract.ProductImages.COL_PRODUCT_ID, productId);
                v.put(DbContract.ProductImages.COL_SORT_ORDER, 0);
                db.insert(DbContract.ProductImages.TABLE, null, v);
            }
        }
    }

    public String findPrimaryImageUrl(long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.ProductImages.TABLE,
                new String[]{DbContract.ProductImages.COL_IMAGE_URL},
                DbContract.ProductImages.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(productId)}, null, null,
                DbContract.ProductImages.COL_SORT_ORDER + " ASC", "1")) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return null;
    }

    private List<Product> queryProducts(String where, String[] args, String order) {
        List<Product> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT p.*, c." + DbContract.Categories.COL_NAME + " AS category_name, "
                + "(SELECT pi." + DbContract.ProductImages.COL_IMAGE_URL
                + " FROM " + DbContract.ProductImages.TABLE + " pi WHERE pi."
                + DbContract.ProductImages.COL_PRODUCT_ID + " = p." + DbContract.Products.COL_ID
                + " ORDER BY pi." + DbContract.ProductImages.COL_SORT_ORDER + " ASC LIMIT 1) AS primary_image_url "
                + "FROM " + DbContract.Products.TABLE + " p "
                + "LEFT JOIN " + DbContract.Categories.TABLE + " c ON p."
                + DbContract.Products.COL_CATEGORY_ID + " = c." + DbContract.Categories.COL_ID;
        if (where != null) {
            sql += " WHERE " + where;
        }
        if (order != null) {
            sql += " ORDER BY p." + order;
        }
        try (Cursor c = db.rawQuery(sql, args)) {
            while (c.moveToNext()) {
                String categoryName = null;
                int catNameIdx = c.getColumnIndex("category_name");
                if (catNameIdx >= 0) {
                    categoryName = c.getString(catNameIdx);
                }
                list.add(mapProduct(c, categoryName));
            }
        }
        return list;
    }

    private Product mapProduct(Cursor c, String categoryName) {
        Product p = new Product();
        p.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Products.COL_ID)));
        p.setCategoryId(c.getLong(c.getColumnIndexOrThrow(DbContract.Products.COL_CATEGORY_ID)));
        p.setName(c.getString(c.getColumnIndexOrThrow(DbContract.Products.COL_NAME)));
        p.setDescription(c.getString(c.getColumnIndexOrThrow(DbContract.Products.COL_DESCRIPTION)));
        p.setPriceMillimes(c.getInt(c.getColumnIndexOrThrow(DbContract.Products.COL_PRICE_MILLIMES)));
        p.setPromoPriceMillimes(c.getInt(c.getColumnIndexOrThrow(DbContract.Products.COL_PROMO_PRICE_MILLIMES)));
        p.setStock(c.getInt(c.getColumnIndexOrThrow(DbContract.Products.COL_STOCK)));
        p.setActive(c.getInt(c.getColumnIndexOrThrow(DbContract.Products.COL_IS_ACTIVE)) == 1);
        p.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Products.COL_CREATED_AT)));
        p.setCategoryName(categoryName);
        putOptionalString(c, DbContract.Products.COL_BRAND, p::setBrand);
        putOptionalString(c, DbContract.Products.COL_SKIN_TYPE, p::setSkinType);
        putOptionalString(c, DbContract.Products.COL_INGREDIENTS, p::setIngredients);
        putOptionalString(c, DbContract.Products.COL_TIPS, p::setTips);
        int alertIdx = c.getColumnIndex(DbContract.Products.COL_ALERT_THRESHOLD);
        if (alertIdx >= 0 && !c.isNull(alertIdx)) {
            p.setAlertThreshold(c.getInt(alertIdx));
        }
        int primaryIdx = c.getColumnIndex("primary_image_url");
        if (primaryIdx >= 0 && !c.isNull(primaryIdx)) {
            p.setPrimaryImageUrl(c.getString(primaryIdx));
        }
        return p;
    }

    private interface StringSetter {
        void set(String value);
    }

    private void putOptionalString(Cursor c, String col, StringSetter setter) {
        int idx = c.getColumnIndex(col);
        if (idx >= 0 && !c.isNull(idx)) {
            setter.set(c.getString(idx));
        }
    }

    public int countByCategory(long categoryId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Products.TABLE
                        + " WHERE " + DbContract.Products.COL_CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    /** Déplace tous les produits d’une catégorie vers une autre. */
    public int reassignCategory(long fromCategoryId, long toCategoryId) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Products.COL_CATEGORY_ID, toCategoryId);
        return dbHelper.getWritableDatabase().update(DbContract.Products.TABLE, v,
                DbContract.Products.COL_CATEGORY_ID + "=?",
                new String[]{String.valueOf(fromCategoryId)});
    }

    /** Marques déjà présentes sur les produits (pour sélection admin). */
    public List<String> findDistinctBrands() {
        List<String> brands = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery(
                "SELECT DISTINCT " + DbContract.Products.COL_BRAND
                        + " FROM " + DbContract.Products.TABLE
                        + " WHERE " + DbContract.Products.COL_BRAND + " IS NOT NULL"
                        + " AND TRIM(" + DbContract.Products.COL_BRAND + ") != ''"
                        + " ORDER BY " + DbContract.Products.COL_BRAND,
                null)) {
            while (c.moveToNext()) {
                brands.add(c.getString(0));
            }
        }
        return brands;
    }

    private ContentValues toValues(Product product) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Products.COL_CATEGORY_ID, product.getCategoryId());
        v.put(DbContract.Products.COL_NAME, product.getName());
        v.put(DbContract.Products.COL_BRAND, product.getBrand());
        v.put(DbContract.Products.COL_SKIN_TYPE, product.getSkinType());
        v.put(DbContract.Products.COL_DESCRIPTION, product.getDescription());
        v.put(DbContract.Products.COL_INGREDIENTS, product.getIngredients());
        v.put(DbContract.Products.COL_TIPS, product.getTips());
        v.put(DbContract.Products.COL_PRICE_MILLIMES, product.getPriceMillimes());
        v.put(DbContract.Products.COL_PROMO_PRICE_MILLIMES, product.getPromoPriceMillimes());
        v.put(DbContract.Products.COL_STOCK, product.getStock());
        v.put(DbContract.Products.COL_ALERT_THRESHOLD, product.getAlertThreshold());
        v.put(DbContract.Products.COL_IS_ACTIVE, product.isActive() ? 1 : 0);
        return v;
    }
}