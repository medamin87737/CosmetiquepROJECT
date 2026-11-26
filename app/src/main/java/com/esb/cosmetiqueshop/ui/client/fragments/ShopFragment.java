package com.esb.cosmetiqueshop.ui.client.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.ProductAdapter;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.data.repository.ProductRepository;
import com.esb.cosmetiqueshop.databinding.BottomSheetShopFiltersBinding;
import com.esb.cosmetiqueshop.databinding.FragmentShopBinding;
import com.esb.cosmetiqueshop.model.Category;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.ui.client.ClientMainActivity;
import com.esb.cosmetiqueshop.ui.product.ProductDetailActivity;
import com.esb.cosmetiqueshop.utils.GuestAuthHelper;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ShopFragment extends Fragment {

    private static final String ARG_CATEGORY_ID = "category_id";
    private static final String ARG_SEARCH_QUERY = "search_query";

    private FragmentShopBinding binding;
    private ProductRepository productRepository;
    private CatalogRepository catalogRepository;
    private CartRepository cartRepository;
    private SessionManager sessionManager;
    private final List<Product> allProducts = new ArrayList<>();
    private final List<Category> categories = new ArrayList<>();
    private Long selectedCategoryId = null;
    private int sortMode;
    private boolean filterPromo;
    private boolean filterStock;
    private String initialQuery = "";
    private boolean gridDecorationAdded;

    public static ShopFragment newInstance(long categoryId) {
        ShopFragment f = new ShopFragment();
        Bundle b = new Bundle();
        b.putLong(ARG_CATEGORY_ID, categoryId);
        f.setArguments(b);
        return f;
    }

    public static ShopFragment newInstanceSearch(String query) {
        ShopFragment f = new ShopFragment();
        Bundle b = new Bundle();
        b.putString(ARG_SEARCH_QUERY, query);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentShopBinding.inflate(inflater, container, false);
        productRepository = new ProductRepository(requireContext());
        catalogRepository = new CatalogRepository(requireContext());
        cartRepository = new CartRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        if (getArguments() != null) {
            if (getArguments().containsKey(ARG_CATEGORY_ID)) {
                long id = getArguments().getLong(ARG_CATEGORY_ID, -1);
                if (id > 0) {
                    selectedCategoryId = id;
                }
            }
            String q = getArguments().getString(ARG_SEARCH_QUERY);
            if (q != null) {
                initialQuery = q;
            }
        }

        binding.recyclerProducts.setLayoutManager(new GridLayoutManager(getContext(), 2));
        if (!gridDecorationAdded) {
            int gap = getResources().getDimensionPixelSize(R.dimen.product_grid_gap);
            binding.recyclerProducts.addItemDecoration(
                    new ProductAdapter.GridSpacingDecoration(2, gap));
            gridDecorationAdded = true;
        }
        binding.btnOpenFilters.setOnClickListener(v -> showFiltersSheet());

        if (!initialQuery.isEmpty()) {
            binding.edtSearch.setText(initialQuery);
        }
        binding.edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                applyFilters();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        catalogRepository.getCategories(cats -> {
            categories.clear();
            categories.addAll(cats);
        });

        productRepository.getAllActive(products -> {
            allProducts.clear();
            allProducts.addAll(products);
            applyFilters();
        });
        return binding.getRoot();
    }

    public void setCategoryFilter(long categoryId) {
        selectedCategoryId = categoryId > 0 ? categoryId : null;
        applyFilters();
    }

    private void showFiltersSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        BottomSheetShopFiltersBinding sheet =
                BottomSheetShopFiltersBinding.inflate(getLayoutInflater());
        dialog.setContentView(sheet.getRoot());

        List<String> categoryLabels = new ArrayList<>();
        categoryLabels.add(getString(R.string.filter_all_categories));
        int selectedCatIndex = 0;
        for (int i = 0; i < categories.size(); i++) {
            Category c = categories.get(i);
            categoryLabels.add(c.getName());
            if (selectedCategoryId != null && selectedCategoryId == c.getId()) {
                selectedCatIndex = i + 1;
            }
        }
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, categoryLabels);
        sheet.spinnerFilterCategory.setAdapter(catAdapter);
        sheet.spinnerFilterCategory.setSelection(selectedCatIndex);

        ArrayAdapter<CharSequence> sortAdapter = ArrayAdapter.createFromResource(requireContext(),
                R.array.sort_options, android.R.layout.simple_spinner_dropdown_item);
        sheet.spinnerFilterSort.setAdapter(sortAdapter);
        sheet.spinnerFilterSort.setSelection(Math.max(0, Math.min(sortMode, sortAdapter.getCount() - 1)));

        sheet.switchFilterPromo.setChecked(filterPromo);
        sheet.switchFilterStock.setChecked(filterStock);

        sheet.btnFilterReset.setOnClickListener(v -> {
            selectedCategoryId = null;
            sortMode = 0;
            filterPromo = false;
            filterStock = false;
            binding.edtSearch.setText("");
            applyFilters();
            dialog.dismiss();
        });

        sheet.btnFilterApply.setOnClickListener(v -> {
            int catPos = sheet.spinnerFilterCategory.getSelectedItemPosition();
            if (catPos <= 0) {
                selectedCategoryId = null;
            } else {
                selectedCategoryId = categories.get(catPos - 1).getId();
            }
            sortMode = sheet.spinnerFilterSort.getSelectedItemPosition();
            filterPromo = sheet.switchFilterPromo.isChecked();
            filterStock = sheet.switchFilterStock.isChecked();
            applyFilters();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void applyFilters() {
        if (binding == null) {
            return;
        }
        String q = binding.edtSearch.getText() != null ? binding.edtSearch.getText().toString() : "";
        List<Product> filtered = new ArrayList<>();
        for (Product p : allProducts) {
            if (selectedCategoryId != null && p.getCategoryId() != selectedCategoryId) {
                continue;
            }
            if (filterPromo && !p.hasPromo()) {
                continue;
            }
            if (filterStock && p.getStock() <= 0) {
                continue;
            }
            if (!q.isEmpty() && !p.getName().toLowerCase().contains(q.toLowerCase())) {
                continue;
            }
            filtered.add(p);
        }
        if (sortMode == 1) {
            Collections.sort(filtered, Comparator.comparingInt(Product::getEffectivePriceMillimes));
        } else if (sortMode == 2) {
            Collections.sort(filtered, (a, b) -> Integer.compare(b.getEffectivePriceMillimes(),
                    a.getEffectivePriceMillimes()));
        } else {
            Collections.sort(filtered, Comparator.comparing(p -> p.getName().toLowerCase()));
        }
        binding.recyclerProducts.setAdapter(new ProductAdapter(filtered,
                product -> startActivity(ProductDetailActivity.intent(requireContext(), product.getId())),
                this::addProductToCart,
                false));
        binding.stateEmpty.getRoot().setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void addProductToCart(Product product) {
        if (!GuestAuthHelper.requireLogin(sessionManager, requireContext())) {
            return;
        }
        cartRepository.addOne(sessionManager.getUserId(), product.getId(), ok -> {
            Toast.makeText(requireContext(),
                    ok ? R.string.added_to_cart : R.string.error_add_cart,
                    Toast.LENGTH_SHORT).show();
            if (ok && requireActivity() instanceof ClientMainActivity) {
                ((ClientMainActivity) requireActivity()).refreshBadges();
            }
        });
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
