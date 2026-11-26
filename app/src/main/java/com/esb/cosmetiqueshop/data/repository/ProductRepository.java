package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.ProductDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.model.ProductImage;
import com.esb.cosmetiqueshop.utils.AppExecutors;

import java.util.List;

public class ProductRepository {

    public interface ListCallback {
        void onResult(List<Product> products);
    }

    public interface ProductCallback {
        void onResult(Product product);
    }

    public interface StringCallback {
        void onResult(String url);
    }

    public interface BrandsCallback {
        void onResult(List<String> brands);
    }

    private final ProductDao productDao;
    private final AppExecutors executors;

    public ProductRepository(Context context) {
        productDao = new ProductDao(new DatabaseHelper(context));
        executors = AppExecutors.getInstance();
    }

    public void getAllForAdmin(ListCallback callback) {
        executors.diskIO().execute(() -> {
            List<Product> list = productDao.findAllForAdmin();
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getAllActive(ListCallback callback) {
        executors.diskIO().execute(() -> {
            List<Product> list = productDao.findAllActive();
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getByCategory(long categoryId, ListCallback callback) {
        executors.diskIO().execute(() -> {
            List<Product> list = productDao.findByCategory(categoryId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getById(long id, ProductCallback callback) {
        executors.diskIO().execute(() -> {
            Product p = productDao.findById(id);
            executors.mainThread().execute(() -> callback.onResult(p));
        });
    }

    public void getDistinctBrands(BrandsCallback callback) {
        executors.diskIO().execute(() -> {
            List<String> brands = productDao.findDistinctBrands();
            executors.mainThread().execute(() -> callback.onResult(brands));
        });
    }

    public void search(String query, ListCallback callback) {
        executors.diskIO().execute(() -> {
            List<Product> list = productDao.searchByName(query);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void save(Product product, ProductCallback callback) {
        save(product, null, callback);
    }

    public void save(Product product, String primaryImageUrl, ProductCallback callback) {
        executors.diskIO().execute(() -> {
            if (product.getId() > 0) {
                productDao.update(product);
            } else {
                long id = productDao.insert(product);
                product.setId(id);
            }
            if (primaryImageUrl != null && !primaryImageUrl.trim().isEmpty()) {
                productDao.replacePrimaryImage(product.getId(), primaryImageUrl.trim());
            }
            Product saved = productDao.findById(product.getId());
            executors.mainThread().execute(() -> callback.onResult(saved));
        });
    }

    public void getPrimaryImageUrl(long productId, StringCallback callback) {
        executors.diskIO().execute(() -> {
            String url = productDao.findPrimaryImageUrl(productId);
            executors.mainThread().execute(() -> callback.onResult(url));
        });
    }

    public List<ProductImage> getImagesSync(long productId) {
        return productDao.findImagesForProduct(productId);
    }
}