package com.esb.cosmetiqueshop.utils;

import android.app.Activity;
import android.view.View;
import android.view.ViewParent;

import androidx.annotation.Nullable;

import com.esb.cosmetiqueshop.R;
import com.google.android.material.appbar.MaterialToolbar;

/**
 * Configure une flèche retour blanche et décale le header sous la status bar.
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
        applySafeHeader(activity, toolbar);
    }

    /** Cherche le conteneur avec statusBarSpacer et applique le décalage. */
    private static void applySafeHeader(Activity activity, MaterialToolbar toolbar) {
        View header = activity.findViewById(R.id.headerToolbarRoot);
        if (header == null) {
            ViewParent parent = toolbar.getParent();
            if (parent instanceof View) {
                View p = (View) parent;
                if (p.findViewById(R.id.statusBarSpacer) != null) {
                    header = p;
                }
            }
        }
        if (header != null) {
            StatusBarHelper.applyHeader(activity, header);
        }
    }
}
