package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.adapter.OrderCardAdapter;
import com.esb.cosmetiqueshop.data.repository.OrderRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminListBinding;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.ui.admin.AdminOrderManageActivity;

/** Liste admin de toutes les commandes (tous statuts). */
public class AdminOrdersFragment extends Fragment {

    private FragmentAdminListBinding binding;
    private OrderRepository orderRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        binding.fabAdd.setVisibility(View.GONE);
        binding.recyclerAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        orderRepository = new OrderRepository(requireContext());
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }

    private void loadOrders() {
        if (binding == null || orderRepository == null) {
            return;
        }
        orderRepository.getAllOrders(orders -> {
            if (binding == null || !isAdded()) {
                return;
            }
            binding.recyclerAdmin.setAdapter(
                    new OrderCardAdapter(orders, this::openOrder, true));
            binding.txtEmptyAdmin.setVisibility(orders == null || orders.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
    }

    private void openOrder(Order order) {
        startActivity(AdminOrderManageActivity.intent(requireContext(), order.getId()));
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
