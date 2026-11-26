package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.NotificationItem;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    private final DatabaseHelper dbHelper;

    public NotificationDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<NotificationItem> findByUser(long userId) {
        List<NotificationItem> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Notifications.TABLE, null,
                DbContract.Notifications.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                DbContract.Notifications.COL_CREATED_AT + " DESC")) {
            while (c.moveToNext()) {
                list.add(map(c));
            }
        }
        return list;
    }

    public int countUnread(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Notifications.TABLE
                + " WHERE " + DbContract.Notifications.COL_USER_ID + "=? AND "
                + DbContract.Notifications.COL_IS_READ + "=0",
                new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public long insert(NotificationItem item) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Notifications.COL_USER_ID, item.getUserId());
        v.put(DbContract.Notifications.COL_TITLE, item.getTitle());
        v.put(DbContract.Notifications.COL_BODY, item.getBody());
        v.put(DbContract.Notifications.COL_TYPE, item.getType());
        v.put(DbContract.Notifications.COL_IS_READ, item.isRead() ? 1 : 0);
        if (item.getReferenceId() != null) {
            v.put(DbContract.Notifications.COL_REFERENCE_ID, item.getReferenceId());
        }
        v.put(DbContract.Notifications.COL_CREATED_AT, DateUtils.now());
        return dbHelper.getWritableDatabase().insert(DbContract.Notifications.TABLE, null, v);
    }

    public boolean markRead(long notificationId) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Notifications.COL_IS_READ, 1);
        int rows = dbHelper.getWritableDatabase().update(DbContract.Notifications.TABLE, v,
                DbContract.Notifications.COL_ID + "=?", new String[]{String.valueOf(notificationId)});
        return rows > 0;
    }

    private NotificationItem map(Cursor c) {
        NotificationItem n = new NotificationItem();
        n.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Notifications.COL_ID)));
        n.setUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.Notifications.COL_USER_ID)));
        n.setTitle(c.getString(c.getColumnIndexOrThrow(DbContract.Notifications.COL_TITLE)));
        n.setBody(c.getString(c.getColumnIndexOrThrow(DbContract.Notifications.COL_BODY)));
        n.setType(c.getString(c.getColumnIndexOrThrow(DbContract.Notifications.COL_TYPE)));
        n.setRead(c.getInt(c.getColumnIndexOrThrow(DbContract.Notifications.COL_IS_READ)) == 1);
        int refIdx = c.getColumnIndex(DbContract.Notifications.COL_REFERENCE_ID);
        if (refIdx >= 0 && !c.isNull(refIdx)) {
            n.setReferenceId(c.getLong(refIdx));
        }
        n.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Notifications.COL_CREATED_AT)));
        return n;
    }
}