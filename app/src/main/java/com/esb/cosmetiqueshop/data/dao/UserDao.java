package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.model.UserRole;
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.PasswordHasher;

import java.util.ArrayList;
import java.util.List;

public class UserDao {

    private final DatabaseHelper dbHelper;

    public UserDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public User findByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Users.TABLE, null,
                DbContract.Users.COL_EMAIL + "=?",
                new String[]{email}, null, null, null)) {
            if (c.moveToFirst()) {
                return mapUser(c);
            }
        }
        return null;
    }

    public User findById(long id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Users.TABLE, null,
                DbContract.Users.COL_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return mapUser(c);
            }
        }
        return null;
    }

    public boolean verifyCredentials(String email, String password, User[] outUser) {
        User user = findByEmail(email);
        if (user == null || !user.isActive()) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Users.TABLE,
                new String[]{DbContract.Users.COL_PASSWORD_HASH, DbContract.Users.COL_SALT},
                DbContract.Users.COL_ID + "=?",
                new String[]{String.valueOf(user.getId())}, null, null, null)) {
            if (!c.moveToFirst()) {
                return false;
            }
            String hash = c.getString(0);
            String salt = c.getString(1);
            if (!PasswordHasher.verify(password, salt, hash)) {
                return false;
            }
            if (outUser != null && outUser.length > 0) {
                outUser[0] = user;
            }
            return true;
        }
    }

    public long insertClient(String email, String password, String fullName, String phone) {
        return insertUser(email, password, fullName, phone, UserRole.CLIENT, false, true);
    }

    public long insertUser(String email, String password, String fullName, String phone,
                           UserRole role, boolean mustChangePassword, boolean active) {
        String salt = PasswordHasher.generateSaltBase64();
        String hash = PasswordHasher.hashPassword(password, salt);
        ContentValues v = new ContentValues();
        v.put(DbContract.Users.COL_EMAIL, email);
        v.put(DbContract.Users.COL_PASSWORD_HASH, hash);
        v.put(DbContract.Users.COL_SALT, salt);
        v.put(DbContract.Users.COL_FULL_NAME, fullName);
        v.put(DbContract.Users.COL_PHONE, phone);
        v.put(DbContract.Users.COL_ROLE, role.name());
        v.put(DbContract.Users.COL_MUST_CHANGE_PASSWORD, mustChangePassword ? 1 : 0);
        v.put(DbContract.Users.COL_IS_ACTIVE, active ? 1 : 0);
        v.put(DbContract.Users.COL_CREATED_AT, DateUtils.now());
        return dbHelper.getWritableDatabase().insert(DbContract.Users.TABLE, null, v);
    }

    public boolean updateUserAdmin(long userId, String email, String fullName, String phone,
                                   UserRole role, boolean active) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Users.COL_EMAIL, email);
        v.put(DbContract.Users.COL_FULL_NAME, fullName);
        v.put(DbContract.Users.COL_PHONE, phone);
        v.put(DbContract.Users.COL_ROLE, role.name());
        v.put(DbContract.Users.COL_IS_ACTIVE, active ? 1 : 0);
        int rows = dbHelper.getWritableDatabase().update(DbContract.Users.TABLE, v,
                DbContract.Users.COL_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public List<User> findAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Users.TABLE, null, null, null, null, null,
                DbContract.Users.COL_FULL_NAME)) {
            while (c.moveToNext()) {
                list.add(mapUser(c));
            }
        }
        return list;
    }

    public boolean setActive(long userId, boolean active) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Users.COL_IS_ACTIVE, active ? 1 : 0);
        int rows = dbHelper.getWritableDatabase().update(DbContract.Users.TABLE, v,
                DbContract.Users.COL_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean updatePassword(long userId, String newPassword, boolean clearMustChange) {
        String salt = PasswordHasher.generateSaltBase64();
        String hash = PasswordHasher.hashPassword(newPassword, salt);
        ContentValues v = new ContentValues();
        v.put(DbContract.Users.COL_PASSWORD_HASH, hash);
        v.put(DbContract.Users.COL_SALT, salt);
        if (clearMustChange) {
            v.put(DbContract.Users.COL_MUST_CHANGE_PASSWORD, 0);
        }
        int rows = dbHelper.getWritableDatabase().update(DbContract.Users.TABLE, v,
                DbContract.Users.COL_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public boolean updateProfile(long userId, String fullName, String phone) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Users.COL_FULL_NAME, fullName);
        v.put(DbContract.Users.COL_PHONE, phone);
        int rows = dbHelper.getWritableDatabase().update(DbContract.Users.TABLE, v,
                DbContract.Users.COL_ID + "=?", new String[]{String.valueOf(userId)});
        return rows > 0;
    }

    public List<User> findByRole(UserRole role) {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Users.TABLE, null,
                DbContract.Users.COL_ROLE + "=?",
                new String[]{role.name()}, null, null, DbContract.Users.COL_FULL_NAME)) {
            while (c.moveToNext()) {
                list.add(mapUser(c));
            }
        }
        return list;
    }

    private User mapUser(Cursor c) {
        User u = new User();
        u.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Users.COL_ID)));
        u.setEmail(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_EMAIL)));
        u.setFullName(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_FULL_NAME)));
        u.setPhone(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_PHONE)));
        u.setRole(UserRole.fromString(c.getString(c.getColumnIndexOrThrow(DbContract.Users.COL_ROLE))));
        u.setMustChangePassword(c.getInt(c.getColumnIndexOrThrow(DbContract.Users.COL_MUST_CHANGE_PASSWORD)) == 1);
        int activeIdx = c.getColumnIndex(DbContract.Users.COL_IS_ACTIVE);
        u.setActive(activeIdx < 0 || c.getInt(activeIdx) == 1);
        u.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Users.COL_CREATED_AT)));
        return u;
    }
}