package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.CategoryDao;
import com.esb.cosmetiqueshop.data.dao.FavoriteDao;
import com.esb.cosmetiqueshop.data.dao.OrderDao;
import com.esb.cosmetiqueshop.data.dao.ProductDao;
import com.esb.cosmetiqueshop.data.dao.ReviewDao;
import com.esb.cosmetiqueshop.data.dao.SettingsDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Category;
import com.esb.cosmetiqueshop.model.Favorite;
import com.esb.cosmetiqueshop.model.Review;
import com.esb.cosmetiqueshop.utils.AppExecutors;

import java.util.List;

/**
 * Catalogue : catégories, favoris, avis et paramètres livraison.
 */
public class CatalogRepository {

    public interface CategoriesCallback {
        void onResult(List<Category> categories);
    }

    public interface FavoritesCallback {
        void onResult(List<Favorite> favorites);
    }

    public interface ReviewsCallback {
        void onResult(List<Review> reviews);
    }

    public interface DeliveryFeesCallback {
        void onResult(int standardMillimes, int freeFromMillimes, int expressMillimes);
    }

    private final CategoryDao categoryDao;
    private final FavoriteDao favoriteDao;
    private final ReviewDao reviewDao;
    private final SettingsDao settingsDao;
    private final ProductDao productDao;
    private final OrderDao orderDao;
    private final AppExecutors executors;

    public interface CategoryCallback {
        void onResult(Category category);

        void onFailure(String message);
    }

    public interface ReviewSubmitCallback {
        void onSuccess();

        void onFailure(String message);
    }

    public CatalogRepository(Context context) {
        DatabaseHelper helper = new DatabaseHelper(context);
        categoryDao = new CategoryDao(helper);
        favoriteDao = new FavoriteDao(helper);
        reviewDao = new ReviewDao(helper);
        settingsDao = new SettingsDao(helper);
        productDao = new ProductDao(helper);
        orderDao = new OrderDao(helper);
        executors = AppExecutors.getInstance();
    }

    public void getCategories(CategoriesCallback callback) {
        executors.diskIO().execute(() -> {
            List<Category> list = categoryDao.findAllOrdered();
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getFavorites(long userId, FavoritesCallback callback) {
        executors.diskIO().execute(() -> {
            List<Favorite> list = favoriteDao.findByUser(userId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void toggleFavorite(long userId, long productId, CatalogRepository.BoolCallback callback) {
        executors.diskIO().execute(() -> {
            boolean nowFavorite;
            if (favoriteDao.isFavorite(userId, productId)) {
                favoriteDao.remove(userId, productId);
                nowFavorite = false;
            } else {
                favoriteDao.add(userId, productId);
                nowFavorite = true;
            }
            boolean finalFav = nowFavorite;
            executors.mainThread().execute(() -> callback.onResult(finalFav));
        });
    }

    public void getReviews(long productId, ReviewsCallback callback) {
        executors.diskIO().execute(() -> {
            List<Review> list = reviewDao.findByProduct(productId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getDeliveryFees(DeliveryFeesCallback callback) {
        executors.diskIO().execute(() -> {
            int standard = settingsDao.getInt(DbContract.AppSettings.KEY_DELIVERY_STANDARD, 7000);
            int freeFrom = settingsDao.getInt(DbContract.AppSettings.KEY_FREE_FROM, 150000);
            int express = settingsDao.getInt(DbContract.AppSettings.KEY_DELIVERY_EXPRESS, 12000);
            executors.mainThread().execute(() -> callback.onResult(standard, freeFrom, express));
        });
    }

    public interface BoolCallback {
        void onResult(boolean value);
    }

    public void saveCategory(Category category, CategoryCallback callback) {
        executors.diskIO().execute(() -> {
            if (category.getId() > 0) {
                categoryDao.update(category);
            } else {
                long id = categoryDao.insert(category);
                category.setId(id);
            }
            Category saved = categoryDao.findById(category.getId());
            executors.mainThread().execute(() -> callback.onResult(saved));
        });
    }

    public interface DeleteCallback {
        void onSuccess();

        void onFailure(String message);
    }

    /**
     * Supprime une catégorie. Les produits liés sont déplacés vers une autre catégorie.
     * Impossible s’il ne reste qu’une seule catégorie.
     */
    public void deleteCategory(long categoryId, DeleteCallback callback) {
        executors.diskIO().execute(() -> {
            Category other = categoryDao.findFirstOther(categoryId);
            if (other == null) {
                executors.mainThread().execute(() ->
                        callback.onFailure("Impossible : il doit rester au moins une catégorie."));
                return;
            }
            int linked = productDao.countByCategory(categoryId);
            if (linked > 0) {
                productDao.reassignCategory(categoryId, other.getId());
            }
            boolean ok = categoryDao.delete(categoryId);
            if (ok) {
                executors.mainThread().execute(callback::onSuccess);
            } else {
                executors.mainThread().execute(() ->
                        callback.onFailure("Suppression impossible."));
            }
        });
    }

    /** @deprecated préférer {@link #deleteCategory(long, DeleteCallback)} */
    public void deleteCategory(long categoryId, BoolCallback callback) {
        deleteCategory(categoryId, new DeleteCallback() {
            @Override
            public void onSuccess() {
                callback.onResult(true);
            }

            @Override
            public void onFailure(String message) {
                callback.onResult(false);
            }
        });
    }

    public void submitReview(long userId, long productId, int rating, String comment,
                             ReviewSubmitCallback callback) {
        executors.diskIO().execute(() -> {
            if (reviewDao.existsForUserAndProduct(userId, productId)) {
                postReviewFailure(callback, "Vous avez déjà noté ce produit.");
                return;
            }
            if (rating < 1 || rating > 6) {
                postReviewFailure(callback, "Note invalide (1 à 6 étoiles).");
                return;
            }
            Long orderId = orderDao.findDeliveredOrderIdForProduct(userId, productId);
            if (orderId == null) {
                postReviewFailure(callback, "Avis après livraison uniquement.");
                return;
            }
            Review review = new Review();
            review.setUserId(userId);
            review.setProductId(productId);
            review.setOrderId(orderId);
            review.setRating(rating);
            review.setComment(comment);
            review.setCreatedAt(com.esb.cosmetiqueshop.utils.DateUtils.now());
            long id = reviewDao.insert(review);
            if (id > 0) {
                executors.mainThread().execute(callback::onSuccess);
            } else {
                postReviewFailure(callback, "Publication impossible.");
            }
        });
    }

    private void postReviewFailure(ReviewSubmitCallback callback, String msg) {
        executors.mainThread().execute(() -> callback.onFailure(msg));
    }

    public ProductDao getProductDao() {
        return productDao;
    }
}