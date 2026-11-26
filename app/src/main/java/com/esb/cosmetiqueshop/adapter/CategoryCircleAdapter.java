package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Category;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;

import java.util.List;

public class CategoryCircleAdapter extends RecyclerView.Adapter<CategoryCircleAdapter.Holder> {

    public interface Listener {
        void onCategoryClick(Category category);
    }

    private final List<Category> categories;
    private final Listener listener;

    public CategoryCircleAdapter(List<Category> categories, Listener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_category_circle, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Category c = categories.get(position);
        holder.txtCategoryName.setText(c.getName());
        ProductImageLoader.load(holder.imgCategory, c.getImageUrl());
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(c));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView imgCategory;
        final TextView txtCategoryName;

        Holder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imgCategory);
            txtCategoryName = itemView.findViewById(R.id.txtCategoryName);
        }
    }
}