package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;

import java.util.ArrayList;
import java.util.List;

/**
 * Une page = 2 produits. Pour ViewPager2 : MATCH_PARENT obligatoire.
 */
public class ProductPageAdapter extends RecyclerView.Adapter<ProductPageAdapter.PageHolder> {

    public interface Listener {
        void onProductClick(Product product);
    }

    private final List<Product> products;
    private final Listener listener;

    public ProductPageAdapter(List<Product> products, Listener listener) {
        this.products = products != null ? products : new ArrayList<>();
        this.listener = listener;
    }

    /** Conservé pour compatibilité ; ViewPager2 impose MATCH_PARENT. */
    public void setPageWidth(int widthPx) {
        // no-op : ViewPager2 refuse toute largeur autre que match_parent
    }

    @Override
    public int getItemCount() {
        return products.isEmpty() ? 0 : (products.size() + 1) / 2;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View page = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_page, parent, false);
        page.setLayoutParams(new RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return new PageHolder(page);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder holder, int position) {
        ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
        if (lp != null && (lp.width != ViewGroup.LayoutParams.MATCH_PARENT
                || lp.height != ViewGroup.LayoutParams.MATCH_PARENT)) {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
            holder.itemView.setLayoutParams(lp);
        }

        int leftIndex = position * 2;
        int rightIndex = leftIndex + 1;
        bindCard(holder.left, products.get(leftIndex));
        if (rightIndex < products.size()) {
            holder.rightRoot.setVisibility(View.VISIBLE);
            bindCard(holder.right, products.get(rightIndex));
        } else {
            holder.rightRoot.setVisibility(View.INVISIBLE);
            holder.rightRoot.setClickable(false);
        }
    }

    private void bindCard(CardViews card, Product product) {
        if (card == null || card.root == null || product == null) {
            return;
        }
        card.name.setText(product.getName());
        card.category.setText(product.getCategoryName() != null ? product.getCategoryName() : "");
        card.price.setText(PriceUtils.formatMillimes(product.getEffectivePriceMillimes()));
        if (card.promo != null) {
            card.promo.setVisibility(product.hasPromo() ? View.VISIBLE : View.GONE);
        }
        ProductImageLoader.load(card.image,
                product.getPrimaryImageUrl() != null ? product.getPrimaryImageUrl() : "");
        card.root.setOnClickListener(v -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    static class CardViews {
        final View root;
        final ImageView image;
        final TextView name;
        final TextView category;
        final TextView price;
        final TextView promo;

        CardViews(View root) {
            this.root = root;
            image = root.findViewById(R.id.imgProduct);
            name = root.findViewById(R.id.txtProductName);
            category = root.findViewById(R.id.txtProductCategory);
            price = root.findViewById(R.id.txtProductPrice);
            promo = root.findViewById(R.id.txtPromoBadge);
        }
    }

    static class PageHolder extends RecyclerView.ViewHolder {
        final CardViews left;
        final CardViews right;
        final View rightRoot;

        PageHolder(@NonNull View itemView) {
            super(itemView);
            View leftRoot = itemView.findViewById(R.id.cardLeft);
            rightRoot = itemView.findViewById(R.id.cardRight);
            left = new CardViews(leftRoot);
            right = new CardViews(rightRoot);
        }
    }
}
