package com.esb.cosmetiqueshop.ui.checkout;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.data.repository.OrderRepository;
import com.esb.cosmetiqueshop.databinding.ActivityCheckoutBinding;
import com.esb.cosmetiqueshop.databinding.CheckoutStepAddressBinding;
import com.esb.cosmetiqueshop.databinding.CheckoutStepConfirmBinding;
import com.esb.cosmetiqueshop.databinding.CheckoutStepDeliveryBinding;
import com.esb.cosmetiqueshop.model.CartItem;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.ui.client.ClientMainActivity;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {

    private ActivityCheckoutBinding binding;
    private String address;
    private String phone;
    private boolean express;
    private int subtotal;
    private int delivery;
    private final List<CartItem> cartItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCheckoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        com.esb.cosmetiqueshop.utils.ToolbarBackHelper.enableFinish(binding.toolbar, this);

        binding.tabsCheckout.addTab(binding.tabsCheckout.newTab().setText(R.string.checkout_step_address));
        binding.tabsCheckout.addTab(binding.tabsCheckout.newTab().setText(R.string.checkout_step_delivery));
        binding.tabsCheckout.addTab(binding.tabsCheckout.newTab().setText(R.string.checkout_step_confirm));

        long userId = new SessionManager(this).getUserId();
        CartRepository cartRepository = new CartRepository(this);
        cartRepository.loadCart(userId, (items, total) -> {
            cartItems.clear();
            cartItems.addAll(items);
            subtotal = total;
        });

        showAddressStep();

        binding.tabsCheckout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    showAddressStep();
                } else if (tab.getPosition() == 1) {
                    showDeliveryStep();
                } else {
                    showConfirmStep();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showAddressStep() {
        binding.checkoutStepContainer.removeAllViews();
        CheckoutStepAddressBinding step = CheckoutStepAddressBinding.inflate(
                LayoutInflater.from(this), binding.checkoutStepContainer, false);
        binding.checkoutStepContainer.addView(step.getRoot());
        if (phone != null) {
            step.edtPhone.setText(phone);
        }
        if (address != null) {
            step.edtAddress.setText(address);
        }
        step.btnNext.setOnClickListener(v -> {
            address = text(step.edtAddress);
            phone = text(step.edtPhone);
            if (address.trim().isEmpty() || phone.trim().isEmpty()) {
                Toast.makeText(this, R.string.state_error, Toast.LENGTH_SHORT).show();
                return;
            }
            binding.tabsCheckout.getTabAt(1).select();
        });
    }

    private void showDeliveryStep() {
        binding.checkoutStepContainer.removeAllViews();
        CheckoutStepDeliveryBinding step = CheckoutStepDeliveryBinding.inflate(
                LayoutInflater.from(this), binding.checkoutStepContainer, false);
        binding.checkoutStepContainer.addView(step.getRoot());

        CatalogRepository catalogRepository = new CatalogRepository(this);
        catalogRepository.getDeliveryFees((standard, freeFrom, expressFee) -> {
            step.txtDeliveryFees.setText(getString(R.string.delivery_free_hint,
                    PriceUtils.formatMillimes(freeFrom)));
            step.btnNext.setOnClickListener(v -> {
                RadioButton expressRadio = step.radioExpress;
                express = expressRadio.isChecked();
                delivery = subtotal >= freeFrom ? 0 : (express ? expressFee : standard);
                binding.tabsCheckout.getTabAt(2).select();
            });
        });
    }

    private void showConfirmStep() {
        binding.checkoutStepContainer.removeAllViews();
        CheckoutStepConfirmBinding step = CheckoutStepConfirmBinding.inflate(
                LayoutInflater.from(this), binding.checkoutStepContainer, false);
        binding.checkoutStepContainer.addView(step.getRoot());
        step.txtRecap.setText((address != null ? address : "") + "\n"
                + (phone != null ? phone : "") + "\n"
                + getString(R.string.subtotal_label) + " " + PriceUtils.formatMillimes(subtotal) + "\n"
                + getString(R.string.delivery_label) + " " + PriceUtils.formatMillimes(delivery) + "\n"
                + getString(R.string.total_label) + " "
                + PriceUtils.formatMillimes(subtotal + delivery));

        step.btnConfirm.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        long userId = new SessionManager(this).getUserId();
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem c : cartItems) {
            OrderItem oi = new OrderItem();
            oi.setProductId(c.getProductId());
            oi.setQuantity(c.getQuantity());
            oi.setProductName(c.getProduct().getName());
            orderItems.add(oi);
        }
        OrderRepository orderRepository = new OrderRepository(this);
        orderRepository.createFromCart(userId, orderItems, address, phone, "COD", express,
                new OrderRepository.OrderCallback() {
                    @Override
                    public void onSuccess(com.esb.cosmetiqueshop.model.Order order) {
                        new CartRepository(CheckoutActivity.this).clear(userId, x -> {
                            Toast.makeText(CheckoutActivity.this, R.string.checkout_ok, Toast.LENGTH_SHORT).show();
                            startActivity(new android.content.Intent(CheckoutActivity.this, ClientMainActivity.class));
                            finish();
                        });
                    }

                    @Override
                    public void onFailure(String message) {
                        Toast.makeText(CheckoutActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private static String text(com.google.android.material.textfield.TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString() : "";
    }
}