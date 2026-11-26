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

public class AdminProductAdapter extends RecyclerView.Adapter<AdminProductAdapter.Holder> {

    public interface Listener {
        void onProductClick(Product product);
    }

    private final List<Product> products = new ArrayList<>();
    private final Listener listener;

    public AdminProductAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Product> list) {
        products.clear();
        if (list != null) {
            products.addAll(list);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_admin_product, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Product p = products.get(position);
        holder.txtName.setText(p.getName());
        String cat = p.getCategoryName() != null ? p.getCategoryName() : "";
        String meta = holder.itemView.getContext().getString(
                R.string.admin_product_meta, cat, p.getStock());
        if (!p.isActive()) {
            meta += holder.itemView.getContext().getString(R.string.admin_product_hidden);
        }
        holder.txtMeta.setText(meta);
        holder.txtPrice.setText(PriceUtils.formatMillimes(p.getEffectivePriceMillimes()));
        ProductImageLoader.load(holder.img,
                p.getPrimaryImageUrl() != null ? p.getPrimaryImageUrl() : "");
        holder.itemView.setOnClickListener(v -> listener.onProductClick(p));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView txtName;
        final TextView txtMeta;
        final TextView txtPrice;

        Holder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgAdminProduct);
            txtName = itemView.findViewById(R.id.txtAdminProductName);
            txtMeta = itemView.findViewById(R.id.txtAdminProductMeta);
            txtPrice = itemView.findViewById(R.id.txtAdminProductPrice);
        }
    }
}
