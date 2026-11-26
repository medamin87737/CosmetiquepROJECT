package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.AdminDashboardRepository;
import com.esb.cosmetiqueshop.data.repository.ExportRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminRevenueBinding;
import com.esb.cosmetiqueshop.utils.PriceUtils;

public class AdminRevenueFragment extends Fragment {

    private FragmentAdminRevenueBinding binding;
    private byte[] pendingData;
    private ActivityResultLauncher<Intent> createDocumentLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createDocumentLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getData() != null && result.getData().getData() != null && pendingData != null) {
                        try (java.io.OutputStream out = requireContext().getContentResolver()
                                .openOutputStream(result.getData().getData())) {
                            if (out != null) {
                                out.write(pendingData);
                            }
                            Toast.makeText(requireContext(), R.string.export_saved, Toast.LENGTH_SHORT).show();
                        } catch (java.io.IOException e) {
                            Toast.makeText(requireContext(), R.string.state_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminRevenueBinding.inflate(inflater, container, false);
        new AdminDashboardRepository(requireContext()).loadStats(stats -> {
            if (binding == null) {
                return;
            }
            binding.txtRevenueTotal.setText(PriceUtils.formatMillimes(stats.revenueMillimes));
            binding.txtRevenueOrders.setText(getString(R.string.admin_revenue_orders, stats.deliveredOrders));
            binding.chartRevenue.setValues(stats.revenueLast7Days);
            // Jauge panier moyen : échelle relative (max 200 DT = 200000 millimes)
            float ticketPct = Math.min(100f, stats.averageTicketMillimes / 2000f);
            binding.gaugeAverageTicket.setProgress(ticketPct, getString(R.string.admin_gauge_ticket));
        });

        ExportRepository exportRepository = new ExportRepository(requireContext());
        binding.btnExportCsv.setOnClickListener(v ->
                exportRepository.buildOrdersCsvForSave(this::launchSave));
        binding.btnExportPdf.setOnClickListener(v ->
                exportRepository.buildOrdersPdfForSave(this::launchSave));
        return binding.getRoot();
    }

    private void launchSave(byte[] data, String mime, String name) {
        pendingData = data;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, name);
        createDocumentLauncher.launch(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
