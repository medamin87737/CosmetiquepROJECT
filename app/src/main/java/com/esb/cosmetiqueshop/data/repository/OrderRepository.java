package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.OrderDao;
import com.esb.cosmetiqueshop.data.dao.ProductDao;
import com.esb.cosmetiqueshop.data.dao.SettingsDao;
import com.esb.cosmetiqueshop.data.dao.UserDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.model.OrderStatusHistory;
import com.esb.cosmetiqueshop.model.Product;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.utils.AppExecutors;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderRepository {

    public interface OrderCallback {
        void onSuccess(Order order);

        void onFailure(String message);
    }

    public interface OrdersCallback {
        void onResult(List<Order> orders);
    }

    public interface HistoryCallback {
        void onResult(List<OrderStatusHistory> history);
    }

    private final Context appContext;
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final SettingsDao settingsDao;
    private final UserDao userDao;
    private final DatabaseHelper dbHelper;
    private final AppExecutors executors;

    public OrderRepository(Context context) {
        appContext = context.getApplicationContext();
        dbHelper = new DatabaseHelper(context);
        orderDao = new OrderDao(dbHelper);
        productDao = new ProductDao(dbHelper);
        settingsDao = new SettingsDao(dbHelper);
        userDao = new UserDao(dbHelper);
        executors = AppExecutors.getInstance();
    }

    public void createFromCart(long userId, List<OrderItem> items, String address, String phone,
                               String paymentMethod, boolean expressDelivery, OrderCallback callback) {
        executors.diskIO().execute(() -> {
            if (items == null || items.isEmpty()) {
                postFailure(callback, "Panier vide.");
                return;
            }
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                int subtotal = 0;
                List<OrderItem> validated = new ArrayList<>();
                for (OrderItem item : items) {
                    Product p = productDao.findByIdInTransaction(db, item.getProductId());
                    if (p == null || !p.isActive() || p.getStock() < item.getQuantity()) {
                        throw new IllegalStateException("Stock insuffisant pour " + item.getProductName());
                    }
                    int unit = p.getEffectivePriceMillimes();
                    item.setUnitPriceMillimes(unit);
                    item.setProductName(p.getName());
                    subtotal += unit * item.getQuantity();
                    validated.add(item);
                }
                int delivery = computeDeliveryMillimes(subtotal, expressDelivery);
                Order order = new Order();
                order.setUserId(userId);
                order.setStatus(OrderStatus.EN_ATTENTE);
                order.setSubtotalMillimes(subtotal);
                order.setDeliveryMillimes(delivery);
                order.setTotalMillimes(subtotal + delivery);
                order.setDeliveryAddress(address);
                order.setDeliveryPhone(phone);
                order.setPaymentMethod(paymentMethod);

                long orderId = orderDao.insertOrder(db, order);
                for (OrderItem item : validated) {
                    item.setOrderId(orderId);
                    orderDao.insertOrderItem(db, orderId, item);
                    if (!productDao.adjustStock(db, item.getProductId(), -item.getQuantity())) {
                        throw new IllegalStateException("Erreur décrément stock.");
                    }
                }
                OrderStatusHistory history = new OrderStatusHistory();
                history.setOrderId(orderId);
                history.setNewStatus(OrderStatus.EN_ATTENTE);
                history.setChangedByUserId(userId);
                history.setCreatedAt(DateUtils.now());
                orderDao.insertStatusHistory(db, history);

                db.setTransactionSuccessful();
                Order saved = orderDao.findById(orderId);
                new NotificationRepository(appContext).notifyAdminsNewOrder(saved);
                postSuccess(callback, saved);
            } catch (Exception e) {
                postFailure(callback, e.getMessage() != null ? e.getMessage() : "Commande impossible.");
            } finally {
                db.endTransaction();
            }
        });
    }

    public void updateStatus(long orderId, OrderStatus newStatus,
                             long changedByUserId, String note, OrderCallback callback) {
        updateStatusInternal(orderId, newStatus, changedByUserId, note, false, callback);
    }

    /** Mise à jour admin : choix libre préparation / livrée / annulée / échec. */
    public void updateStatusAsAdmin(long orderId, OrderStatus newStatus,
                                    long changedByUserId, String note, OrderCallback callback) {
        updateStatusInternal(orderId, newStatus, changedByUserId, note, true, callback);
    }

    private void updateStatusInternal(long orderId, OrderStatus newStatus,
                                      long changedByUserId, String note, boolean adminMode,
                                      OrderCallback callback) {
        executors.diskIO().execute(() -> {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                postFailure(callback, "Commande introuvable.");
                return;
            }
            OrderStatus current = order.getStatus();
            boolean allowed = adminMode
                    ? current.canAdminSetTo(newStatus)
                    : current.canTransitionTo(newStatus);
            if (!allowed) {
                postFailure(callback, "Transition de statut non autorisée.");
                return;
            }
            android.database.sqlite.SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                if (newStatus == OrderStatus.ANNULEE && current != OrderStatus.ANNULEE) {
                    restoreStockOnce(db, order);
                } else if (current == OrderStatus.ANNULEE && newStatus != OrderStatus.ANNULEE) {
                    consumeStockAgain(db, order);
                }
                orderDao.updateStatus(db, orderId, newStatus);
                OrderStatusHistory h = new OrderStatusHistory();
                h.setOrderId(orderId);
                h.setOldStatus(current);
                h.setNewStatus(newStatus);
                h.setChangedByUserId(changedByUserId);
                h.setNote(note);
                h.setCreatedAt(DateUtils.now());
                orderDao.insertStatusHistory(db, h);
                db.setTransactionSuccessful();
                Order updated = orderDao.findById(orderId);
                hydrateClientName(updated);
                new NotificationRepository(appContext).notifyClientOrderStatus(updated, newStatus);
                postSuccess(callback, updated);
            } catch (Exception e) {
                postFailure(callback, e.getMessage() != null ? e.getMessage() : "Mise à jour impossible.");
            } finally {
                db.endTransaction();
            }
        });
    }

    /** Restaure le stock une seule fois à l'annulation. */
    private void restoreStockOnce(android.database.sqlite.SQLiteDatabase db, Order order) {
        if (orderDao.isStockRestored(db, order.getId())) {
            return;
        }
        for (OrderItem item : order.getItems()) {
            productDao.adjustStock(db, item.getProductId(), item.getQuantity());
        }
        OrderStatusHistory marker = new OrderStatusHistory();
        marker.setOrderId(order.getId());
        marker.setOldStatus(order.getStatus());
        marker.setNewStatus(OrderStatus.ANNULEE);
        marker.setChangedByUserId(0);
        marker.setNote("STOCK_RESTORED");
        marker.setCreatedAt(DateUtils.now());
        orderDao.insertStatusHistory(db, marker);
    }

    /** Reprend le stock après réouverture d’une commande annulée. */
    private void consumeStockAgain(android.database.sqlite.SQLiteDatabase db, Order order) {
        for (OrderItem item : order.getItems()) {
            if (!productDao.adjustStock(db, item.getProductId(), -item.getQuantity())) {
                throw new IllegalStateException("Stock insuffisant pour " + item.getProductName());
            }
        }
        orderDao.clearStockRestoredMarker(db, order.getId());
    }

    private void hydrateClientName(Order order) {
        if (order == null) {
            return;
        }
        User user = userDao.findById(order.getUserId());
        if (user != null) {
            order.setClientName(user.getFullName());
        }
    }

    private int computeDeliveryMillimes(int subtotal, boolean express) {
        int freeFrom = settingsDao.getInt(DbContract.AppSettings.KEY_FREE_FROM, 150000);
        if (subtotal >= freeFrom) {
            return 0;
        }
        if (express) {
            return settingsDao.getInt(DbContract.AppSettings.KEY_DELIVERY_EXPRESS, 12000);
        }
        return settingsDao.getInt(DbContract.AppSettings.KEY_DELIVERY_STANDARD, 7000);
    }

    public void getOrdersForUser(long userId, OrdersCallback callback) {
        executors.diskIO().execute(() -> {
            List<Order> list = orderDao.findByUser(userId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getAllOrders(OrdersCallback callback) {
        executors.diskIO().execute(() -> {
            List<Order> list = orderDao.findAll();
            for (Order order : list) {
                hydrateClientName(order);
            }
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    /** Colis livrés avec nom client hydraté. */
    public void getDeliveredOrders(OrdersCallback callback) {
        executors.diskIO().execute(() -> {
            List<Order> list = orderDao.findByStatus(OrderStatus.LIVREE);
            for (Order order : list) {
                hydrateClientName(order);
            }
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void getOrderHistory(long orderId, HistoryCallback callback) {
        executors.diskIO().execute(() -> {
            List<OrderStatusHistory> list = orderDao.findHistory(orderId);
            List<OrderStatusHistory> filtered = new ArrayList<>();
            for (OrderStatusHistory h : list) {
                if (!"STOCK_RESTORED".equals(h.getNote())) {
                    filtered.add(h);
                }
            }
            executors.mainThread().execute(() -> callback.onResult(filtered));
        });
    }

    public void getOrder(long orderId, OrderCallback callback) {
        executors.diskIO().execute(() -> {
            Order o = orderDao.findById(orderId);
            if (o != null) {
                hydrateClientName(o);
                postSuccess(callback, o);
            } else {
                postFailure(callback, "Introuvable.");
            }
        });
    }

    private void postSuccess(OrderCallback callback, Order order) {
        executors.mainThread().execute(() -> callback.onSuccess(order));
    }

    private void postFailure(OrderCallback callback, String msg) {
        executors.mainThread().execute(() -> callback.onFailure(msg));
    }
}