package com.esb.cosmetiqueshop.ui.admin;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.UserRepository;
import com.esb.cosmetiqueshop.databinding.ActivityAdminMainBinding;
import com.esb.cosmetiqueshop.ui.NavigationHelper;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminCategoriesFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminClientsFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminDashboardFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminDeliveriesFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminMessagesFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminOrdersFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminProductsFragment;
import com.esb.cosmetiqueshop.ui.admin.fragments.AdminRevenueFragment;
import com.esb.cosmetiqueshop.utils.StatusBarHelper;
import com.google.android.material.navigation.NavigationView;

public class AdminMainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActivityAdminMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarHelper.applyHeader(this, binding.adminToolbar);

        setSupportActionBar(binding.adminToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.drawerLayout,
                binding.adminToolbar, R.string.drawer_open, R.string.drawer_close);
        binding.drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        toggle.getDrawerArrowDrawable().setColor(ContextCompat.getColor(this, R.color.blanc));
        binding.adminNavView.setNavigationItemSelectedListener(this);
        binding.adminNavView.setItemIconTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.bordeaux)));

        setupSidebarToggle();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        if (savedInstanceState == null) {
            openFragment(new AdminDashboardFragment(), R.string.admin_dashboard);
            binding.adminNavView.setCheckedItem(R.id.nav_admin_dashboard);
        }
    }

    /** Flèche centrée sur le bord de la sidebar : ouvrir / réduire. */
    private void setupSidebarToggle() {
        binding.btnSidebarToggle.setOnClickListener(v -> {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        binding.drawerLayout.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {
                // La flèche suit le bord droit de la sidebar
                binding.btnSidebarToggle.setTranslationX(slideOffset * drawerView.getWidth());
            }

            @Override
            public void onDrawerOpened(@NonNull View drawerView) {
                updateSidebarToggle(true);
            }

            @Override
            public void onDrawerClosed(@NonNull View drawerView) {
                updateSidebarToggle(false);
            }
        });

        updateSidebarToggle(binding.drawerLayout.isDrawerOpen(GravityCompat.START));
    }

    private void updateSidebarToggle(boolean open) {
        if (open) {
            binding.btnSidebarToggle.setImageResource(R.drawable.ic_chevron_left);
            binding.btnSidebarToggle.setContentDescription(getString(R.string.drawer_close));
            binding.btnSidebarToggle.setBackgroundResource(R.drawable.bg_sidebar_toggle_open);
        } else {
            binding.btnSidebarToggle.setImageResource(R.drawable.ic_chevron_right);
            binding.btnSidebarToggle.setContentDescription(getString(R.string.drawer_open));
            binding.btnSidebarToggle.setBackgroundResource(R.drawable.bg_sidebar_toggle);
        }
        binding.btnSidebarToggle.setColorFilter(ContextCompat.getColor(this, R.color.blanc));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_admin_logout) {
            new UserRepository(this).logout();
            NavigationHelper.openPublic(this);
            finish();
            return true;
        }
        Fragment fragment;
        int title;
        if (id == R.id.nav_admin_products) {
            fragment = new AdminProductsFragment();
            title = R.string.admin_products;
        } else if (id == R.id.nav_admin_categories) {
            fragment = new AdminCategoriesFragment();
            title = R.string.admin_categories;
        } else if (id == R.id.nav_admin_orders) {
            fragment = new AdminOrdersFragment();
            title = R.string.admin_orders;
        } else if (id == R.id.nav_admin_deliveries) {
            fragment = new AdminDeliveriesFragment();
            title = R.string.admin_deliveries;
        } else if (id == R.id.nav_admin_clients) {
            fragment = new AdminClientsFragment();
            title = R.string.admin_users;
        } else if (id == R.id.nav_admin_messages) {
            fragment = new AdminMessagesFragment();
            title = R.string.admin_messages;
        } else if (id == R.id.nav_admin_revenue) {
            fragment = new AdminRevenueFragment();
            title = R.string.admin_revenue;
        } else {
            fragment = new AdminDashboardFragment();
            title = R.string.admin_dashboard;
        }
        openFragment(fragment, title);
        binding.drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    public void navigateTo(int menuItemId) {
        MenuItem item = binding.adminNavView.getMenu().findItem(menuItemId);
        if (item != null) {
            binding.adminNavView.setCheckedItem(menuItemId);
            onNavigationItemSelected(item);
        }
    }

    private void openFragment(Fragment fragment, int titleRes) {
        binding.txtToolbarTitle.setText(titleRes);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.adminFragmentContainer, fragment)
                .commit();
    }
}
