package com.esb.cosmetiqueshop.adapter;

import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public interface OnAddToCartListener {
        void onAddToCart(Product product);
    }

    private final List<Product> products;
    private final OnProductClickListener listener;
    @Nullable
    private final OnAddToCartListener addToCartListener;
    private final boolean horizontal;
    private int fixedCardWidthPx;
    private int gapPx;

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this(products, listener, null, false);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener, boolean horizontal) {
        this(products, listener, null, horizontal);
    }

    public ProductAdapter(List<Product> products,
                          OnProductClickListener listener,
                          @Nullable OnAddToCartListener addToCartListener,
                          boolean horizontal) {
        this.products = products;
        this.listener = listener;
        this.addToCartListener = addToCartListener;
        this.horizontal = horizontal;
    }

    /** 2 cartes de même taille + espace entre elles (remplit exactement la largeur). */
    public void setCarouselWidth(int recyclerWidthPx, int gapPx) {
        this.gapPx = Math.max(0, gapPx);
        if (recyclerWidthPx > gapPx) {
            int usable = recyclerWidthPx - this.gapPx;
            this.fixedCardWidthPx = usable / 2;
        }
        notifyDataSetChanged();
    }

    public void setFixedCardWidthPx(int widthPx) {
        setCarouselWidth(widthPx * 2, 0);
    }

    /** Espace horizontal entre cartes (carrousel). */
    public static class GapDecoration extends RecyclerView.ItemDecoration {
        private final int gapPx;

        public GapDecoration(int gapPx) {
            this.gapPx = gapPx;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int pos = parent.getChildAdapterPosition(view);
            if (pos == RecyclerView.NO_POSITION) {
                return;
            }
            if (pos > 0) {
                outRect.left = gapPx;
            }
        }
    }

    /** Espacement grille 2 colonnes. */
    public static class GridSpacingDecoration extends RecyclerView.ItemDecoration {
        private final int spanCount;
        private final int spacingPx;

        public GridSpacingDecoration(int spanCount, int spacingPx) {
            this.spanCount = spanCount;
            this.spacingPx = spacingPx;
        }

        @Override
        public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                                   @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            if (position == RecyclerView.NO_POSITION) {
                return;
            }
            int column = position % spanCount;
            outRect.left = spacingPx - column * spacingPx / spanCount;
            outRect.right = (column + 1) * spacingPx / spanCount;
            if (position >= spanCount) {
                outRect.top = spacingPx;
            }
        }
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        if (horizontal) {
            int height = parent.getResources().getDimensionPixelSize(R.dimen.product_card_height);
            int width = fixedCardWidthPx > 0
                    ? fixedCardWidthPx
                    : parent.getResources().getDimensionPixelSize(R.dimen.product_card_width);
            view.setLayoutParams(new RecyclerView.LayoutParams(width, height));
        }
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        if (horizontal && fixedCardWidthPx > 0) {
            ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
            if (lp != null) {
                lp.width = fixedCardWidthPx;
                lp.height = holder.itemView.getResources()
                        .getDimensionPixelSize(R.dimen.product_card_height);
                holder.itemView.setLayoutParams(lp);
            }
        }
        final Product product = products.get(position);
        holder.txtProductName.setText(product.getName());
        String category = product.getCategoryName() != null ? product.getCategoryName() : "";
        holder.txtProductCategory.setText(category);
        holder.txtProductPrice.setText(PriceUtils.formatMillimes(product.getEffectivePriceMillimes()));
        if (holder.txtPromoBadge != null) {
            holder.txtPromoBadge.setVisibility(product.hasPromo() ? View.VISIBLE : View.GONE);
        }
        ProductImageLoader.load(holder.imgProduct,
                product.getPrimaryImageUrl() != null ? product.getPrimaryImageUrl() : "");
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));

        if (holder.btnAddCart != null) {
            if (addToCartListener != null) {
                holder.btnAddCart.setVisibility(View.VISIBLE);
                holder.btnAddCart.setOnClickListener(v -> addToCartListener.onAddToCart(product));
            } else {
                holder.btnAddCart.setVisibility(View.GONE);
                holder.btnAddCart.setOnClickListener(null);
            }
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgProduct;
        final TextView txtProductName;
        final TextView txtProductCategory;
        final TextView txtProductPrice;
        final TextView txtPromoBadge;
        final ImageButton btnAddCart;

        ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            txtProductCategory = itemView.findViewById(R.id.txtProductCategory);
            txtProductPrice = itemView.findViewById(R.id.txtProductPrice);
            txtPromoBadge = itemView.findViewById(R.id.txtPromoBadge);
            btnAddCart = itemView.findViewById(R.id.btnAddCart);
        }
    }
}
