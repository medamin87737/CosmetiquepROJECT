package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryDao {

    private final DatabaseHelper dbHelper;

    public CategoryDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Category> findAllOrdered() {
        List<Category> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Categories.TABLE, null, null, null, null, null,
                DbContract.Categories.COL_SORT_ORDER)) {
            while (c.moveToNext()) {
                list.add(map(c));
            }
        }
        return list;
    }

    public Category findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Categories.TABLE, null,
                DbContract.Categories.COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return map(c);
            }
        }
        return null;
    }

    public long insert(Category category) {
        ContentValues v = toValues(category);
        return dbHelper.getWritableDatabase().insert(DbContract.Categories.TABLE, null, v);
    }

    public boolean update(Category category) {
        ContentValues v = toValues(category);
        int rows = dbHelper.getWritableDatabase().update(DbContract.Categories.TABLE, v,
                DbContract.Categories.COL_ID + "=?", new String[]{String.valueOf(category.getId())});
        return rows > 0;
    }

    /** Suppression uniquement si aucun produit lié. */
    public boolean deleteIfEmpty(long categoryId, int productCount) {
        if (productCount > 0) {
            return false;
        }
        return delete(categoryId);
    }

    public boolean delete(long categoryId) {
        int rows = dbHelper.getWritableDatabase().delete(DbContract.Categories.TABLE,
                DbContract.Categories.COL_ID + "=?", new String[]{String.valueOf(categoryId)});
        return rows > 0;
    }

    /** Première autre catégorie (pour réaffectation avant suppression). */
    public Category findFirstOther(long excludeId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Categories.TABLE, null,
                DbContract.Categories.COL_ID + "!=?",
                new String[]{String.valueOf(excludeId)}, null, null,
                DbContract.Categories.COL_SORT_ORDER, "1")) {
            if (c.moveToFirst()) {
                return map(c);
            }
        }
        return null;
    }

    private ContentValues toValues(Category category) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Categories.COL_NAME, category.getName());
        v.put(DbContract.Categories.COL_DESCRIPTION, category.getDescription());
        v.put(DbContract.Categories.COL_IMAGE_URL, category.getImageUrl());
        v.put(DbContract.Categories.COL_SORT_ORDER, category.getSortOrder());
        return v;
    }

    private Category map(Cursor c) {
        Category cat = new Category();
        cat.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Categories.COL_ID)));
        cat.setName(c.getString(c.getColumnIndexOrThrow(DbContract.Categories.COL_NAME)));
        cat.setDescription(c.getString(c.getColumnIndexOrThrow(DbContract.Categories.COL_DESCRIPTION)));
        cat.setImageUrl(c.getString(c.getColumnIndexOrThrow(DbContract.Categories.COL_IMAGE_URL)));
        cat.setSortOrder(c.getInt(c.getColumnIndexOrThrow(DbContract.Categories.COL_SORT_ORDER)));
        return cat;
    }
}