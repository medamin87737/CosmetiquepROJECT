package com.esb.cosmetiqueshop.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.databinding.ItemAdminCategoryBinding;
import com.esb.cosmetiqueshop.model.Category;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;

import java.util.ArrayList;
import java.util.List;

public class AdminCategoryAdapter extends RecyclerView.Adapter<AdminCategoryAdapter.Holder> {

    public interface Listener {
        void onClick(Category category);
    }

    private final List<Category> items = new ArrayList<>();
    private final Listener listener;

    public AdminCategoryAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<Category> categories) {
        items.clear();
        if (categories != null) {
            items.addAll(categories);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAdminCategoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class Holder extends RecyclerView.ViewHolder {
        private final ItemAdminCategoryBinding binding;

        Holder(ItemAdminCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Category category) {
            binding.txtAdminCategoryName.setText(category.getName());
            String desc = category.getDescription();
            binding.txtAdminCategoryDesc.setText(TextUtils.isEmpty(desc) ? "" : desc);
            ProductImageLoader.load(binding.imgAdminCategory, category.getImageUrl());
            binding.getRoot().setOnClickListener(v -> listener.onClick(category));
        }
    }
}
