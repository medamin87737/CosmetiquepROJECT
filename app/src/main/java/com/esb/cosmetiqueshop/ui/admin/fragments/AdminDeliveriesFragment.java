package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.adapter.DeliveredPackageAdapter;
import com.esb.cosmetiqueshop.data.repository.OrderRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminDeliveriesBinding;
import com.esb.cosmetiqueshop.ui.admin.AdminOrderManageActivity;

/**
 * Admin — colis livrés (détails client, adresse, articles). Pas de rôle livreur.
 */
public class AdminDeliveriesFragment extends Fragment {

    private FragmentAdminDeliveriesBinding binding;
    private OrderRepository orderRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDeliveriesBinding.inflate(inflater, container, false);
        orderRepository = new OrderRepository(requireContext());
        binding.recyclerDelivered.setLayoutManager(new LinearLayoutManager(requireContext()));
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDelivered();
    }

    private void loadDelivered() {
        orderRepository.getDeliveredOrders(orders -> {
            if (binding == null || !isAdded()) {
                return;
            }
            boolean empty = orders == null || orders.isEmpty();
            binding.txtDeliveredEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
            binding.recyclerDelivered.setVisibility(empty ? View.GONE : View.VISIBLE);
            if (empty) {
                return;
            }
            binding.recyclerDelivered.setAdapter(new DeliveredPackageAdapter(orders, order ->
                    startActivity(AdminOrderManageActivity.intent(requireContext(), order.getId()))));
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
