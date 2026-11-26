package com.esb.cosmetiqueshop.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.UserRepository;
import com.esb.cosmetiqueshop.databinding.ActivityAdminUserEditBinding;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.model.UserRole;

public class AdminUserEditActivity extends AppCompatActivity {

    private static final String EXTRA_USER_ID = "user_id";

    private ActivityAdminUserEditBinding binding;
    private UserRepository userRepository;
    private long userId = -1;
    private final UserRole[] roles = {UserRole.CLIENT, UserRole.ADMIN};
    private String[] roleLabels;

    public static Intent intent(Context context, long userId) {
        Intent i = new Intent(context, AdminUserEditActivity.class);
        i.putExtra(EXTRA_USER_ID, userId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminUserEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        userRepository = new UserRepository(this);
        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        roleLabels = new String[]{
                getString(R.string.role_client),
                getString(R.string.role_admin)
        };
        ArrayAdapter<String> roleAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, roleLabels);
        binding.spinnerRole.setAdapter(roleAdapter);
        binding.spinnerRole.setOnItemClickListener((parent, view, position, id) ->
                updateRoleBadge(roles[position]));
        // Valeur par défaut
        binding.spinnerRole.setText(roleLabels[0], false);
        updateRoleBadge(roles[0]);

        if (userId > 0) {
            binding.toolbar.setTitle(R.string.btn_edit);
            userRepository.getUserById(userId, this::bindUser);
        } else {
            binding.toolbar.setTitle(R.string.btn_add);
            binding.switchActive.setChecked(true);
        }

        binding.btnSave.setOnClickListener(v -> save());
    }

    private void bindUser(User user) {
        if (user == null) {
            Toast.makeText(this, R.string.state_error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        binding.inputFullName.setText(user.getFullName());
        binding.inputEmail.setText(user.getEmail());
        binding.inputPhone.setText(user.getPhone());
        binding.switchActive.setChecked(user.isActive());
        UserRole displayRole = user.getRole() == UserRole.ADMIN ? UserRole.ADMIN : UserRole.CLIENT;
        for (int i = 0; i < roles.length; i++) {
            if (roles[i] == displayRole) {
                binding.spinnerRole.setText(roleLabels[i], false);
                updateRoleBadge(roles[i]);
                break;
            }
        }
        binding.inputPassword.setHint(R.string.password_optional_hint);
    }

    private void updateRoleBadge(UserRole role) {
        if (role == UserRole.ADMIN) {
            binding.txtRoleBadge.setText(R.string.role_admin);
            binding.txtRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_admin);
            binding.txtRoleBadge.setTextColor(ContextCompat.getColor(this, R.color.blanc));
        } else {
            binding.txtRoleBadge.setText(R.string.role_client);
            binding.txtRoleBadge.setBackgroundResource(R.drawable.bg_role_badge_client);
            binding.txtRoleBadge.setTextColor(ContextCompat.getColor(this, R.color.bordeaux));
        }
    }

    private int selectedRoleIndex() {
        String current = binding.spinnerRole.getText() != null
                ? binding.spinnerRole.getText().toString() : "";
        for (int i = 0; i < roleLabels.length; i++) {
            if (roleLabels[i].equals(current)) {
                return i;
            }
        }
        return 0;
    }

    private void save() {
        String name = text(binding.inputFullName);
        String email = text(binding.inputEmail);
        String phone = text(binding.inputPhone);
        String password = text(binding.inputPassword);
        UserRole role = roles[selectedRoleIndex()];
        boolean active = binding.switchActive.isChecked();

        UserRepository.AuthCallback cb = new UserRepository.AuthCallback() {
            @Override
            public void onSuccess(User user) {
                Toast.makeText(AdminUserEditActivity.this,
                        userId > 0 ? R.string.admin_user_updated : R.string.admin_user_created,
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(AdminUserEditActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        };

        if (userId <= 0) {
            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, R.string.password_hint, Toast.LENGTH_SHORT).show();
                return;
            }
            userRepository.adminCreateUser(email, password, name, phone, role, active, cb);
        } else {
            userRepository.adminUpdateUser(userId, email, name, phone, role, active,
                    TextUtils.isEmpty(password) ? null : password, cb);
        }
    }

    private String text(android.widget.EditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
