package com.esb.cosmetiqueshop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.esb.cosmetiqueshop.model.UserRole;

/**
 * Session utilisateur (id + rôle) via SharedPreferences.
 */
public class SessionManager {

    private static final String PREFS_NAME = "cosmetique_session";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ROLE = "role";
    private static final String KEY_EMAIL = "email";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(long userId, UserRole role, String email) {
        prefs.edit()
                .putLong(KEY_USER_ID, userId)
                .putString(KEY_ROLE, role.name())
                .putString(KEY_EMAIL, email)
                .apply();
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }

    public boolean isLoggedIn() {
        return prefs.getLong(KEY_USER_ID, -1L) > 0;
    }

    public long getUserId() {
        return prefs.getLong(KEY_USER_ID, -1L);
    }

    public UserRole getRole() {
        return UserRole.fromString(prefs.getString(KEY_ROLE, null));
    }

    public String getEmail() {
        return prefs.getString(KEY_EMAIL, "");
    }
}