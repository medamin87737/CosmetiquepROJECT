package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Review;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewDao {

    private final DatabaseHelper dbHelper;

    public ReviewDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Review> findByProduct(long productId) {
        List<Review> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT r.*, u." + DbContract.Users.COL_FULL_NAME + " AS user_name "
                + "FROM " + DbContract.Reviews.TABLE + " r "
                + "LEFT JOIN " + DbContract.Users.TABLE + " u ON r."
                + DbContract.Reviews.COL_USER_ID + " = u." + DbContract.Users.COL_ID
                + " WHERE r." + DbContract.Reviews.COL_PRODUCT_ID + "=?"
                + " ORDER BY r." + DbContract.Reviews.COL_CREATED_AT + " DESC";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(productId)})) {
            while (c.moveToNext()) {
                list.add(map(c));
            }
        }
        return list;
    }

    public boolean existsForUserAndProduct(long userId, long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Reviews.TABLE, new String[]{DbContract.Reviews.COL_ID},
                DbContract.Reviews.COL_USER_ID + "=? AND " + DbContract.Reviews.COL_PRODUCT_ID + "=?",
                new String[]{String.valueOf(userId), String.valueOf(productId)}, null, null, null)) {
            return c.moveToFirst();
        }
    }

    public long insert(Review review) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Reviews.COL_PRODUCT_ID, review.getProductId());
        v.put(DbContract.Reviews.COL_USER_ID, review.getUserId());
        if (review.getOrderId() != null) {
            v.put(DbContract.Reviews.COL_ORDER_ID, review.getOrderId());
        }
        v.put(DbContract.Reviews.COL_RATING, review.getRating());
        v.put(DbContract.Reviews.COL_COMMENT, review.getComment());
        v.put(DbContract.Reviews.COL_CREATED_AT, DateUtils.now());
        return dbHelper.getWritableDatabase().insert(DbContract.Reviews.TABLE, null, v);
    }

    private Review map(Cursor c) {
        Review r = new Review();
        r.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Reviews.COL_ID)));
        r.setProductId(c.getLong(c.getColumnIndexOrThrow(DbContract.Reviews.COL_PRODUCT_ID)));
        r.setUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.Reviews.COL_USER_ID)));
        int orderIdx = c.getColumnIndex(DbContract.Reviews.COL_ORDER_ID);
        if (orderIdx >= 0 && !c.isNull(orderIdx)) {
            r.setOrderId(c.getLong(orderIdx));
        }
        r.setRating(c.getInt(c.getColumnIndexOrThrow(DbContract.Reviews.COL_RATING)));
        r.setComment(c.getString(c.getColumnIndexOrThrow(DbContract.Reviews.COL_COMMENT)));
        r.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Reviews.COL_CREATED_AT)));
        int nameIdx = c.getColumnIndex("user_name");
        if (nameIdx >= 0) {
            r.setUserName(c.getString(nameIdx));
        }
        return r;
    }
}