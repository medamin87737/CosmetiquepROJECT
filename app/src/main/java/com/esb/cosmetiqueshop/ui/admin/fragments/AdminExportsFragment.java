package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.ExportRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminDashboardBinding;
import com.google.android.material.button.MaterialButton;

public class AdminExportsFragment extends Fragment {

    private byte[] pendingData;
    private String pendingMime;
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
        FragmentAdminDashboardBinding binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        LinearLayout wrap = new LinearLayout(requireContext());
        wrap.setOrientation(LinearLayout.VERTICAL);
        binding.gridStats.removeAllViews();

        MaterialButton csv = new MaterialButton(requireContext());
        csv.setText(R.string.btn_export_csv);
        csv.setOnClickListener(v -> new ExportRepository(requireContext()).buildOrdersCsvForSave(this::launchSave));
        wrap.addView(csv);

        MaterialButton pdf = new MaterialButton(requireContext());
        pdf.setText(R.string.btn_export_pdf);
        pdf.setOnClickListener(v -> new ExportRepository(requireContext()).buildOrdersPdfForSave(this::launchSave));
        wrap.addView(pdf);

        binding.gridStats.addView(wrap);
        return binding.getRoot();
    }

    private void launchSave(byte[] data, String mime, String name) {
        pendingData = data;
        pendingMime = mime;
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(mime);
        intent.putExtra(Intent.EXTRA_TITLE, name);
        createDocumentLauncher.launch(intent);
    }
}