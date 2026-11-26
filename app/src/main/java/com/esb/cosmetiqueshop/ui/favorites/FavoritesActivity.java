package com.esb.cosmetiqueshop.ui.favorites;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.ProductAdapter;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.data.repository.ProductRepository;
import com.esb.cosmetiqueshop.databinding.ActivitySimpleListBinding;
import com.esb.cosmetiqueshop.model.Favorite;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.ui.product.ProductDetailActivity;
import com.esb.cosmetiqueshop.utils.GuestAuthHelper;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

import java.util.ArrayList;
import java.util.List;

public class FavoritesActivity extends AppCompatActivity {

    private ActivitySimpleListBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySimpleListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.toolbar.setTitle(R.string.favorites_title);
        ToolbarBackHelper.enableFinish(binding.toolbar, this);
        if (binding.toolbar.getOverflowIcon() != null) {
            binding.toolbar.getOverflowIcon().setTint(getColor(R.color.blanc));
        }

        binding.recycler.setLayoutManager(new GridLayoutManager(this, 2));
        int gap = getResources().getDimensionPixelSize(R.dimen.product_grid_gap);
        binding.recycler.addItemDecoration(new ProductAdapter.GridSpacingDecoration(2, gap));
        loadFavorites();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }

    private void loadFavorites() {
        long userId = new SessionManager(this).getUserId();
        CatalogRepository catalogRepository = new CatalogRepository(this);
        ProductRepository productRepository = new ProductRepository(this);

        catalogRepository.getFavorites(userId, favorites -> {
            List<Product> products = new ArrayList<>();
            if (favorites == null || favorites.isEmpty()) {
                showProducts(products);
                return;
            }
            final int[] pending = {favorites.size()};
            for (Favorite f : favorites) {
                productRepository.getById(f.getProductId(), product -> {
                    if (product != null) {
                        products.add(product);
                    }
                    pending[0]--;
                    if (pending[0] == 0) {
                        showProducts(products);
                    }
                });
            }
        });
    }

    private void showProducts(List<Product> products) {
        boolean empty = products.isEmpty();
        SessionManager session = new SessionManager(this);
        CartRepository cartRepository = new CartRepository(this);
        binding.recycler.setAdapter(new ProductAdapter(products,
                p -> startActivity(ProductDetailActivity.intent(this, p.getId())),
                product -> {
                    if (!GuestAuthHelper.requireLogin(session, this)) {
                        return;
                    }
                    cartRepository.addOne(session.getUserId(), product.getId(), ok ->
                            Toast.makeText(this,
                                    ok ? R.string.added_to_cart : R.string.error_add_cart,
                                    Toast.LENGTH_SHORT).show());
                },
                false));
        binding.stateEmpty.getRoot().setVisibility(empty ? View.VISIBLE : View.GONE);
        if (empty) {
            binding.stateEmpty.txtEmpty.setText(R.string.favorites_empty);
        }
        binding.recycler.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
