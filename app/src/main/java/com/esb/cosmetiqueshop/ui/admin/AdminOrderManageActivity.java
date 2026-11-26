package com.esb.cosmetiqueshop.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.OrderItemAdapter;
import com.esb.cosmetiqueshop.adapter.SimpleTextAdapter;
import com.esb.cosmetiqueshop.data.repository.OrderRepository;
import com.esb.cosmetiqueshop.databinding.ActivityAdminOrderManageBinding;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.model.OrderStatusHistory;
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.OrderStatusUi;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

/**
 * Admin — détail commande (produits + images) et choix de statut
 * (préparation / livrée / annulée / échec livraison).
 */
public class AdminOrderManageActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_ID = "order_id";

    private ActivityAdminOrderManageBinding binding;
    private OrderRepository orderRepository;
    private Order currentOrder;
    private OrderItemAdapter itemAdapter;
    private final OrderStatus[] statusChoices = OrderStatus.adminChoices();
    private String[] statusLabels;

    public static Intent intent(Context context, long orderId) {
        Intent i = new Intent(context, AdminOrderManageActivity.class);
        i.putExtra(EXTRA_ORDER_ID, orderId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminOrderManageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        long orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        orderRepository = new OrderRepository(this);

        statusLabels = new String[statusChoices.length];
        for (int i = 0; i < statusChoices.length; i++) {
            statusLabels[i] = OrderStatusUi.label(this, statusChoices[i]);
        }

        itemAdapter = new OrderItemAdapter();
        binding.recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItems.setAdapter(itemAdapter);

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, statusLabels);
        binding.spinnerStatus.setAdapter(statusAdapter);

        orderRepository.getOrder(orderId, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                currentOrder = order;
                bindOrder(order);
                loadTimeline(orderId);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AdminOrderManageActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        binding.btnApplyStatus.setOnClickListener(v -> applyStatus());
    }

    private void bindOrder(Order order) {
        binding.txtOrderNumber.setText(getString(R.string.order_number, order.getId()));
        OrderStatusUi.applyBadge(binding.txtStatusBadge, order.getStatus());

        String client = order.getClientName() != null && !order.getClientName().isEmpty()
                ? order.getClientName()
                : ("#" + order.getUserId());
        binding.txtClient.setText(getString(R.string.delivered_client_label) + " : " + client);

        String address = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "—";
        binding.txtAddress.setText(getString(R.string.delivered_address_label) + " : " + address);

        String phone = order.getDeliveryPhone() != null ? order.getDeliveryPhone() : "—";
        binding.txtPhone.setText(getString(R.string.delivered_phone_label) + " : " + phone);

        binding.txtDate.setText(DateUtils.formatDisplay(order.getCreatedAt()));
        binding.txtTotal.setText(getString(R.string.total_label) + " "
                + PriceUtils.formatMillimes(order.getTotalMillimes()));

        itemAdapter.submit(order.getItems());

        // Pré-sélection du statut admin le plus proche
        int selectIdx = indexForStatus(order.getStatus());
        binding.spinnerStatus.setText(statusLabels[selectIdx], false);
    }

    private int indexForStatus(OrderStatus status) {
        for (int i = 0; i < statusChoices.length; i++) {
            if (statusChoices[i] == status) {
                return i;
            }
        }
        // En attente / confirmée / en livraison → préparation par défaut
        return 0;
    }

    private int selectedStatusIndex() {
        String current = binding.spinnerStatus.getText() != null
                ? binding.spinnerStatus.getText().toString() : "";
        for (int i = 0; i < statusLabels.length; i++) {
            if (statusLabels[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    private void loadTimeline(long orderId) {
        orderRepository.getOrderHistory(orderId, history -> {
            binding.recyclerTimeline.setLayoutManager(new LinearLayoutManager(this));
            binding.recyclerTimeline.setAdapter(new SimpleTextAdapter<>(history, this::timelineLine, h -> {
            }));
        });
    }

    private String timelineLine(OrderStatusHistory h) {
        return OrderStatusUi.label(this, h.getNewStatus()) + " — " + DateUtils.formatDisplay(h.getCreatedAt());
    }

    private void applyStatus() {
        if (currentOrder == null) {
            return;
        }
        OrderStatus target = statusChoices[selectedStatusIndex()];
        if (target == currentOrder.getStatus()) {
            Toast.makeText(this, R.string.status_unchanged, Toast.LENGTH_SHORT).show();
            return;
        }
        binding.btnApplyStatus.setEnabled(false);
        orderRepository.updateStatusAsAdmin(currentOrder.getId(), target,
                new SessionManager(this).getUserId(), null, new OrderRepository.OrderCallback() {
                    @Override
                    public void onSuccess(Order order) {
                        Toast.makeText(AdminOrderManageActivity.this,
                                R.string.status_updated, Toast.LENGTH_SHORT).show();
                        currentOrder = order;
                        bindOrder(order);
                        loadTimeline(order.getId());
                        binding.btnApplyStatus.setEnabled(true);
                    }

                    @Override
                    public void onFailure(String message) {
                        binding.btnApplyStatus.setEnabled(true);
                        Toast.makeText(AdminOrderManageActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
