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
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.OrderStatusUi;
import com.esb.cosmetiqueshop.utils.PriceUtils;

import java.util.List;

public class OrderCardAdapter extends RecyclerView.Adapter<OrderCardAdapter.Holder> {

    public interface Listener {
        void onOrderClick(Order order);
    }

    private final List<Order> orders;
    private final Listener listener;
    private final boolean showClient;

    public OrderCardAdapter(List<Order> orders, Listener listener) {
        this(orders, listener, false);
    }

    public OrderCardAdapter(List<Order> orders, Listener listener, boolean showClient) {
        this.orders = orders;
        this.listener = listener;
        this.showClient = showClient;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_order_card, parent, false);
        return new Holder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Order order = orders.get(position);
        holder.txtOrderId.setText(holder.itemView.getContext()
                .getString(R.string.order_number, order.getId()));
        OrderStatusUi.applyBadge(holder.txtStatusBadge, order.getStatus());
        holder.txtOrderTotal.setText(PriceUtils.formatMillimes(order.getTotalMillimes()));
        holder.txtOrderDate.setText(DateUtils.formatDisplay(order.getCreatedAt()));

        int qty = 0;
        if (order.getItems() != null) {
            for (OrderItem item : order.getItems()) {
                qty += item.getQuantity();
            }
        }
        holder.txtItemsSummary.setText(holder.itemView.getContext()
                .getString(R.string.order_articles_count, qty));

        if (showClient && order.getClientName() != null && !order.getClientName().isEmpty()) {
            holder.txtClient.setVisibility(View.VISIBLE);
            holder.txtClient.setText(holder.itemView.getContext()
                    .getString(R.string.delivered_client_label) + " : " + order.getClientName());
        } else {
            holder.txtClient.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onOrderClick(order);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView txtOrderId;
        final TextView txtStatusBadge;
        final TextView txtOrderTotal;
        final TextView txtOrderDate;
        final TextView txtItemsSummary;
        final TextView txtClient;

        Holder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtStatusBadge = itemView.findViewById(R.id.txtOrderStatusBadge);
            txtOrderTotal = itemView.findViewById(R.id.txtOrderTotal);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtItemsSummary = itemView.findViewById(R.id.txtOrderItemsSummary);
            txtClient = itemView.findViewById(R.id.txtOrderClient);
        }
    }
}
