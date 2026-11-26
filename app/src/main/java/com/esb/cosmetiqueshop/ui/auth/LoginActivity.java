package com.esb.cosmetiqueshop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.UserRepository;
import com.esb.cosmetiqueshop.databinding.ActivityAuthBinding;
import com.esb.cosmetiqueshop.ui.NavigationHelper;
import com.esb.cosmetiqueshop.ui.publichome.PublicHomeActivity;
import com.esb.cosmetiqueshop.utils.ProDialog;
import com.esb.cosmetiqueshop.utils.StatusBarHelper;

public class LoginActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarHelper.applyHeader(this, binding.headerAuthRoot);
        userRepository = new UserRepository(this);
        binding.btnAuthBack.setOnClickListener(v -> finish());

        binding.layoutAuthTitleRow.setVisibility(View.VISIBLE);
        binding.imgAuthPerson.setVisibility(View.VISIBLE);
        binding.txtAuthTitle.setVisibility(View.VISIBLE);
        binding.txtAuthTitle.setText(R.string.login_title);
        binding.imgAuthLoginIcon.setVisibility(View.VISIBLE);
        binding.txtAuthSubtitle.setText(R.string.login_subtitle);
        binding.txtAuthSubtitle.setGravity(android.view.Gravity.CENTER);
        binding.layoutFullName.setVisibility(View.GONE);
        binding.layoutPhone.setVisibility(View.GONE);
        binding.layoutConfirmPassword.setVisibility(View.GONE);
        binding.btnPrimary.setText(R.string.btn_login);
        binding.btnSecondaryLink.setText(R.string.link_register);
        binding.btnGuest.setVisibility(View.VISIBLE);

        binding.btnPrimary.setOnClickListener(v -> attemptLogin());
        binding.btnSecondaryLink.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        binding.btnGuest.setOnClickListener(v -> {
            startActivity(new Intent(this, PublicHomeActivity.class));
            finish();
        });
    }

    private void attemptLogin() {
        String email = binding.edtEmail.getText() != null ? binding.edtEmail.getText().toString() : "";
        String password = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString() : "";
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            ProDialog.error(this, R.string.error_invalid_login);
            return;
        }
        userRepository.login(email, password, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(com.esb.cosmetiqueshop.model.User user) {
                NavigationHelper.openAfterLogin(LoginActivity.this, user);
            }

            @Override
            public void onFailure(String message) {
                ProDialog.error(LoginActivity.this, message);
            }
        });
    }
}
