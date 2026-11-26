package com.esb.cosmetiqueshop.utils;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AlertDialog;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.ui.auth.LoginActivity;
import com.esb.cosmetiqueshop.ui.auth.RegisterActivity;

/**
 * Dialogue invité pour actions réservées aux clients connectés.
 */
public final class GuestAuthHelper {

    private GuestAuthHelper() {
    }

    public static void show(Context context, Runnable onDismissGuest) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.guest_dialog_title)
                .setMessage(R.string.guest_dialog_message)
                .setPositiveButton(R.string.btn_guest_login, (d, w) -> {
                    context.startActivity(new Intent(context, LoginActivity.class));
                })
                .setNeutralButton(R.string.btn_guest_register, (d, w) -> {
                    context.startActivity(new Intent(context, RegisterActivity.class));
                })
                .setNegativeButton(R.string.btn_continue_guest, (d, w) -> {
                    if (onDismissGuest != null) {
                        onDismissGuest.run();
                    }
                })
                .show();
    }

    public static boolean requireLogin(SessionManager session, Context context) {
        if (session.isLoggedIn()) {
            return true;
        }
        show(context, null);
        return false;
    }
}