package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;

import java.util.ArrayList;
import java.util.List;

/** Lignes produit d’une commande (image, prix, quantité). */
public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.Holder> {

    private final List<OrderItem> items = new ArrayList<>();

    public void submit(List<OrderItem> data) {
        items.clear();
        if (data != null) {
            items.addAll(data);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order_line, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        OrderItem item = items.get(position);
        holder.txtName.setText(item.getProductName());
        holder.txtQty.setText(holder.itemView.getContext()
                .getString(R.string.order_item_qty, item.getQuantity()));
        holder.txtUnit.setText(holder.itemView.getContext()
                .getString(R.string.order_item_unit_price,
                        PriceUtils.formatMillimes(item.getUnitPriceMillimes())));
        holder.txtTotal.setText(PriceUtils.formatMillimes(item.getLineTotalMillimes()));
        ProductImageLoader.load(holder.img, item.getProductImageUrl());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView txtName;
        final TextView txtQty;
        final TextView txtUnit;
        final TextView txtTotal;

        Holder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgOrderProduct);
            txtName = itemView.findViewById(R.id.txtOrderItemName);
            txtQty = itemView.findViewById(R.id.txtOrderItemQty);
            txtUnit = itemView.findViewById(R.id.txtOrderItemUnit);
            txtTotal = itemView.findViewById(R.id.txtOrderItemTotal);
        }
    }
}
