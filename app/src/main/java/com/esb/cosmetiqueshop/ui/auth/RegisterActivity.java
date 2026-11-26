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
import com.esb.cosmetiqueshop.utils.ProDialog;
import com.esb.cosmetiqueshop.utils.StatusBarHelper;

public class RegisterActivity extends AppCompatActivity {

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
        binding.imgAuthLoginIcon.setVisibility(View.GONE);
        binding.imgAuthPerson.setVisibility(View.GONE);

        binding.layoutAuthTitleRow.setVisibility(View.VISIBLE);
        binding.txtAuthTitle.setVisibility(View.VISIBLE);
        binding.txtAuthTitle.setText(R.string.register_title);
        binding.txtAuthSubtitle.setText(R.string.login_subtitle);
        binding.txtAuthSubtitle.setGravity(android.view.Gravity.CENTER);
        binding.layoutFullName.setVisibility(View.VISIBLE);
        binding.layoutPhone.setVisibility(View.VISIBLE);
        binding.layoutConfirmPassword.setVisibility(View.VISIBLE);
        binding.btnPrimary.setText(R.string.btn_register);
        binding.btnSecondaryLink.setText(R.string.link_login);
        binding.btnGuest.setVisibility(View.GONE);

        binding.btnPrimary.setOnClickListener(v -> attemptRegister());
        binding.btnSecondaryLink.setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    private void attemptRegister() {
        String email = text(binding.edtEmail);
        String password = text(binding.edtPassword);
        String confirm = text(binding.edtConfirmPassword);
        String name = text(binding.edtFullName);
        String phone = text(binding.edtPhone);
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name)) {
            ProDialog.error(this, R.string.error_invalid_login);
            return;
        }
        if (!TextUtils.equals(password, confirm)) {
            ProDialog.error(this, R.string.error_password_mismatch);
            return;
        }
        userRepository.register(email, password, name, phone, new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(com.esb.cosmetiqueshop.model.User user) {
                NavigationHelper.openAfterLogin(RegisterActivity.this, user);
            }

            @Override
            public void onFailure(String message) {
                ProDialog.error(RegisterActivity.this, message);
            }
        });
    }

    private static String text(com.google.android.material.textfield.TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString() : "";
    }
}
