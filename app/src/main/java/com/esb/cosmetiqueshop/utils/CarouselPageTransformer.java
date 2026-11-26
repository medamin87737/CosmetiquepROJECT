package com.esb.cosmetiqueshop.utils;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Transition dynamique type rotation 3D entre les pages produits.
 */
public class CarouselPageTransformer implements ViewPager2.PageTransformer {

    private static final float MIN_SCALE = 0.88f;
    private static final float MAX_ROTATION = 28f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        page.setCameraDistance(page.getResources().getDisplayMetrics().density * 16000f);

        if (position < -1f || position > 1f) {
            page.setAlpha(0f);
            return;
        }

        float abs = Math.abs(position);
        float scale = Math.max(MIN_SCALE, 1f - abs * 0.12f);
        page.setScaleX(scale);
        page.setScaleY(scale);
        page.setRotationY(-position * MAX_ROTATION);
        page.setTranslationX(-position * page.getWidth() * 0.08f);
        page.setAlpha(0.45f + (1f - abs) * 0.55f);
    }
}
