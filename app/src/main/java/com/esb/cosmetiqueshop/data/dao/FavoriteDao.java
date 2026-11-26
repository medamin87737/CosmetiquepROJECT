package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Favorite;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class FavoriteDao {

    private final DatabaseHelper dbHelper;

    public FavoriteDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Favorite> findByUser(long userId) {
        List<Favorite> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Favorites.TABLE, null,
                DbContract.Favorites.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                DbContract.Favorites.COL_CREATED_AT + " DESC")) {
            while (c.moveToNext()) {
                list.add(map(c));
            }
        }
        return list;
    }

    public boolean isFavorite(long userId, long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Favorites.TABLE, new String[]{DbContract.Favorites.COL_ID},
                DbContract.Favorites.COL_USER_ID + "=? AND " + DbContract.Favorites.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null)) {
            return c.moveToFirst();
        }
    }

    public long add(long userId, long productId) {
        if (isFavorite(userId, productId)) {
            return -1;
        }
        ContentValues v = new ContentValues();
        v.put(DbContract.Favorites.COL_USER_ID, userId);
        v.put(DbContract.Favorites.COL_PRODUCT_ID, productId);
        v.put(DbContract.Favorites.COL_CREATED_AT, DateUtils.now());
        return dbHelper.getWritableDatabase().insert(DbContract.Favorites.TABLE, null, v);
    }

    public boolean remove(long userId, long productId) {
        int rows = dbHelper.getWritableDatabase().delete(DbContract.Favorites.TABLE,
                DbContract.Favorites.COL_USER_ID + "=? AND " + DbContract.Favorites.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        return rows > 0;
    }

    private Favorite map(Cursor c) {
        Favorite f = new Favorite();
        f.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Favorites.COL_ID)));
        f.setUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.Favorites.COL_USER_ID)));
        f.setProductId(c.getLong(c.getColumnIndexOrThrow(DbContract.Favorites.COL_PRODUCT_ID)));
        f.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Favorites.COL_CREATED_AT)));
        return f;
    }
}