package com.esb.cosmetiqueshop.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.OrderStatusUi;
import com.esb.cosmetiqueshop.utils.PriceUtils;

import java.util.List;

/**
 * Cartes « colis livré » pour l’admin (détails client + articles).
 */
public class DeliveredPackageAdapter extends RecyclerView.Adapter<DeliveredPackageAdapter.Holder> {

    public interface Listener {
        void onClick(Order order);
    }

    private final List<Order> orders;
    private final Listener listener;

    public DeliveredPackageAdapter(List<Order> orders, Listener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_delivered_package, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Order order = orders.get(position);
        holder.txtOrder.setText(holder.itemView.getContext()
                .getString(R.string.order_number, order.getId()));
        holder.txtDate.setText(holder.itemView.getContext()
                .getString(R.string.delivered_date_label) + " : "
                + DateUtils.formatDisplay(order.getUpdatedAt()));
        holder.txtTotal.setText(PriceUtils.formatMillimes(order.getTotalMillimes()));
        OrderStatusUi.applyBadge(holder.txtStatusBadge,
                order.getStatus() != null ? order.getStatus() : OrderStatus.LIVREE);

        String client = order.getClientName() != null && !order.getClientName().isEmpty()
                ? order.getClientName()
                : ("#" + order.getUserId());
        holder.txtClient.setText(holder.itemView.getContext()
                .getString(R.string.delivered_client_label) + " : " + client);

        String address = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "—";
        holder.txtAddress.setText(holder.itemView.getContext()
                .getString(R.string.delivered_address_label) + " : " + address);

        String phone = order.getDeliveryPhone() != null ? order.getDeliveryPhone() : "—";
        holder.txtPhone.setText(holder.itemView.getContext()
                .getString(R.string.delivered_phone_label) + " : " + phone);

        int qty = 0;
        StringBuilder items = new StringBuilder();
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                qty += item.getQuantity();
                if (items.length() > 0) {
                    items.append(" · ");
                }
                items.append(item.getProductName()).append(" ×").append(item.getQuantity());
            }
        }
        holder.txtItems.setText(holder.itemView.getContext()
                .getString(R.string.order_articles_count, qty)
                + (items.length() > 0 ? "\n" + items : ""));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtOrder;
        final TextView txtDate;
        final TextView txtTotal;
        final TextView txtStatusBadge;
        final TextView txtClient;
        final TextView txtAddress;
        final TextView txtPhone;
        final TextView txtItems;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtOrder = itemView.findViewById(R.id.txtPackageOrder);
            txtDate = itemView.findViewById(R.id.txtPackageDate);
            txtTotal = itemView.findViewById(R.id.txtPackageTotal);
            txtStatusBadge = itemView.findViewById(R.id.txtPackageStatusBadge);
            txtClient = itemView.findViewById(R.id.txtPackageClient);
            txtAddress = itemView.findViewById(R.id.txtPackageAddress);
            txtPhone = itemView.findViewById(R.id.txtPackagePhone);
            txtItems = itemView.findViewById(R.id.txtPackageItems);
        }
    }
}
