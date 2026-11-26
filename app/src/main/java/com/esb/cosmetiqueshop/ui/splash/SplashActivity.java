package com.esb.cosmetiqueshop.ui.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.databinding.ActivitySplashBinding;
import com.esb.cosmetiqueshop.ui.NavigationHelper;
import com.esb.cosmetiqueshop.utils.SessionManager;

/**
 * Splash Luxury Beauty : logo pro + route session.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 1400L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Theme_CosmetiqueShop_Splash);
        super.onCreate(savedInstanceState);
        ActivitySplashBinding binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.imgSplashLogo.setImageResource(R.drawable.logo_rose_mark);
        binding.imgSplashLogo.setAlpha(0f);
        binding.imgSplashLogo.setScaleX(0.86f);
        binding.imgSplashLogo.setScaleY(0.86f);
        binding.imgSplashLogo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(520)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        new Handler(Looper.getMainLooper()).postDelayed(this::route, SPLASH_DELAY_MS);
    }

    private void route() {
        SessionManager session = new SessionManager(this);
        Intent intent;
        if (session.isLoggedIn()) {
            intent = NavigationHelper.homeIntent(this, session.getRole(), false);
        } else {
            intent = NavigationHelper.homeIntent(this, null, false);
        }
        startActivity(intent);
        finish();
    }
}
