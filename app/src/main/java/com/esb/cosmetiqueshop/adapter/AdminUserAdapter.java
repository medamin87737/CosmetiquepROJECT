package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.databinding.ItemAdminUserBinding;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.model.UserRole;

import java.util.ArrayList;
import java.util.List;

public class AdminUserAdapter extends RecyclerView.Adapter<AdminUserAdapter.Holder> {

    public interface Listener {
        void onEdit(User user);

        void onToggleActive(User user);
    }

    private final List<User> items = new ArrayList<>();
    private final Listener listener;

    public AdminUserAdapter(Listener listener) {
        this.listener = listener;
    }

    public void submit(List<User> users) {
        items.clear();
        if (users != null) {
            items.addAll(users);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(ItemAdminUserBinding.inflate(
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
        private final ItemAdminUserBinding binding;

        Holder(ItemAdminUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.txtUserName.setText(user.getFullName());
            binding.txtUserEmail.setText(user.getEmail());
            boolean isAdmin = user.getRole() == UserRole.ADMIN;
            String roleLabel = itemView.getContext().getString(
                    isAdmin ? R.string.role_admin : R.string.role_client);
            binding.txtUserRoleBadge.setText(roleLabel);
            if (isAdmin) {
                binding.txtUserRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_admin);
                binding.txtUserRoleBadge.setTextColor(
                        itemView.getContext().getColor(R.color.blanc));
            } else {
                binding.txtUserRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_client);
                binding.txtUserRoleBadge.setTextColor(
                        itemView.getContext().getColor(R.color.bordeaux));
            }
            String status = user.isActive()
                    ? itemView.getContext().getString(R.string.admin_user_active)
                    : itemView.getContext().getString(R.string.client_inactive);
            String phone = user.getPhone() != null ? user.getPhone() : "";
            binding.txtUserMeta.setText(phone.isEmpty() ? status : phone + " · " + status);
            binding.btnToggleUser.setText(user.isActive()
                    ? R.string.btn_deactivate : R.string.btn_activate);
            binding.btnEditUser.setOnClickListener(v -> listener.onEdit(user));
            binding.btnToggleUser.setOnClickListener(v -> listener.onToggleActive(user));
        }
    }
}
