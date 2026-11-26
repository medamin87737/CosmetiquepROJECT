package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.AdminDashboardRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminDashboardBinding;
import com.esb.cosmetiqueshop.databinding.ItemStatCardBinding;
import com.esb.cosmetiqueshop.utils.PriceUtils;

public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        AdminDashboardRepository repo = new AdminDashboardRepository(requireContext());
        repo.loadStats(stats -> {
            if (binding == null) {
                return;
            }
            binding.gridStats.removeAllViews();
            addStat(R.string.stat_revenue, PriceUtils.formatMillimes(stats.revenueMillimes));
            addStat(R.string.stat_orders, String.valueOf(stats.totalOrders));
            addStat(R.string.status_en_attente, String.valueOf(stats.pendingOrders));
            addStat(R.string.admin_products, String.valueOf(stats.activeProducts));
            addStat(R.string.stat_clients, String.valueOf(stats.totalClients));
            addStat(R.string.stat_low_stock, String.valueOf(stats.lowStockProducts));

            binding.chartRevenue.setValues(stats.revenueLast7Days);
            binding.gaugeFulfillment.setProgress(stats.fulfillmentPercent,
                    getString(R.string.admin_gauge_fulfillment));
            binding.gaugeStock.setProgress(stats.stockHealthPercent,
                    getString(R.string.admin_gauge_stock));
            binding.chartOrdersStatus.setData(stats.statusLabels, stats.statusCounts);
        });
        return binding.getRoot();
    }

    private void addStat(int labelRes, String value) {
        ItemStatCardBinding card = ItemStatCardBinding.inflate(getLayoutInflater(), binding.gridStats, false);
        card.txtStatLabel.setText(getString(labelRes));
        card.txtStatValue.setText(value);
        GridLayout.LayoutParams lp = new GridLayout.LayoutParams();
        lp.width = 0;
        lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        lp.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        lp.setMargins(8, 8, 8, 8);
        card.getRoot().setLayoutParams(lp);
        binding.gridStats.addView(card.getRoot());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
