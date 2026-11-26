package com.esb.cosmetiqueshop.utils;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.esb.cosmetiqueshop.R;

import java.io.File;

/**
 * Charge images locales (drawable://), fichiers, content URI ou http(s).
 */
public final class ProductImageLoader {

    private ProductImageLoader() {
    }

    public static void load(ImageView target, String url) {
        if (TextUtils.isEmpty(url) || url.startsWith("placeholder://")) {
            target.setImageResource(R.drawable.product_placeholder);
            return;
        }
        Context context = target.getContext();
        if (url.startsWith("drawable://")) {
            String name = url.substring("drawable://".length());
            int resId = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
            if (resId != 0) {
                Glide.with(target).load(resId).centerCrop().into(target);
            } else {
                target.setImageResource(R.drawable.product_placeholder);
            }
            return;
        }
        if (url.startsWith("http://") || url.startsWith("https://")) {
            Glide.with(target)
                    .load(url)
                    .placeholder(R.drawable.product_placeholder)
                    .error(R.drawable.product_placeholder)
                    .centerCrop()
                    .into(target);
            return;
        }
        File file = new File(url);
        if (file.exists()) {
            Glide.with(target)
                    .load(file)
                    .placeholder(R.drawable.product_placeholder)
                    .error(R.drawable.product_placeholder)
                    .centerCrop()
                    .into(target);
            return;
        }
        if (url.startsWith("content://") || url.startsWith("file://")) {
            Glide.with(target)
                    .load(Uri.parse(url))
                    .placeholder(R.drawable.product_placeholder)
                    .error(R.drawable.product_placeholder)
                    .centerCrop()
                    .into(target);
            return;
        }
        // nom de drawable brut
        int resId = context.getResources().getIdentifier(url, "drawable", context.getPackageName());
        if (resId != 0) {
            Glide.with(target).load(resId).centerCrop().into(target);
        } else {
            target.setImageResource(R.drawable.product_placeholder);
        }
    }
}
