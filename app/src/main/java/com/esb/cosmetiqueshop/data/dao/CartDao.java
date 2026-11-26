package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CartDao {

    private final DatabaseHelper dbHelper;

    public CartDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<CartItem> findByUser(long userId) {
        List<CartItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.CartItems.TABLE, null,
                DbContract.CartItems.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null, null)) {
            while (c.moveToNext()) {
                list.add(map(c));
            }
        }
        return list;
    }

    public long upsertQuantity(long userId, long productId, int quantity) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.query(DbContract.CartItems.TABLE, new String[]{DbContract.CartItems.COL_ID},
                DbContract.CartItems.COL_USER_ID + "=? AND " + DbContract.CartItems.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)},
                null, null, null)) {
            ContentValues v = new ContentValues();
            v.put(DbContract.CartItems.COL_QUANTITY, quantity);
            if (c.moveToFirst()) {
                long id = c.getLong(0);
                db.update(DbContract.CartItems.TABLE, v,
                        DbContract.CartItems.COL_ID + "=?", new String[]{String.valueOf(id)});
                return id;
            }
            v.put(DbContract.CartItems.COL_USER_ID, userId);
            v.put(DbContract.CartItems.COL_PRODUCT_ID, productId);
            return db.insert(DbContract.CartItems.TABLE, null, v);
        }
    }

    public boolean removeItem(long userId, long productId) {
        int rows = dbHelper.getWritableDatabase().delete(DbContract.CartItems.TABLE,
                DbContract.CartItems.COL_USER_ID + "=? AND " + DbContract.CartItems.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)});
        return rows > 0;
    }

    public void clearForUser(long userId) {
        dbHelper.getWritableDatabase().delete(DbContract.CartItems.TABLE,
                DbContract.CartItems.COL_USER_ID + "=?", new String[]{String.valueOf(userId)});
    }

    private CartItem map(Cursor c) {
        CartItem item = new CartItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.CartItems.COL_ID)));
        item.setUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.CartItems.COL_USER_ID)));
        item.setProductId(c.getLong(c.getColumnIndexOrThrow(DbContract.CartItems.COL_PRODUCT_ID)));
        item.setQuantity(c.getInt(c.getColumnIndexOrThrow(DbContract.CartItems.COL_QUANTITY)));
        return item;
    }
}