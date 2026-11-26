package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.AdminProductAdapter;
import com.esb.cosmetiqueshop.data.repository.ProductRepository;
import com.esb.cosmetiqueshop.databinding.FragmentAdminListBinding;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.ui.admin.AdminProductEditActivity;

import java.util.ArrayList;
import java.util.List;

public class AdminProductsFragment extends Fragment {

    private static final int PAGE_SIZE = 8;

    private FragmentAdminListBinding binding;
    private ProductRepository productRepository;
    private AdminProductAdapter adapter;
    private final List<Product> allProducts = new ArrayList<>();
    private int currentPage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        productRepository = new ProductRepository(requireContext());

        binding.recyclerAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new AdminProductAdapter(p ->
                startActivity(AdminProductEditActivity.intent(requireContext(), p.getId())));
        binding.recyclerAdmin.setAdapter(adapter);

        binding.layoutPagination.setVisibility(View.VISIBLE);
        binding.fabAdd.setOnClickListener(v ->
                startActivity(AdminProductEditActivity.intent(requireContext(), -1)));
        binding.btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 0) {
                currentPage--;
                showPage();
            }
        });
        binding.btnNextPage.setOnClickListener(v -> {
            if ((currentPage + 1) * PAGE_SIZE < allProducts.size()) {
                currentPage++;
                showPage();
            }
        });

        load();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        load();
    }

    private void load() {
        productRepository.getAllForAdmin(products -> {
            if (binding == null) {
                return;
            }
            allProducts.clear();
            if (products != null) {
                allProducts.addAll(products);
            }
            currentPage = 0;
            showPage();
        });
    }

    private void showPage() {
        int totalPages = Math.max(1, (int) Math.ceil(allProducts.size() / (double) PAGE_SIZE));
        if (currentPage >= totalPages) {
            currentPage = totalPages - 1;
        }
        int from = currentPage * PAGE_SIZE;
        int to = Math.min(from + PAGE_SIZE, allProducts.size());
        List<Product> page = from < to ? allProducts.subList(from, to) : new ArrayList<>();
        adapter.submit(page);

        boolean empty = allProducts.isEmpty();
        binding.txtEmptyAdmin.setVisibility(empty ? View.VISIBLE : View.GONE);
        binding.recyclerAdmin.setVisibility(empty ? View.GONE : View.VISIBLE);
        binding.txtPageInfo.setText(getString(R.string.page_info, currentPage + 1, totalPages));
        binding.btnPrevPage.setEnabled(currentPage > 0);
        binding.btnNextPage.setEnabled((currentPage + 1) * PAGE_SIZE < allProducts.size());
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
