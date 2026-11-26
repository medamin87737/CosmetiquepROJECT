package com.esb.cosmetiqueshop.ui.client.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.ProductAdapter;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.ProductRepository;
import com.esb.cosmetiqueshop.databinding.FragmentClientHomeProductsBinding;
import com.esb.cosmetiqueshop.databinding.IncludeProductCarouselBinding;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.ui.client.ClientMainActivity;
import com.esb.cosmetiqueshop.ui.product.ProductDetailActivity;
import com.esb.cosmetiqueshop.utils.GuestAuthHelper;
import com.esb.cosmetiqueshop.utils.RecyclerScrollHelper;
import com.esb.cosmetiqueshop.utils.SessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Accueil client connecté : sections produits horizontales (promos, best, nouveautés).
 */
public class ClientHomeFragment extends Fragment {

    private FragmentClientHomeProductsBinding binding;
    private ProductRepository productRepository;
    private CartRepository cartRepository;
    private SessionManager sessionManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentClientHomeProductsBinding.inflate(inflater, container, false);
        productRepository = new ProductRepository(requireContext());
        cartRepository = new CartRepository(requireContext());
        sessionManager = new SessionManager(requireContext());

        setupCarouselHeader(binding.carouselPromos, R.string.section_promos);
        setupCarouselHeader(binding.carouselBest, R.string.section_best);
        setupCarouselHeader(binding.carouselNew, R.string.section_new);

        binding.btnOpenShop.setOnClickListener(v -> {
            if (requireActivity() instanceof ClientMainActivity) {
                ((ClientMainActivity) requireActivity()).openShopAll();
            }
        });

        loadProducts();
        return binding.getRoot();
    }

    private void setupCarouselHeader(IncludeProductCarouselBinding carousel, @StringRes int titleRes) {
        carousel.txtCarouselTitle.setText(titleRes);
        carousel.btnCarouselPrev.setOnClickListener(v ->
                RecyclerScrollHelper.scrollPrevious(carousel.recyclerCarousel));
        carousel.btnCarouselNext.setOnClickListener(v ->
                RecyclerScrollHelper.scrollNext(carousel.recyclerCarousel));
        carousel.recyclerCarousel.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        carousel.recyclerCarousel.setNestedScrollingEnabled(false);
        carousel.recyclerCarousel.setHasFixedSize(true);
    }

    private void loadProducts() {
        productRepository.getAllActive(products -> {
            if (binding == null || !isAdded()) {
                return;
            }
            List<Product> all = products != null ? products : Collections.emptyList();

            List<Product> promos = new ArrayList<>();
            for (Product p : all) {
                if (p.hasPromo()) {
                    promos.add(p);
                }
            }
            if (promos.isEmpty()) {
                promos = new ArrayList<>(all);
            }

            List<Product> best = new ArrayList<>(all);
            Collections.sort(best, Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));

            List<Product> news = new ArrayList<>(all);
            Collections.sort(news, (a, b) -> Long.compare(b.getId(), a.getId()));

            bindCarousel(binding.carouselPromos, promos);
            bindCarousel(binding.carouselBest, best);
            bindCarousel(binding.carouselNew, news);
        });
    }

    private void bindCarousel(IncludeProductCarouselBinding carousel, List<Product> products) {
        ProductAdapter adapter = new ProductAdapter(products,
                product -> startActivity(ProductDetailActivity.intent(requireContext(), product.getId())),
                this::addProductToCart,
                true);
        carousel.recyclerCarousel.setAdapter(adapter);

        int gap = getResources().getDimensionPixelSize(R.dimen.product_card_gap);
        while (carousel.recyclerCarousel.getItemDecorationCount() > 0) {
            carousel.recyclerCarousel.removeItemDecorationAt(0);
        }
        carousel.recyclerCarousel.addItemDecoration(new ProductAdapter.GapDecoration(gap));

        carousel.recyclerCarousel.post(() -> {
            int width = carousel.recyclerCarousel.getWidth();
            if (width <= 0) {
                width = getResources().getDisplayMetrics().widthPixels
                        - 2 * getResources().getDimensionPixelSize(R.dimen.spacing_md);
            }
            adapter.setCarouselWidth(width, gap);
        });
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
