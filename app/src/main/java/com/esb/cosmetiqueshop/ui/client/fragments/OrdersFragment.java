package com.esb.cosmetiqueshop.ui.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.OrderCardAdapter;
import com.esb.cosmetiqueshop.data.repository.OrderRepository;
import com.esb.cosmetiqueshop.databinding.FragmentOrdersBinding;
import com.esb.cosmetiqueshop.ui.order.OrderDetailActivity;
import com.esb.cosmetiqueshop.utils.GuestAuthHelper;
import com.esb.cosmetiqueshop.utils.SessionManager;

public class OrdersFragment extends Fragment {

    private FragmentOrdersBinding binding;
    private OrderRepository orderRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        orderRepository = new OrderRepository(requireContext());
        sessionManager = new SessionManager(requireContext());
        binding.recyclerOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!GuestAuthHelper.requireLogin(sessionManager, requireContext())) {
            binding.stateEmpty.getRoot().setVisibility(View.VISIBLE);
            binding.stateEmpty.txtEmpty.setText(R.string.orders_empty);
            return;
        }
        orderRepository.getOrdersForUser(sessionManager.getUserId(), orders -> {
            if (binding == null) {
                return;
            }
            binding.recyclerOrders.setAdapter(new OrderCardAdapter(orders, order ->
                    startActivity(OrderDetailActivity.intent(requireContext(), order.getId()))));
            boolean empty = orders == null || orders.isEmpty();
            binding.stateEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
            if (empty) {
                binding.stateEmpty.txtEmpty.setText(R.string.orders_empty);
            }
            binding.recyclerOrders.setVisibility(empty ? View.GONE : View.VISIBLE);
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
