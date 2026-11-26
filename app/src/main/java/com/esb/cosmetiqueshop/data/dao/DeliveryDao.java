package com.esb.cosmetiqueshop.data.dao;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.DeliveryAgent;

import java.util.ArrayList;
import java.util.List;

public class DeliveryDao {

    private final DatabaseHelper dbHelper;

    public DeliveryDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<DeliveryAgent> findAll() {
        List<DeliveryAgent> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT d.*, u." + DbContract.Users.COL_FULL_NAME + ", u."
                + DbContract.Users.COL_PHONE + ", u." + DbContract.Users.COL_EMAIL
                + " FROM " + DbContract.DeliveryAgents.TABLE + " d "
                + "INNER JOIN " + DbContract.Users.TABLE + " u ON d."
                + DbContract.DeliveryAgents.COL_USER_ID + " = u." + DbContract.Users.COL_ID;
        try (Cursor c = db.rawQuery(sql, null)) {
            while (c.moveToNext()) {
                list.add(map(c));
            }
        }
        return list;
    }

    public DeliveryAgent findById(long agentId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT d.*, u." + DbContract.Users.COL_FULL_NAME + ", u."
                + DbContract.Users.COL_PHONE + ", u." + DbContract.Users.COL_EMAIL
                + " FROM " + DbContract.DeliveryAgents.TABLE + " d "
                + "INNER JOIN " + DbContract.Users.TABLE + " u ON d."
                + DbContract.DeliveryAgents.COL_USER_ID + " = u." + DbContract.Users.COL_ID
                + " WHERE d." + DbContract.DeliveryAgents.COL_ID + "=?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(agentId)})) {
            if (c.moveToFirst()) {
                return map(c);
            }
        }
        return null;
    }

    public DeliveryAgent findByUserId(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT d.*, u." + DbContract.Users.COL_FULL_NAME + ", u."
                + DbContract.Users.COL_PHONE + ", u." + DbContract.Users.COL_EMAIL
                + " FROM " + DbContract.DeliveryAgents.TABLE + " d "
                + "INNER JOIN " + DbContract.Users.TABLE + " u ON d."
                + DbContract.DeliveryAgents.COL_USER_ID + " = u." + DbContract.Users.COL_ID
                + " WHERE d." + DbContract.DeliveryAgents.COL_USER_ID + "=?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(userId)})) {
            if (c.moveToFirst()) {
                return map(c);
            }
        }
        return null;
    }

    private DeliveryAgent map(Cursor c) {
        DeliveryAgent a = new DeliveryAgent();
        a.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.DeliveryAgents.COL_ID)));
        a.setUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.DeliveryAgents.COL_USER_ID)));
        a.setVehicleInfo(c.getString(c.getColumnIndexOrThrow(DbContract.DeliveryAgents.COL_VEHICLE_INFO)));
        a.setAvailable(c.getInt(c.getColumnIndexOrThrow(DbContract.DeliveryAgents.COL_IS_AVAILABLE)) == 1);
        a.setFullName(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_FULL_NAME)));
        a.setPhone(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_PHONE)));
        a.setEmail(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_EMAIL)));
        return a;
    }
}