package com.esb.cosmetiqueshop.ui.admin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.esb.cosmetiqueshop.R;
import com.esb.cosmetiqueshop.data.repository.CatalogRepository;
import com.esb.cosmetiqueshop.data.repository.ProductRepository;
import com.esb.cosmetiqueshop.databinding.ActivityAdminProductEditBinding;
import com.esb.cosmetiqueshop.model.Category;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.PriceUtils;
import com.esb.cosmetiqueshop.utils.ProductImageLoader;
import com.esb.cosmetiqueshop.utils.ProDialog;
import com.esb.cosmetiqueshop.utils.ToolbarBackHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Création / édition produit : marque saisie libre, promo activable à tout moment.
 */
public class AdminProductEditActivity extends AppCompatActivity {

    private static final String EXTRA_PRODUCT_ID = "product_id";

    private ActivityAdminProductEditBinding binding;
    private ProductRepository productRepository;
    private CatalogRepository catalogRepository;
    private final List<Category> categories = new ArrayList<>();
    private long productId;
    private Uri pendingGalleryUri;
    private String pendingLocalImagePath;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri == null) {
                    return;
                }
                pendingGalleryUri = uri;
                ProductImageLoader.load(binding.imgPreview, uri.toString());
            });

    public static Intent intent(Context context, long productId) {
        Intent i = new Intent(context, AdminProductEditActivity.class);
        i.putExtra(EXTRA_PRODUCT_ID, productId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAdminProductEditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ToolbarBackHelper.enableFinish(binding.toolbar, this);

        productRepository = new ProductRepository(this);
        catalogRepository = new CatalogRepository(this);
        productId = getIntent().getLongExtra(EXTRA_PRODUCT_ID, -1);

        ArrayAdapter<CharSequence> skinAdapter = ArrayAdapter.createFromResource(this,
                R.array.skin_types, android.R.layout.simple_spinner_item);
        skinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinnerSkin.setAdapter(skinAdapter);

        binding.switchPromo.setOnCheckedChangeListener((buttonView, isChecked) ->
                binding.layoutPromoPrice.setVisibility(isChecked ? View.VISIBLE : View.GONE));

        catalogRepository.getCategories(list -> {
            categories.clear();
            categories.addAll(list);
            ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, categoryNames());
            catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            binding.spinnerCategory.setAdapter(catAdapter);
            if (productId > 0) {
                productRepository.getById(productId, this::bindProduct);
                productRepository.getPrimaryImageUrl(productId, this::bindPrimaryImage);
            }
        });

        binding.btnPickGallery.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        binding.btnSave.setOnClickListener(v -> saveProduct());
    }

    private List<String> categoryNames() {
        List<String> names = new ArrayList<>();
        for (Category c : categories) {
            names.add(c.getName());
        }
        return names;
    }

    private void bindProduct(Product product) {
        if (product == null) {
            return;
        }
        binding.edtName.setText(product.getName());
        binding.edtBrand.setText(product.getBrand() != null ? product.getBrand() : "");
        binding.edtDescription.setText(product.getDescription());
        binding.edtIngredients.setText(product.getIngredients());
        binding.edtTips.setText(product.getTips());
        binding.edtPriceDt.setText(PriceUtils.formatMillimesNoSuffix(product.getPriceMillimes()));

        boolean onPromo = product.getPromoPriceMillimes() > 0;
        binding.switchPromo.setChecked(onPromo);
        binding.layoutPromoPrice.setVisibility(onPromo ? View.VISIBLE : View.GONE);
        if (onPromo) {
            binding.edtPromoDt.setText(PriceUtils.formatMillimesNoSuffix(product.getPromoPriceMillimes()));
        } else {
            binding.edtPromoDt.setText("");
        }

        binding.edtStock.setText(String.valueOf(product.getStock()));
        binding.edtAlertThreshold.setText(String.valueOf(product.getAlertThreshold()));
        binding.switchActive.setChecked(product.isActive());
        for (int i = 0; i < categories.size(); i++) {
            if (categories.get(i).getId() == product.getCategoryId()) {
                binding.spinnerCategory.setSelection(i);
                break;
            }
        }
        if (product.getSkinType() != null) {
            ArrayAdapter adapter = (ArrayAdapter) binding.spinnerSkin.getAdapter();
            for (int i = 0; i < adapter.getCount(); i++) {
                if (product.getSkinType().equals(adapter.getItem(i))) {
                    binding.spinnerSkin.setSelection(i);
                    break;
                }
            }
        }
    }

    private void bindPrimaryImage(String url) {
        if (url != null) {
            binding.edtImageUrl.setText(url);
            ProductImageLoader.load(binding.imgPreview, url);
        }
    }

    private void saveProduct() {
        String name = text(binding.edtName);
        if (TextUtils.isEmpty(name) || categories.isEmpty()) {
            Toast.makeText(this, R.string.state_error, Toast.LENGTH_SHORT).show();
            return;
        }
        int price = parseDt(binding.edtPriceDt);
        if (price <= 0) {
            Toast.makeText(this, R.string.state_error, Toast.LENGTH_SHORT).show();
            return;
        }

        int promo = 0;
        if (binding.switchPromo.isChecked()) {
            promo = parseDt(binding.edtPromoDt);
            if (promo <= 0 || promo >= price) {
                ProDialog.error(this, R.string.error_promo_price);
                return;
            }
        }

        Product product = new Product();
        product.setId(productId);
        int catIndex = binding.spinnerCategory.getSelectedItemPosition();
        product.setCategoryId(categories.get(catIndex).getId());
        product.setName(name);
        product.setBrand(text(binding.edtBrand));
        product.setSkinType(binding.spinnerSkin.getSelectedItem().toString());
        product.setDescription(text(binding.edtDescription));
        product.setIngredients(text(binding.edtIngredients));
        product.setTips(text(binding.edtTips));
        product.setPriceMillimes(price);
        product.setPromoPriceMillimes(promo);
        product.setStock(parseInt(binding.edtStock, 0));
        product.setAlertThreshold(parseInt(binding.edtAlertThreshold, 10));
        product.setActive(binding.switchActive.isChecked());

        String imageUrl = text(binding.edtImageUrl);

        productRepository.save(product, imageUrl, saved -> {
            if (saved == null) {
                return;
            }
            long id = saved.getId();
            if (pendingGalleryUri != null) {
                String finalUrl = copyGalleryImageToAppStorage(pendingGalleryUri, id);
                if (finalUrl != null) {
                    productRepository.save(saved, finalUrl, ignored -> {
                        Toast.makeText(this, R.string.btn_save, Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }
            }
            Toast.makeText(this, R.string.btn_save, Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    /** Copie l'image choisie vers filesDir/product_images/{id}.jpg */
    private String copyGalleryImageToAppStorage(Uri source, long id) {
        File dir = new File(getFilesDir(), "product_images");
        if (!dir.exists() && !dir.mkdirs()) {
            return null;
        }
        File dest = new File(dir, id + ".jpg");
        try (InputStream in = getContentResolver().openInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) {
                return null;
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            pendingLocalImagePath = dest.getAbsolutePath();
            return pendingLocalImagePath;
        } catch (IOException e) {
            return null;
        }
    }

    private static String text(com.google.android.material.textfield.TextInputEditText edt) {
        return edt.getText() != null ? edt.getText().toString().trim() : "";
    }

    private static int parseDt(com.google.android.material.textfield.TextInputEditText edt) {
        String raw = text(edt);
        if (raw.isEmpty()) {
            return 0;
        }
        try {
            return PriceUtils.dtToMillimes(Double.parseDouble(raw.replace(',', '.')));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static int parseInt(com.google.android.material.textfield.TextInputEditText edt, int def) {
        String raw = text(edt);
        if (raw.isEmpty()) {
            return def;
        }
        try {
            return Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
