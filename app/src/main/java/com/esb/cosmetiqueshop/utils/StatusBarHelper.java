package com.esb.cosmetiqueshop.utils;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.esb.cosmetiqueshop.R;

/**
 * Décale logo / titre sous heure·wifi·batterie (sans chevauchement).
 * Header compact : le fond remonte derrière la status bar, le contenu reste en dessous.
 */
public final class StatusBarHelper {

    /** Marge sous heure / wifi / batterie pour éviter tout chevauchement */
    private static final int EXTRA_DP = 10;

    private StatusBarHelper() {
    }

    public static void applyHeader(@Nullable Activity activity, @Nullable View headerRoot) {
        applyHeader(activity, headerRoot, null);
    }

    public static void applyHeader(@Nullable Activity activity, @Nullable View headerRoot,
                                   @Nullable View bottomBar) {
        if (activity == null || headerRoot == null) {
            return;
        }

        Window window = activity.getWindow();
        // Edge-to-edge : fond header derrière la status bar
        WindowCompat.setDecorFitsSystemWindows(window, false);
        window.setStatusBarColor(Color.TRANSPARENT);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }

        headerRoot.setBackgroundResource(R.drawable.bg_gradient_blue);

        final int extraPx = dp(activity.getResources(), EXTRA_DP);
        final int fallback = statusBarHeightPx(activity.getResources());

        // Appliquer tout de suite une hauteur de secours
        View spacer = headerRoot.findViewById(R.id.statusBarSpacer);
        if (spacer != null) {
            setSpacerHeight(spacer, fallback + extraPx);
        }

        ViewCompat.setOnApplyWindowInsetsListener(headerRoot, (v, insets) -> {
            Insets status = insets.getInsets(WindowInsetsCompat.Type.statusBars());
            int top = status.top > 0 ? status.top : fallback;
            View s = v.findViewById(R.id.statusBarSpacer);
            if (s != null) {
                setSpacerHeight(s, top + extraPx);
            } else {
                v.setPadding(v.getPaddingLeft(), top + extraPx,
                        v.getPaddingRight(), v.getPaddingBottom());
            }
            if (bottomBar != null) {
                Insets nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
                bottomBar.setPadding(
                        bottomBar.getPaddingLeft(),
                        bottomBar.getPaddingTop(),
                        bottomBar.getPaddingRight(),
                        nav.bottom);
            }
            return insets;
        });

        // Plusieurs passes : certains OEM livrent les insets en retard
        ViewCompat.requestApplyInsets(headerRoot);
        headerRoot.post(() -> ViewCompat.requestApplyInsets(headerRoot));
        headerRoot.postDelayed(() -> ViewCompat.requestApplyInsets(headerRoot), 50);
    }

    private static int statusBarHeightPx(Resources res) {
        int id = res.getIdentifier("status_bar_height", "dimen", "android");
        if (id > 0) {
            return res.getDimensionPixelSize(id);
        }
        return dp(res, 28);
    }

    private static int dp(Resources res, int dp) {
        return Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, res.getDisplayMetrics()));
    }

    private static void setSpacerHeight(View spacer, int heightPx) {
        ViewGroup.LayoutParams lp = spacer.getLayoutParams();
        if (lp == null) {
            spacer.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, heightPx));
            return;
        }
        if (lp.height != heightPx) {
            lp.height = heightPx;
            spacer.setLayoutParams(lp);
            spacer.requestLayout();
        }
    }
}
