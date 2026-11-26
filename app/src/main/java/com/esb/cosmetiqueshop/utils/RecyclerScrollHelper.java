package com.esb.cosmetiqueshop.utils;

import android.view.animation.DecelerateInterpolator;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Navigation Suivant / Précédent fiable (scroll pixel pour catégories).
 */
public final class RecyclerScrollHelper {

    private RecyclerScrollHelper() {
    }

    public static void scrollNext(RecyclerView recyclerView) {
        scrollHorizontally(recyclerView, 1);
    }

    public static void scrollPrevious(RecyclerView recyclerView) {
        scrollHorizontally(recyclerView, -1);
    }

    public static void scrollNext(ViewPager2 pager) {
        if (pager == null || pager.getAdapter() == null) {
            return;
        }
        int next = pager.getCurrentItem() + 1;
        if (next < pager.getAdapter().getItemCount()) {
            pager.setCurrentItem(next, true);
        }
    }

    public static void scrollPrevious(ViewPager2 pager) {
        if (pager == null) {
            return;
        }
        int prev = pager.getCurrentItem() - 1;
        if (prev >= 0) {
            pager.setCurrentItem(prev, true);
        }
    }

    /**
     * Fait défiler d'environ 80 % de la largeur visible — changement clairement visible.
     */
    private static void scrollHorizontally(RecyclerView recyclerView, int direction) {
        if (recyclerView == null || !(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
            return;
        }
        LinearLayoutManager lm = (LinearLayoutManager) recyclerView.getLayoutManager();
        int count = recyclerView.getAdapter() != null ? recyclerView.getAdapter().getItemCount() : 0;
        if (count <= 1) {
            return;
        }

        int first = lm.findFirstVisibleItemPosition();
        int last = lm.findLastCompletelyVisibleItemPosition();
        if (last == RecyclerView.NO_POSITION) {
            last = lm.findLastVisibleItemPosition();
        }
        if (first == RecyclerView.NO_POSITION) {
            return;
        }

        // Déjà au bout
        if (direction > 0 && last >= count - 1) {
            return;
        }
        if (direction < 0 && first <= 0) {
            return;
        }

        int width = recyclerView.getWidth();
        if (width <= 0) {
            width = recyclerView.getResources().getDisplayMetrics().widthPixels;
        }
        // Une « page » ≈ largeur utile du carrousel
        int dx = direction * Math.max((int) (width * 0.85f), 200);
        recyclerView.stopScroll();
        recyclerView.smoothScrollBy(dx, 0, new DecelerateInterpolator(), 320);
    }
}
