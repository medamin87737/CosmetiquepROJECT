package com.esb.cosmetiqueshop.ui.order;

import android.content.Context;
import android.content.Intent;
import android.graphics.pdf.PdfDocument;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.OrderItemAdapter;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.ExportRepository;
import com.esb.cosmetiqueshop.data.repository.OrderRepository;
import com.esb.cosmetiqueshop.databinding.ActivityOrderDetailBinding;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.utils.OrderStatusUi;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

public class OrderDetailActivity extends AppCompatActivity {

    private static final String EXTRA_ORDER_ID = "order_id";

    private ActivityOrderDetailBinding binding;
    private long orderId;
    private OrderRepository orderRepository;
    private Order currentOrder;
    private OrderItemAdapter itemAdapter;

    public static Intent intent(Context context, long orderId) {
        Intent i = new Intent(context, OrderDetailActivity.class);
        i.putExtra(EXTRA_ORDER_ID, orderId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        orderId = getIntent().getLongExtra(EXTRA_ORDER_ID, -1);
        orderRepository = new OrderRepository(this);

        itemAdapter = new OrderItemAdapter();
        binding.recyclerItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerItems.setAdapter(itemAdapter);

        binding.btnReorder.setOnClickListener(v -> {
            if (currentOrder != null) {
                reorder(currentOrder);
            }
        });
        binding.btnPdf.setOnClickListener(v -> {
            if (currentOrder != null) {
                exportStub(currentOrder);
            }
        });
        binding.btnCancel.setOnClickListener(v -> cancelOrder());

        load();
    }

    private void load() {
        orderRepository.getOrder(orderId, new OrderRepository.OrderCallback() {
            @Override
            public void onSuccess(Order order) {
                currentOrder = order;
                bindOrder(order);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void bindOrder(Order order) {
        binding.txtOrderNumber.setText(getString(R.string.order_number, order.getId()));
        OrderStatusUi.applyBadge(binding.txtStatusBadge, order.getStatus());

        String address = order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "";
        String phone = order.getDeliveryPhone() != null ? order.getDeliveryPhone() : "";
        binding.txtAddress.setText(address + (phone.isEmpty() ? "" : "\n" + phone));

        itemAdapter.submit(order.getItems());
        binding.txtTotal.setText(getString(R.string.total_label) + " "
                + PriceUtils.formatMillimes(order.getTotalMillimes()));
        binding.btnCancel.setVisibility(
                order.getStatus() == OrderStatus.EN_ATTENTE ? View.VISIBLE : View.GONE);
    }

    private void cancelOrder() {
        orderRepository.updateStatus(orderId, OrderStatus.ANNULEE,
                new SessionManager(this).getUserId(), null,
                new OrderRepository.OrderCallback() {
                    @Override
                    public void onSuccess(Order o) {
                        Toast.makeText(OrderDetailActivity.this, R.string.status_annulee,
                                Toast.LENGTH_SHORT).show();
                        load();
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void reorder(Order order) {
        long userId = new SessionManager(this).getUserId();
        CartRepository cart = new CartRepository(this);
        for (OrderItem item : order.getItems()) {
            cart.setQuantity(userId, item.getProductId(), item.getQuantity(), ok -> {
            });
        }
        Toast.makeText(this, R.string.add_to_cart, Toast.LENGTH_SHORT).show();
        finish();
    }

    private void exportStub(Order order) {
        ExportRepository exportRepository = new ExportRepository(this);
        exportRepository.buildOrderInvoiceText(order.getId(), text -> {
            PdfDocument doc = new PdfDocument();
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(300, 400, 1).create();
            PdfDocument.Page page = doc.startPage(info);
            page.getCanvas().drawText(text, 10, 20, new android.graphics.Paint());
            doc.finishPage(page);
            doc.close();
            Toast.makeText(this, R.string.export_saved, Toast.LENGTH_SHORT).show();
        });
    }
}
