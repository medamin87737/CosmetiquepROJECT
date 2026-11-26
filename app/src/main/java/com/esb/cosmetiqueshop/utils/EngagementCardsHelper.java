package com.esb.cosmetiqueshop.utils;

import android.content.Context;

import androidx.annotation.StringRes;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.databinding.ItemEngagementCardBinding;

/**
 * Configure les cartes « Nos engagements » (média + textes).
 */
public final class EngagementCardsHelper {

    private EngagementCardsHelper() {
    }

    public static void bindAll(Context context,
                               ItemEngagementCardBinding quality,
                               ItemEngagementCardBinding delivery,
                               ItemEngagementCardBinding support) {
        bindOne(context, quality,
                R.string.engagement_quality, R.string.engagement_quality_desc,
                "video_quality", "img_cat_04");
        bindOne(context, delivery,
                R.string.engagement_delivery, R.string.engagement_delivery_desc,
                "video_delivery", "img_prod_20");
        bindOne(context, support,
                R.string.engagement_support, R.string.engagement_support_desc,
                "video_support", "img_cat_02");
    }

    public static void releaseAll(ItemEngagementCardBinding quality,
                                  ItemEngagementCardBinding delivery,
                                  ItemEngagementCardBinding support) {
        if (quality != null) {
            RawVideoHelper.release(quality.engagementPlayer);
        }
        if (delivery != null) {
            RawVideoHelper.release(delivery.engagementPlayer);
        }
        if (support != null) {
            RawVideoHelper.release(support.engagementPlayer);
        }
    }

    private static void bindOne(Context context,
                                ItemEngagementCardBinding card,
                                @StringRes int titleRes,
                                @StringRes int descRes,
                                String rawName,
                                String imageDrawable) {
        if (card == null) {
            return;
        }
        card.txtEngagementTitle.setText(titleRes);
        card.txtEngagementDesc.setText(descRes);
        RawVideoHelper.bindEngagementCard(
                card.engagementPlayer,
                card.engagementPlaceholder,
                context,
                rawName,
                imageDrawable);
    }
}
