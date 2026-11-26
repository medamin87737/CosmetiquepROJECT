package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.CartDao;
import com.esb.cosmetiqueshop.data.dao.ProductDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.CartItem;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.utils.AppExecutors;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {

    public interface CartCallback {
        void onResult(List<CartItem> items, int totalMillimes);
    }

    public interface BoolCallback {
        void onResult(boolean success);
    }

    private final CartDao cartDao;
    private final ProductDao productDao;
    private final AppExecutors executors;

    public CartRepository(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        cartDao = new CartDao(helper);
        productDao = new ProductDao(helper);
        executors = AppExecutors.getInstance();
    }

    public void loadCart(long userId, CartCallback callback) {
        executors.diskIO().execute(() -> {
            List<CartItem> raw = cartDao.findByUser(userId);
            List<CartItem> hydrated = new ArrayList<>();
            int total = 0;
            for (CartItem item : raw) {
                Product p = productDao.findById(item.getProductId());
                if (p != null && p.isActive()) {
                    item.setProduct(p);
                    hydrated.add(item);
                    total += item.getLineTotalMillimes();
                }
            }
            int finalTotal = total;
            executors.mainThread().execute(() -> callback.onResult(hydrated, finalTotal));
        });
    }

    public void setQuantity(long userId, long productId, int quantity, BoolCallback callback) {
        executors.diskIO().execute(() -> {
            boolean ok;
            if (quantity <= 0) {
                ok = cartDao.removeItem(userId, productId);
            } else {
                Product p = productDao.findById(productId);
                ok = p != null && p.getStock() >= quantity;
                if (ok) {
                    cartDao.upsertQuantity(userId, productId, quantity);
                }
            }
            boolean finalOk = ok;
            executors.mainThread().execute(() -> callback.onResult(finalOk));
        });
    }

    /** Ajoute +1 au panier (stock vérifié). */
    public void addOne(long userId, long productId, BoolCallback callback) {
        executors.diskIO().execute(() -> {
            Product p = productDao.findById(productId);
            if (p == null || !p.isActive() || p.getStock() <= 0) {
                executors.mainThread().execute(() -> callback.onResult(false));
                return;
            }
            int current = 0;
            for (CartItem item : cartDao.findByUser(userId)) {
                if (item.getProductId() == productId) {
                    current = item.getQuantity();
                    break;
                }
            }
            int next = current + 1;
            boolean ok = next <= p.getStock();
            if (ok) {
                cartDao.upsertQuantity(userId, productId, next);
            }
            boolean finalOk = ok;
            executors.mainThread().execute(() -> callback.onResult(finalOk));
        });
    }

    public void clear(long userId, BoolCallback callback) {
        executors.diskIO().execute(() -> {
            cartDao.clearForUser(userId);
            executors.mainThread().execute(() -> callback.onResult(true));
        });
    }
}