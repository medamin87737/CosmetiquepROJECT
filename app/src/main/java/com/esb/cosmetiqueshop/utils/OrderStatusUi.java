package com.esb.cosmetiqueshop.utils;

import android.content.Context;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.OrderStatus;

public final class OrderStatusUi {

    private OrderStatusUi() {
    }

    public static String label(Context context, OrderStatus status) {
        if (status == null) {
            return context.getString(R.string.status_en_attente);
        }
        switch (status) {
            case CONFIRMEE:
                return context.getString(R.string.status_confirmee);
            case EN_PREPARATION:
                return context.getString(R.string.status_en_preparation);
            case EN_LIVRAISON:
                return context.getString(R.string.status_en_livraison);
            case LIVREE:
                return context.getString(R.string.status_livree);
            case ANNULEE:
                return context.getString(R.string.status_annulee);
            case ECHEC_LIVRAISON:
                return context.getString(R.string.status_echec);
            case EN_ATTENTE:
            default:
                return context.getString(R.string.status_en_attente);
        }
    }

    /** Applique le style badge pro selon le statut. */
    public static void applyBadge(TextView badge, OrderStatus status) {
        if (badge == null) {
            return;
        }
        Context context = badge.getContext();
        badge.setText(label(context, status));
        int bg;
        int textColor;
        if (status == null) {
            status = OrderStatus.EN_ATTENTE;
        }
        switch (status) {
            case EN_PREPARATION:
            case CONFIRMEE:
            case EN_LIVRAISON:
                bg = R.drawable.bg_status_badge_preparation;
                textColor = R.color.bordeaux;
                break;
            case LIVREE:
                bg = R.drawable.bg_status_badge_livree;
                textColor = R.color.blanc;
                break;
            case ANNULEE:
                bg = R.drawable.bg_status_badge_annulee;
                textColor = R.color.texte_secondaire;
                break;
            case ECHEC_LIVRAISON:
                bg = R.drawable.bg_status_badge_echec;
                textColor = R.color.blanc;
                break;
            case EN_ATTENTE:
            default:
                bg = R.drawable.bg_status_badge_attente;
                textColor = R.color.bordeaux_fonce;
                break;
        }
        badge.setBackgroundResource(bg);
        badge.setTextColor(ContextCompat.getColor(context, textColor));
    }
}
