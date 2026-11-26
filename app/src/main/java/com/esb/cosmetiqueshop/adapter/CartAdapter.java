package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.CartItem;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;

import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    public interface CartListener {
        void onIncrease(CartItem item);

        void onDecrease(CartItem item);

        void onRemove(CartItem item);
    }

    private final List<CartItem> items;
    private final CartListener listener;

    public CartAdapter(List<CartItem> items, CartListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = items.get(position);
        Product product = item.getProduct();
        if (product == null) {
            holder.txtCartName.setText(R.string.state_empty);
            holder.txtCartCategory.setText("");
            holder.txtCartUnitPrice.setText("");
            holder.txtCartQty.setText(String.valueOf(item.getQuantity()));
            holder.txtCartPrice.setText("");
            return;
        }

        holder.txtCartName.setText(product.getName());
        String category = product.getCategoryName() != null ? product.getCategoryName() : "";
        holder.txtCartCategory.setText(category);
        holder.txtCartUnitPrice.setText(PriceUtils.formatMillimes(product.getEffectivePriceMillimes()));
        holder.txtCartQty.setText(String.valueOf(item.getQuantity()));
        holder.txtCartPrice.setText(PriceUtils.formatMillimes(item.getLineTotalMillimes()));
        ProductImageLoader.load(holder.imgCartProduct,
                product.getPrimaryImageUrl() != null ? product.getPrimaryImageUrl() : "");

        holder.btnCartPlus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onIncrease(item);
            }
        });
        holder.btnCartMinus.setOnClickListener(v -> {
            if (listener != null) {
                listener.onDecrease(item);
            }
        });
        holder.btnCartRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        final ImageView imgCartProduct;
        final TextView txtCartName;
        final TextView txtCartCategory;
        final TextView txtCartUnitPrice;
        final TextView txtCartQty;
        final TextView txtCartPrice;
        final ImageButton btnCartPlus;
        final ImageButton btnCartMinus;
        final ImageButton btnCartRemove;

        CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCartProduct = itemView.findViewById(R.id.imgCartProduct);
            txtCartName = itemView.findViewById(R.id.txtCartName);
            txtCartCategory = itemView.findViewById(R.id.txtCartCategory);
            txtCartUnitPrice = itemView.findViewById(R.id.txtCartUnitPrice);
            txtCartQty = itemView.findViewById(R.id.txtCartQty);
            txtCartPrice = itemView.findViewById(R.id.txtCartPrice);
            btnCartPlus = itemView.findViewById(R.id.btnCartPlus);
            btnCartMinus = itemView.findViewById(R.id.btnCartMinus);
            btnCartRemove = itemView.findViewById(R.id.btnCartRemove);
        }
    }
}
