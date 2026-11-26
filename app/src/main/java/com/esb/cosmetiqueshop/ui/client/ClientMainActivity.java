package com.esb.cosmetiqueshop.ui.client;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.CartRepository;
import com.esb.cosmetiqueshop.data.repository.NotificationRepository;
import com.esb.cosmetiqueshop.databinding.ActivityClientMainBinding;
import com.esb.cosmetiqueshop.databinding.IncludeHeaderProBinding;
import com.esb.cosmetiqueshop.ui.client.fragments.CartFragment;
import com.esb.cosmetiqueshop.ui.client.fragments.ClientHomeFragment;
import com.esb.cosmetiqueshop.ui.client.fragments.OrdersFragment;
import com.esb.cosmetiqueshop.ui.client.fragments.ProfileFragment;
import com.esb.cosmetiqueshop.ui.assistant.BeautyAssistantActivity;
import com.esb.cosmetiqueshop.ui.client.fragments.ShopFragment;
import com.esb.cosmetiqueshop.ui.notifications.NotificationsActivity;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.StatusBarHelper;
import com.google.android.material.badge.BadgeDrawable;

public class ClientMainActivity extends AppCompatActivity {

    public static final String EXTRA_SHOP_CATEGORY = "shop_category_id";
    public static final String EXTRA_SEARCH_QUERY = "search_query";

    private ActivityClientMainBinding binding;
    private IncludeHeaderProBinding header;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClientMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        header = binding.headerPro;
        StatusBarHelper.applyHeader(this, header.getRoot(), binding.bottomNav);
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        sessionManager = new SessionManager(this);

        setupHeader();
        requestNotificationPermission();
        binding.fabBeautyAssistant.setOnClickListener(v ->
                startActivity(new Intent(this, BeautyAssistantActivity.class)));
        binding.bottomNav.setOnItemSelectedListener(this::onNavSelected);

        String searchQuery = getIntent().getStringExtra(EXTRA_SEARCH_QUERY);
        if (savedInstanceState == null) {
            if (!TextUtils.isEmpty(searchQuery)) {
                openShopWithSearch(searchQuery);
            } else if (getIntent().hasExtra(EXTRA_SHOP_CATEGORY)) {
                long shopCategory = getIntent().getLongExtra(EXTRA_SHOP_CATEGORY, -1);
                if (shopCategory > 0) {
                    openShopWithCategory(shopCategory);
                } else {
                    openShopAll();
                }
            } else {
                binding.bottomNav.setSelectedItemId(R.id.nav_client_home);
                showFragment(new ClientHomeFragment());
            }
        }
        refreshBadges();
    }

    private void setupHeader() {
        header.btnHeaderLogin.setVisibility(android.view.View.GONE);
        header.btnHeaderNotifications.setVisibility(android.view.View.VISIBLE);
        header.btnHeaderNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationsActivity.class)));
    }

    /** Boutique complète (tous les produits). */
    public void openShopAll() {
        binding.bottomNav.setSelectedItemId(R.id.nav_client_shop);
        showFragment(new ShopFragment());
    }

    public void openShopWithCategory(long categoryId) {
        binding.bottomNav.setSelectedItemId(R.id.nav_client_shop);
        if (categoryId <= 0) {
            showFragment(new ShopFragment());
        } else {
            showFragment(ShopFragment.newInstance(categoryId));
        }
    }

    public void openShopWithSearch(String query) {
        binding.bottomNav.setSelectedItemId(R.id.nav_client_shop);
        showFragment(ShopFragment.newInstanceSearch(query));
    }

    private boolean onNavSelected(@NonNull MenuItem item) {
        Fragment fragment;
        int id = item.getItemId();
        if (id == R.id.nav_client_shop) {
            fragment = new ShopFragment();
        } else if (id == R.id.nav_client_cart) {
            fragment = new CartFragment();
        } else if (id == R.id.nav_client_orders) {
            fragment = new OrdersFragment();
        } else if (id == R.id.nav_client_profile) {
            fragment = new ProfileFragment();
        } else {
            fragment = new ClientHomeFragment();
        }
        showFragment(fragment);
        return true;
    }

    private void showFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.clientFragmentContainer, fragment)
                .commit();
    }

    public void refreshBadges() {
        long userId = sessionManager.getUserId();
        if (userId <= 0) {
            return;
        }
        CartRepository cartRepository = new CartRepository(this);
        cartRepository.loadCart(userId, (items, total) -> {
            BadgeDrawable badge = binding.bottomNav.getOrCreateBadge(R.id.nav_client_cart);
            int count = items.size();
            if (count == 0) {
                binding.bottomNav.removeBadge(R.id.nav_client_cart);
            } else {
                badge.setNumber(count);
                badge.setVisible(true);
            }
        });
        NotificationRepository notificationRepository = new NotificationRepository(this);
        notificationRepository.unreadCount(userId, count -> {
            if (count > 0) {
                header.txtHeaderSubtitle.setText(
                        getString(R.string.nav_notifications) + " · " + count);
            } else {
                header.txtHeaderSubtitle.setText(R.string.header_tagline);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshBadges();
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1001);
        }
    }
}
