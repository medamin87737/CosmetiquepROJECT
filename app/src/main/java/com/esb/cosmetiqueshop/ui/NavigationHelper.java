package com.esb.cosmetiqueshop.ui;

import android.content.Context;
import android.content.Intent;

import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.model.UserRole;
import com.esb.cosmetiqueshop.ui.admin.AdminMainActivity;
import com.esb.cosmetiqueshop.ui.auth.ChangePasswordActivity;
import com.esb.cosmetiqueshop.ui.client.ClientMainActivity;
import com.esb.cosmetiqueshop.ui.publichome.PublicHomeActivity;

/**
 * Routage après authentification ou splash.
 */
public final class NavigationHelper {

    private NavigationHelper() {
    }

    public static Intent homeIntent(Context context, UserRole role, boolean mustChangePassword) {
        // mustChangePassword ignoré : pas de forçage de nouveau MDP à la connexion
        if (role == null) {
            return new Intent(context, PublicHomeActivity.class);
        }
        if (role == UserRole.ADMIN) {
            return new Intent(context, AdminMainActivity.class);
        }
        if (role == UserRole.CLIENT) {
            return new Intent(context, ClientMainActivity.class);
        }
        return new Intent(context, PublicHomeActivity.class);
    }

    public static void openAfterLogin(Context context, User user) {
        Intent intent = homeIntent(context, user.getRole(), false);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void openPublic(Context context) {
        Intent intent = new Intent(context, PublicHomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }
}