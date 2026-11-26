package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;

import com.esb.cosmetiqueshop.data.dao.NotificationDao;
import com.esb.cosmetiqueshop.data.dao.UserDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.model.NotificationItem;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.model.User;
import com.esb.cosmetiqueshop.model.UserRole;
import com.esb.cosmetiqueshop.notifications.NotificationHelper;
import com.esb.cosmetiqueshop.utils.AppExecutors;
import com.esb.cosmetiqueshop.utils.OrderStatusUi;

import java.util.List;

public class NotificationRepository {

    public interface NotificationsCallback {
        void onResult(List<NotificationItem> items);
    }

    public interface CountCallback {
        void onResult(int count);
    }

    public interface LongCallback {
        void onResult(long id);
    }

    private final NotificationDao notificationDao;
    private final UserDao userDao;
    private final Context appContext;
    private final AppExecutors executors;

    public NotificationRepository(Context context) {
        appContext = context.getApplicationContext();
        DatabaseHelper helper = new DatabaseHelper(appContext);
        notificationDao = new NotificationDao(helper);
        userDao = new UserDao(helper);
        executors = AppExecutors.getInstance();
    }

    public void loadForUser(long userId, NotificationsCallback callback) {
        executors.diskIO().execute(() -> {
            List<NotificationItem> list = notificationDao.findByUser(userId);
            executors.mainThread().execute(() -> callback.onResult(list));
        });
    }

    public void unreadCount(long userId, CountCallback callback) {
        executors.diskIO().execute(() -> {
            int n = notificationDao.countUnread(userId);
            executors.mainThread().execute(() -> callback.onResult(n));
        });
    }

    public void markRead(long notificationId, Runnable onDone) {
        executors.diskIO().execute(() -> {
            notificationDao.markRead(notificationId);
            if (onDone != null) {
                executors.mainThread().execute(onDone);
            }
        });
    }

    public void push(NotificationItem item, LongCallback callback) {
        executors.diskIO().execute(() -> {
            long id = notificationDao.insert(item);
            NotificationHelper.show(appContext, (int) (id % Integer.MAX_VALUE), item.getTitle(), item.getBody());
            if (callback != null) {
                executors.mainThread().execute(() -> callback.onResult(id));
            }
        });
    }

    /** Nouvelle commande → tous les admins. */
    public void notifyAdminsNewOrder(Order order) {
        executors.diskIO().execute(() -> {
            String title = appContext.getString(com.esb.cosmetiqueshop.R.string.admin_orders);
            String body = appContext.getString(com.esb.cosmetiqueshop.R.string.order_number, order.getId());
            for (User admin : userDao.findByRole(UserRole.ADMIN)) {
                NotificationItem item = new NotificationItem();
                item.setUserId(admin.getId());
                item.setTitle(title);
                item.setBody(body);
                item.setType("ORDER_NEW");
                item.setReferenceId(order.getId());
                pushSync(item);
            }
        });
    }

    /** Changement statut → client propriétaire. */
    public void notifyClientOrderStatus(Order order, OrderStatus newStatus) {
        executors.diskIO().execute(() -> {
            NotificationItem item = new NotificationItem();
            item.setUserId(order.getUserId());
            item.setTitle(appContext.getString(com.esb.cosmetiqueshop.R.string.orders_title));
            item.setBody(appContext.getString(com.esb.cosmetiqueshop.R.string.order_status,
                    OrderStatusUi.label(appContext, newStatus)));
            item.setType("ORDER_STATUS");
            item.setReferenceId(order.getId());
            pushSync(item);
        });
    }

    /** Nouveau message → destinataire(s). */
    public void notifyMessage(long recipientUserId, String preview, long conversationId) {
        executors.diskIO().execute(() -> {
            NotificationItem item = new NotificationItem();
            item.setUserId(recipientUserId);
            item.setTitle(appContext.getString(com.esb.cosmetiqueshop.R.string.messages_title));
            item.setBody(preview);
            item.setType("MESSAGE");
            item.setReferenceId(conversationId);
            pushSync(item);
        });
    }

    public void notifyAdminsMessage(String preview, long conversationId) {
        executors.diskIO().execute(() -> {
            for (User admin : userDao.findByRole(UserRole.ADMIN)) {
                notifyMessage(admin.getId(), preview, conversationId);
            }
        });
    }

    private void pushSync(NotificationItem item) {
        long id = notificationDao.insert(item);
        NotificationHelper.show(appContext, (int) (id % Integer.MAX_VALUE), item.getTitle(), item.getBody());
    }
}