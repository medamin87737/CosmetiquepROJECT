package com.esb.cosmetiqueshop.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.UserRepository;
import com.esb.cosmetiqueshop.databinding.ActivityChangePasswordBinding;
import com.esb.cosmetiqueshop.ui.NavigationHelper;
import com.esb.cosmetiqueshop.utils.ProDialog;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

/**
 * Écran dédié : nouveau mot de passe + confirmation.
 */
public class ChangePasswordActivity extends AppCompatActivity {

    private ActivityChangePasswordBinding binding;
    private UserRepository userRepository;
    private boolean forceChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        userRepository = new UserRepository(this);
        userRepository.getCurrentUser(user -> {
            forceChange = user != null && user.isMustChangePassword();
            if (forceChange) {
                binding.toolbar.setNavigationOnClickListener(v -> moveTaskToBack(true));
                binding.txtSubtitle.setText(R.string.change_password_subtitle);
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (forceChange) {
                    moveTaskToBack(true);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        binding.btnSavePassword.setOnClickListener(v -> savePassword());
    }

    private void savePassword() {
        String p1 = binding.edtNewPassword.getText() != null
                ? binding.edtNewPassword.getText().toString() : "";
        String p2 = binding.edtConfirmPassword.getText() != null
                ? binding.edtConfirmPassword.getText().toString() : "";
        if (!TextUtils.equals(p1, p2)) {
            ProDialog.error(this, R.string.error_password_mismatch);
            return;
        }
        binding.btnSavePassword.setEnabled(false);
        long userId = userRepository.getSessionManager().getUserId();
        userRepository.getCurrentUser(user -> {
            boolean forced = user != null && user.isMustChangePassword();
            forceChange = forced;
            userRepository.changePassword(userId, p1, forced, new UserRepository.AuthCallback() {
                @Override
                public void onSuccess(com.esb.cosmetiqueshop.model.User updated) {
                    Toast.makeText(ChangePasswordActivity.this, R.string.password_saved,
                            Toast.LENGTH_SHORT).show();
                    if (forced) {
                        startActivity(NavigationHelper.homeIntent(ChangePasswordActivity.this,
                                updated.getRole(), false));
                    }
                    finish();
                }

                @Override
                public void onFailure(String message) {
                    binding.btnSavePassword.setEnabled(true);
                    ProDialog.error(ChangePasswordActivity.this, message);
                }
            });
        });
    }
}
