package com.esb.cosmetiqueshop.ui.admin.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.adapter.AdminCategoryAdapter;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.databinding.DialogAdminCategoryBinding;
import com.esb.cosmetiqueshop.databinding.FragmentAdminListBinding;
import com.esb.cosmetiqueshop.model.Category;
import com.esb.cosmetiqueshop.utils.DemoSeedImages;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Admin — catégories : ajout / édition avec image (galerie ou URL).
 */
public class AdminCategoriesFragment extends Fragment {

    private CatalogRepository catalogRepository;
    private FragmentAdminListBinding binding;
    private AdminCategoryAdapter adapter;

    private DialogAdminCategoryBinding dialogBinding;
    private Uri pendingGalleryUri;
    private Category editingCategory;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null || dialogBinding == null) {
                    return;
                }
                pendingGalleryUri = uri;
                ProductImageLoader.load(dialogBinding.imgCategoryPreview, uri.toString());
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminListBinding.inflate(inflater, container, false);
        catalogRepository = new CatalogRepository(requireContext());
        adapter = new AdminCategoryAdapter(this::showCategoryDialog);
        binding.recyclerAdmin.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerAdmin.setAdapter(adapter);
        binding.fabAdd.setOnClickListener(v -> showCategoryDialog(null));
        load();
        return binding.getRoot();
    }

    private void load() {
        catalogRepository.getCategories(categories -> {
            if (binding == null) {
                return;
            }
            adapter.submit(categories);
            binding.txtEmptyAdmin.setVisibility(categories == null || categories.isEmpty()
                    ? View.VISIBLE : View.GONE);
        });
    }

    private void showCategoryDialog(@Nullable Category existing) {
        editingCategory = existing;
        pendingGalleryUri = null;
        dialogBinding = DialogAdminCategoryBinding.inflate(getLayoutInflater());

        if (existing != null) {
            dialogBinding.edtCategoryName.setText(existing.getName());
            dialogBinding.edtCategoryDesc.setText(existing.getDescription());
            String img = existing.getImageUrl();
            if (!TextUtils.isEmpty(img)) {
                if (img.startsWith("http://") || img.startsWith("https://")) {
                    dialogBinding.edtCategoryImageUrl.setText(img);
                }
                ProductImageLoader.load(dialogBinding.imgCategoryPreview, img);
            }
        }

        dialogBinding.btnPickCategoryImage.setOnClickListener(v ->
                pickImageLauncher.launch("image/*"));

        dialogBinding.edtCategoryImageUrl.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (pendingGalleryUri != null) {
                    return;
                }
                String url = s != null ? s.toString().trim() : "";
                if (!url.isEmpty()) {
                    ProductImageLoader.load(dialogBinding.imgCategoryPreview, url);
                }
            }
        });

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext())
                .setTitle(existing == null ? R.string.category_add_title : R.string.category_edit_title)
                .setView(dialogBinding.getRoot())
                .setPositiveButton(R.string.btn_save, null)
                .setNegativeButton(R.string.btn_cancel, null);

        if (existing != null) {
            builder.setNeutralButton(R.string.btn_delete, null);
        }

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setOnClickListener(v -> saveCategory(dialog));
            if (existing != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v ->
                        confirmDeleteCategory(existing, dialog));
            }
        });
        dialog.show();
    }

    private void confirmDeleteCategory(Category category, AlertDialog parentDialog) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.category_delete_confirm_title)
                .setMessage(getString(R.string.category_delete_confirm_message, category.getName()))
                .setPositiveButton(R.string.btn_delete, (d, w) ->
                        catalogRepository.deleteCategory(category.getId(),
                                new CatalogRepository.DeleteCallback() {
                                    @Override
                                    public void onSuccess() {
                                        Toast.makeText(requireContext(), R.string.category_deleted,
                                                Toast.LENGTH_SHORT).show();
                                        parentDialog.dismiss();
                                        dialogBinding = null;
                                        editingCategory = null;
                                        load();
                                    }

                                    @Override
                                    public void onFailure(String message) {
                                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                                    }
                                }))
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void saveCategory(AlertDialog dialog) {
        if (dialogBinding == null) {
            return;
        }
        String name = text(dialogBinding.edtCategoryName);
        if (TextUtils.isEmpty(name)) {
            dialogBinding.edtCategoryName.setError(getString(R.string.category_name_hint));
            return;
        }

        Category c = editingCategory != null ? editingCategory : new Category();
        c.setName(name);
        c.setDescription(text(dialogBinding.edtCategoryDesc));
        if (c.getSortOrder() <= 0) {
            c.setSortOrder(99);
        }

        String url = text(dialogBinding.edtCategoryImageUrl);
        if (!TextUtils.isEmpty(url)) {
            c.setImageUrl(url);
        } else if (TextUtils.isEmpty(c.getImageUrl()) && pendingGalleryUri == null) {
            c.setImageUrl(DemoSeedImages.CAT_SOIN);
        }

        final Uri galleryUri = pendingGalleryUri;
        catalogRepository.saveCategory(c, new CatalogRepository.CategoryCallback() {
            @Override
            public void onResult(Category saved) {
                if (galleryUri != null && saved != null) {
                    String local = copyGalleryImage(galleryUri, saved.getId());
                    if (local != null) {
                        saved.setImageUrl(local);
                        catalogRepository.saveCategory(saved, new CatalogRepository.CategoryCallback() {
                            @Override
                            public void onResult(Category ignored) {
                                finishSave(dialog);
                            }

                            @Override
                            public void onFailure(String message) {
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                            }
                        });
                        return;
                    }
                }
                finishSave(dialog);
            }

            @Override
            public void onFailure(String message) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finishSave(AlertDialog dialog) {
        Toast.makeText(requireContext(), R.string.btn_save, Toast.LENGTH_SHORT).show();
        dialog.dismiss();
        dialogBinding = null;
        pendingGalleryUri = null;
        editingCategory = null;
        load();
    }

    /** Copie galerie → filesDir/category_images/{id}.jpg */
    private String copyGalleryImage(Uri source, long categoryId) {
        File dir = new File(requireContext().getFilesDir(), "category_images");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        File dest = new File(dir, categoryId + ".jpg");
        try (InputStream in = requireContext().getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return dest.getAbsolutePath();
        } catch (IOException e) {
            return null;
        }
    }

    private static String text(com.google.android.material.textfield.TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        dialogBinding = null;
        binding = null;
        super.onDestroyView();
    }
}
