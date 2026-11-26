package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.NotificationItem;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.Holder> {

    public interface Listener {
        void onOpen(NotificationItem item);
    }

    private final List<NotificationItem> items;
    private final Listener listener;

    public NotificationAdapter(List<NotificationItem> items, Listener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notification, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        NotificationItem item = items.get(position);
        holder.txtNotifTitle.setText(item.getTitle());
        holder.txtNotifBody.setText(item.getBody());
        holder.txtNotifDate.setText(DateUtils.formatDisplay(item.getCreatedAt()));
        holder.itemView.setOnClickListener(v -> listener.onOpen(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtNotifTitle;
        final TextView txtNotifBody;
        final TextView txtNotifDate;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtNotifTitle = itemView.findViewById(R.id.txtNotifTitle);
            txtNotifBody = itemView.findViewById(R.id.txtNotifBody);
            txtNotifDate = itemView.findViewById(R.id.txtNotifDate);
        }
    }
}