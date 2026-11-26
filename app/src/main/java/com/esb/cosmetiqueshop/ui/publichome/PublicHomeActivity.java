package com.esb.cosmetiqueshop.ui.publichome;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.CategoryCircleAdapter;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.databinding.ActivityPublicHomeBinding;
import com.esb.cosmetiqueshop.databinding.FragmentClientHomeBinding;
import com.esb.cosmetiqueshop.databinding.IncludeHeaderProBinding;
import com.esb.cosmetiqueshop.ui.auth.LoginActivity;
import com.esb.cosmetiqueshop.ui.auth.RegisterActivity;
import com.esb.cosmetiqueshop.ui.client.ClientMainActivity;
import com.esb.cosmetiqueshop.ui.messages.MessagesActivity;
import com.esb.cosmetiqueshop.utils.EngagementCardsHelper;
import com.esb.cosmetiqueshop.utils.RawVideoHelper;
import com.esb.cosmetiqueshop.utils.RecyclerScrollHelper;
import com.esb.cosmetiqueshop.utils.SessionManager;
import com.esb.cosmetiqueshop.utils.StatusBarHelper;

/**
 * Accueil public vitrine : héro, catégories, cartes engagements vidéo, contact.
 */
public class PublicHomeActivity extends AppCompatActivity {

    private FragmentClientHomeBinding homeBinding;
    private IncludeHeaderProBinding header;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityPublicHomeBinding binding = ActivityPublicHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        header = binding.headerPro;
        StatusBarHelper.applyHeader(this, header.getRoot());
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        homeBinding = FragmentClientHomeBinding.bind(binding.homeContent.getRoot());
        sessionManager = new SessionManager(this);

        header.btnHeaderLogin.setVisibility(View.VISIBLE);
        header.btnHeaderNotifications.setVisibility(View.GONE);
        header.btnHeaderLogin.setOnClickListener(v -> openAccountOrLogin());

        homeBinding.recyclerCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        homeBinding.recyclerCategories.setNestedScrollingEnabled(false);
        homeBinding.recyclerCategories.setHasFixedSize(true);

        new CatalogRepository(this).getCategories(categories ->
                homeBinding.recyclerCategories.setAdapter(
                        new CategoryCircleAdapter(categories, category -> {
                            if (sessionManager.isLoggedIn()) {
                                Intent i = new Intent(this, ClientMainActivity.class);
                                i.putExtra(ClientMainActivity.EXTRA_SHOP_CATEGORY, category.getId());
                                startActivity(i);
                            } else {
                                startActivity(new Intent(this, LoginActivity.class));
                            }
                        })));

        homeBinding.btnCatPrev.bringToFront();
        homeBinding.btnCatNext.bringToFront();
        homeBinding.btnCatPrev.setOnClickListener(v ->
                RecyclerScrollHelper.scrollPrevious(homeBinding.recyclerCategories));
        homeBinding.btnCatNext.setOnClickListener(v ->
                RecyclerScrollHelper.scrollNext(homeBinding.recyclerCategories));

        homeBinding.btnOpenShop.setOnClickListener(v -> openShopOrLogin());
        homeBinding.btnContact.setOnClickListener(v -> {
            if (sessionManager.isLoggedIn()) {
                startActivity(new Intent(this, MessagesActivity.class));
            } else {
                startActivity(new Intent(this, LoginActivity.class));
            }
        });

        EngagementCardsHelper.bindAll(this,
                homeBinding.cardEngagementQuality,
                homeBinding.cardEngagementDelivery,
                homeBinding.cardEngagementSupport);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (homeBinding != null) {
            RawVideoHelper.bind(homeBinding.heroPlayer, homeBinding.heroPlaceholder, this, "video_hero");
        }
    }

    @Override
    protected void onPause() {
        if (homeBinding != null) {
            RawVideoHelper.release(homeBinding.heroPlayer);
        }
        super.onPause();
    }

    private void openShopOrLogin() {
        if (sessionManager.isLoggedIn()) {
            Intent i = new Intent(this, ClientMainActivity.class);
            i.putExtra(ClientMainActivity.EXTRA_SHOP_CATEGORY, -1L);
            startActivity(i);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    private void openAccountOrLogin() {
        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, ClientMainActivity.class));
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_public, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_login) {
            startActivity(new Intent(this, LoginActivity.class));
            return true;
        }
        if (id == R.id.action_register) {
            startActivity(new Intent(this, RegisterActivity.class));
            return true;
        }
        if (id == R.id.action_client_area && sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, ClientMainActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        RawVideoHelper.release(homeBinding.heroPlayer);
        EngagementCardsHelper.releaseAll(
                homeBinding.cardEngagementQuality,
                homeBinding.cardEngagementDelivery,
                homeBinding.cardEngagementSupport);
        super.onDestroy();
    }
}
