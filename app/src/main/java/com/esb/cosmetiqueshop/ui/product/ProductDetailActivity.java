package com.esb.cosmetiqueshop.ui.product;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.ReviewAdapter;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.data.repository.ProductRepository;
import com.esb.cosmetiqueshop.databinding.ActivityProductDetailNewBinding;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.model.Review;
import com.esb.cosmetiqueshop.ui.auth.LoginActivity;
import com.esb.cosmetiqueshop.ui.notifications.NotificationsActivity;
import com.esb.cosmetiqueshop.utils.GuestAuthHelper;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.ProDialog;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.StatusBarHelper;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "product_id";

    private ActivityProductDetailNewBinding binding;
    private ProductRepository productRepository;
    private CatalogRepository catalogRepository;
    private CartRepository cartRepository;
    private SessionManager sessionManager;
    private long productId;
    private int qty = 1;

    public static Intent intent(Context context, long productId) {
        Intent i = new Intent(context, ProductDetailActivity.class);
        i.putExtra(EXTRA_PRODUCT_ID, productId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailNewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);
        productRepository = new ProductRepository(this);
        catalogRepository = new CatalogRepository(this);
        cartRepository = new CartRepository(this);
        sessionManager = new SessionManager(this);

        StatusBarHelper.applyHeader(this, binding.headerPro.getRoot());
        setupHeader();

        productRepository.getById(productId, this::bindProduct);

        binding.btnMinus.setOnClickListener(v -> {
            if (qty > 1) {
                qty--;
                binding.txtQty.setText(String.valueOf(qty));
            }
        });
        binding.btnPlus.setOnClickListener(v -> {
            qty++;
            binding.txtQty.setText(String.valueOf(qty));
        });
        binding.btnAddCart.setOnClickListener(v -> {
            if (!GuestAuthHelper.requireLogin(sessionManager, this)) {
                return;
            }
            cartRepository.setQuantity(sessionManager.getUserId(), productId, qty, ok ->
                    Toast.makeText(this, R.string.add_to_cart, Toast.LENGTH_SHORT).show());
        });
        binding.btnFavorite.setOnClickListener(v -> {
            if (!GuestAuthHelper.requireLogin(sessionManager, this)) {
                return;
            }
            catalogRepository.toggleFavorite(sessionManager.getUserId(), productId, isFav ->
                    Toast.makeText(this, R.string.favorite, Toast.LENGTH_SHORT).show());
        });
        binding.btnAddReview.setOnClickListener(v -> openReviewDialog());
    }

    private void setupHeader() {
        binding.headerPro.btnHeaderBack.setVisibility(View.VISIBLE);
        binding.headerPro.btnHeaderBack.setOnClickListener(v -> finish());

        if (sessionManager.isLoggedIn()) {
            binding.headerPro.btnHeaderLogin.setVisibility(View.GONE);
            binding.headerPro.btnHeaderNotifications.setVisibility(View.VISIBLE);
            binding.headerPro.btnHeaderNotifications.setOnClickListener(v ->
                    startActivity(new Intent(this, NotificationsActivity.class)));
        } else {
            binding.headerPro.btnHeaderNotifications.setVisibility(View.GONE);
            binding.headerPro.btnHeaderLogin.setVisibility(View.VISIBLE);
            binding.headerPro.btnHeaderLogin.setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
    }

    private void bindProduct(Product product) {
        if (product == null) {
            finish();
            return;
        }
        binding.txtName.setText(product.getName());
        if (binding.txtBrand != null) {
            String brand = product.getBrand() != null ? product.getBrand() : "";
            String cat = product.getCategoryName() != null ? product.getCategoryName() : "";
            binding.txtBrand.setText((brand + " · " + cat).trim());
        }
        binding.txtPrice.setText(PriceUtils.formatMillimes(product.getEffectivePriceMillimes()));
        binding.txtStock.setText(getString(R.string.stock_label, product.getStock()));
        StringBuilder details = new StringBuilder(product.getDescription() != null ? product.getDescription() : "");
        if (!TextUtils.isEmpty(product.getIngredients())) {
            details.append("\n\n").append(getString(R.string.ingredients_label)).append("\n")
                    .append(product.getIngredients());
        }
        if (!TextUtils.isEmpty(product.getTips())) {
            details.append("\n\n").append(getString(R.string.tips_label)).append("\n").append(product.getTips());
        }
        binding.txtDescription.setText(details);
        binding.btnAddCart.setEnabled(product.getStock() > 0);
        binding.txtQty.setText(String.valueOf(qty));
        if (binding.imgProductMain != null) {
            com.esb.cosmetiqueshop.utils.ProductImageLoader.load(
                    binding.imgProductMain, product.getPrimaryImageUrl());
        }
        loadReviews();
    }

    private void loadReviews() {
        catalogRepository.getReviews(productId, this::showReviews);
    }

    private void showReviews(java.util.List<Review> reviews) {
        binding.recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReviews.setAdapter(new ReviewAdapter(reviews));
    }

    private void openReviewDialog() {
        if (!GuestAuthHelper.requireLogin(sessionManager, this)) {
            return;
        }
        RatingBar ratingBar = new RatingBar(this);
        ratingBar.setNumStars(5);
        ratingBar.setStepSize(1f);
        EditText comment = new EditText(this);
        comment.setHint(R.string.comment_hint);
        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(48, 16, 48, 0);
        layout.addView(ratingBar);
        layout.addView(comment);
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.btn_submit_review)
                .setView(layout)
                .setPositiveButton(R.string.btn_send, (d, w) -> {
                    int rating = Math.round(ratingBar.getRating());
                    if (rating < 1) {
                        rating = 1;
                    }
                    String body = comment.getText() != null ? comment.getText().toString() : "";
                    catalogRepository.submitReview(sessionManager.getUserId(), productId, rating, body,
                            new CatalogRepository.ReviewSubmitCallback() {
                                @Override
                                public void onSuccess() {
                                    Toast.makeText(ProductDetailActivity.this, R.string.review_added,
                                            Toast.LENGTH_SHORT).show();
                                    loadReviews();
                                }

                                @Override
                                public void onFailure(String message) {
                                    ProDialog.error(ProductDetailActivity.this, message);
                                }
                            });
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
