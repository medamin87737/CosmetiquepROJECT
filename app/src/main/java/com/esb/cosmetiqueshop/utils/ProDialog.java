package com.esb.cosmetiqueshop.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import com.esb.cosmetiqueshop.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Popup professionnelle Material (remplace les Toast système avec icône).
 */
public final class ProDialog {

    private ProDialog() {
    }

    public static void error(@NonNull Context context, @Nullable String message) {
        String msg = message != null && !message.trim().isEmpty()
                ? message
                : context.getString(R.string.state_error);
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_error_title)
                .setMessage(msg)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public static void error(@NonNull Context context, @StringRes int messageRes) {
        error(context, context.getString(messageRes));
    }

    public static void info(@NonNull Context context, @StringRes int titleRes, @Nullable String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(titleRes)
                .setMessage(message != null ? message : "")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    public static void success(@NonNull Context context, @Nullable String message) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_success_title)
                .setMessage(message != null ? message : "")
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
