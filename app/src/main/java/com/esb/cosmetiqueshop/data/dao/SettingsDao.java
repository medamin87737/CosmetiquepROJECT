package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.AppSetting;

import java.util.HashMap;
import java.util.Map;

public class SettingsDao {

    private final DatabaseHelper dbHelper;

    public SettingsDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public String getValue(String key, String defaultValue) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.AppSettings.TABLE,
                new String[]{DbContract.AppSettings.COL_VALUE},
                DbContract.AppSettings.COL_KEY + "=?",
                new String[]{key}, null, null, null)) {
            if (c.moveToFirst()) {
                return c.getString(0);
            }
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String raw = getValue(key, null);
        if (raw == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public Map<String, String> getAll() {
        Map<String, String> map = new HashMap<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.AppSettings.TABLE, null, null, null, null, null, null)) {
            while (c.moveToNext()) {
                map.put(c.getString(c.getColumnIndexOrThrow(DbContract.AppSettings.COL_KEY)),
                        c.getString(c.getColumnIndexOrThrow(DbContract.AppSettings.COL_VALUE)));
            }
        }
        return map;
    }

    public boolean upsert(AppSetting setting) {
        ContentValues v = new ContentValues();
        v.put(DbContract.AppSettings.COL_KEY, setting.getKey());
        v.put(DbContract.AppSettings.COL_VALUE, setting.getValue());
        long id = dbHelper.getWritableDatabase().insertWithOnConflict(
                DbContract.AppSettings.TABLE, null, v, SQLiteDatabase.CONFLICT_REPLACE);
        return id != -1;
    }
}