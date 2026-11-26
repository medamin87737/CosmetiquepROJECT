package com.esb.cosmetiqueshop.ui.auth;

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

public class ChangePasswordActivity extends AppCompatActivity {

    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityAuthBinding binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        StatusBarHelper.applyHeader(this, binding.headerAuthRoot);
        userRepository = new UserRepository(this);
        binding.btnAuthBack.setOnClickListener(v -> finish());
        binding.imgAuthLoginIcon.setVisibility(View.GONE);
        binding.imgAuthPerson.setVisibility(View.GONE);

        binding.layoutAuthTitleRow.setVisibility(View.VISIBLE);
        binding.txtAuthTitle.setVisibility(View.VISIBLE);
        binding.txtAuthTitle.setText(R.string.change_password_title);
        binding.txtAuthSubtitle.setText(R.string.change_password_subtitle);
        binding.edtEmail.setVisibility(View.GONE);
        binding.layoutFullName.setVisibility(View.GONE);
        binding.layoutPhone.setVisibility(View.GONE);
        binding.edtPassword.setHint(getString(R.string.new_password_hint));
        binding.layoutConfirmPassword.setVisibility(View.VISIBLE);
        binding.btnPrimary.setText(R.string.btn_save_password);
        binding.btnSecondaryLink.setVisibility(View.GONE);
        binding.btnGuest.setVisibility(View.GONE);

        binding.btnPrimary.setOnClickListener(v -> {
            String p1 = binding.edtPassword.getText() != null ? binding.edtPassword.getText().toString() : "";
            String p2 = binding.edtConfirmPassword.getText() != null
                    ? binding.edtConfirmPassword.getText().toString() : "";
            if (!TextUtils.equals(p1, p2)) {
                ProDialog.error(this, R.string.error_password_mismatch);
                return;
            }
            long userId = userRepository.getSessionManager().getUserId();
            userRepository.getCurrentUser(user -> {
                boolean forced = user != null && user.isMustChangePassword();
                userRepository.changePassword(userId, p1, forced, new UserRepository.AuthCallback() {
                    @Override
                    public void onSuccess(com.esb.cosmetiqueshop.model.User user) {
                        startActivity(NavigationHelper.homeIntent(ChangePasswordActivity.this,
                                user.getRole(), false));
                        finish();
                    }

                    @Override
                    public void onFailure(String message) {
                        ProDialog.error(ChangePasswordActivity.this, message);
                    }
                });
            });
        });
    }

    @Override
    public void onBackPressed() {
        // Obligatoire pour admin première connexion
        moveTaskToBack(true);
    }
}
