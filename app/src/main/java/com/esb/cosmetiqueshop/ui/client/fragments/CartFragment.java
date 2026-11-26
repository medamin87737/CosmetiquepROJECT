package com.esb.cosmetiqueshop.ui.client.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.CartAdapter;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.databinding.FragmentCartBinding;
import com.esb.cosmetiqueshop.model.CartItem;
import com.esb.cosmetiqueshop.ui.checkout.CheckoutActivity;
import com.esb.cosmetiqueshop.ui.client.ClientMainActivity;
import com.esb.cosmetiqueshop.utils.GuestAuthHelper;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class CartFragment extends Fragment {

    private FragmentCartBinding binding;
    private CartRepository cartRepository;
    private SessionManager sessionManager;
    private final List<CartItem> items = new ArrayList<>();
    private int deliveryStandard;
    private int freeFrom;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        cartRepository = new CartRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        binding.recyclerCart.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerCart.setHasFixedSize(false);

        CatalogRepository catalogRepository = new CatalogRepository(requireContext());
        catalogRepository.getDeliveryFees((standard, freeFromMillimes, express) -> {
            deliveryStandard = standard;
            freeFrom = freeFromMillimes;
            if (binding != null) {
                loadCart();
            }
        });

        binding.btnCheckout.setOnClickListener(v -> {
            if (!GuestAuthHelper.requireLogin(sessionManager, requireContext())) {
                return;
            }
            startActivity(new Intent(requireContext(), CheckoutActivity.class));
        });
        loadCart();
        return binding.getRoot();
    }

    private void loadCart() {
        if (binding == null) {
            return;
        }
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            showEmpty(true);
            return;
        }
        cartRepository.loadCart(userId, (cartItems, subtotal) -> {
            if (binding == null || !isAdded()) {
                return;
            }
            items.clear();
            items.addAll(cartItems);

            CartAdapter adapter = new CartAdapter(items, new CartAdapter.CartListener() {
                @Override
                public void onIncrease(CartItem item) {
                    cartRepository.setQuantity(userId, item.getProductId(),
                            item.getQuantity() + 1, ok -> {
                                loadCart();
                                refreshBadge();
                            });
                }

                @Override
                public void onDecrease(CartItem item) {
                    cartRepository.setQuantity(userId, item.getProductId(),
                            item.getQuantity() - 1, ok -> {
                                loadCart();
                                refreshBadge();
                            });
                }

                @Override
                public void onRemove(CartItem item) {
                    int oldQty = item.getQuantity();
                    cartRepository.setQuantity(userId, item.getProductId(), 0, ok -> {
                        loadCart();
                        refreshBadge();
                        Snackbar.make(binding.getRoot(), R.string.cart_item_removed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.cart_undo, v ->
                                        cartRepository.setQuantity(userId, item.getProductId(), oldQty,
                                                x -> {
                                                    loadCart();
                                                    refreshBadge();
                                                }))
                                .show();
                    });
                }
            });
            binding.recyclerCart.setAdapter(adapter);

            boolean empty = items.isEmpty();
            showEmpty(empty);

            int delivery = subtotal >= freeFrom && freeFrom > 0 ? 0 : deliveryStandard;
            binding.txtSubtotal.setText(PriceUtils.formatMillimes(subtotal));
            binding.txtDelivery.setText(PriceUtils.formatMillimes(delivery));
            binding.txtTotal.setText(PriceUtils.formatMillimes(subtotal + delivery));
            binding.btnCheckout.setEnabled(!empty);
        });
    }

    private void showEmpty(boolean empty) {
        binding.cartEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.layoutCartContent.setVisibility(empty ? View.GONE : View.VISIBLE);
        if (empty) {
            binding.cartEmpty.txtEmpty.setText(R.string.empty_cart);
            binding.cartEmpty.txtEmptyHint.setText(R.string.state_empty_hint);
        }
    }

    private void refreshBadge() {
        if (requireActivity() instanceof ClientMainActivity) {
            ((ClientMainActivity) requireActivity()).refreshBadges();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCart();
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
