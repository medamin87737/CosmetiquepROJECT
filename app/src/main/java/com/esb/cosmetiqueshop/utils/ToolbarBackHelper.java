package com.esb.cosmetiqueshop.utils;

import android.app.Activity;

import androidx.annotation.Nullable;

import com.esb.cosmetiqueshop.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Configure une flèche retour blanche sur les toolbars.
 */
public final class ToolbarBackHelper {

    private ToolbarBackHelper() {
    }

    public static void enable(@Nullable MaterialToolbar toolbar, Activity activity) {
        enableFinish(toolbar, activity);
    }

    public static void enableFinish(@Nullable MaterialToolbar toolbar, Activity activity) {
        if (toolbar == null || activity == null) {
            return;
        }
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationIconTint(activity.getResources().getColor(R.color.blanc, activity.getTheme()));
        toolbar.setNavigationOnClickListener(v -> activity.finish());
    }
}
