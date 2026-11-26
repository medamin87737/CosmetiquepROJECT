package com.esb.cosmetiqueshop.data.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.Order;
import com.esb.cosmetiqueshop.model.OrderItem;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.model.OrderStatusHistory;
import com.esb.cosmetiqueshop.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class OrderDao {

    private final DatabaseHelper dbHelper;

    public OrderDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Order findById(long orderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Orders.TABLE, null,
                DbContract.Orders.COL_ID + "=?",
                new String[]{String.valueOf(orderId)}, null, null, null)) {
            if (!c.moveToFirst()) {
                return null;
            }
            Order order = mapOrder(c);
            order.setItems(findItemsForOrder(orderId));
            return order;
        }
    }

    public List<Order> findByUser(long userId) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Orders.TABLE, null,
                DbContract.Orders.COL_USER_ID + "=?",
                new String[]{String.valueOf(userId)}, null, null,
                DbContract.Orders.COL_CREATED_AT + " DESC")) {
            while (c.moveToNext()) {
                Order o = mapOrder(c);
                o.setItems(findItemsForOrder(o.getId()));
                list.add(o);
            }
        }
        return list;
    }

    public List<Order> findAll() {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Orders.TABLE, null, null, null, null, null,
                DbContract.Orders.COL_CREATED_AT + " DESC")) {
            while (c.moveToNext()) {
                Order o = mapOrder(c);
                o.setItems(findItemsForOrder(o.getId()));
                list.add(o);
            }
        }
        return list;
    }

    public List<Order> findByStatus(OrderStatus status) {
        List<Order> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.Orders.TABLE, null,
                DbContract.Orders.COL_STATUS + "=?",
                new String[]{status.name()}, null, null,
                DbContract.Orders.COL_CREATED_AT + " DESC")) {
            while (c.moveToNext()) {
                Order o = mapOrder(c);
                o.setItems(findItemsForOrder(o.getId()));
                list.add(o);
            }
        }
        return list;
    }

    public long insertOrder(SQLiteDatabase db, Order order) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Orders.COL_USER_ID, order.getUserId());
        v.put(DbContract.Orders.COL_STATUS, order.getStatus().name());
        if (order.getDeliveryAgentId() != null) {
            v.put(DbContract.Orders.COL_DELIVERY_AGENT_ID, order.getDeliveryAgentId());
        }
        v.put(DbContract.Orders.COL_SUBTOTAL_MILLIMES, order.getSubtotalMillimes());
        v.put(DbContract.Orders.COL_DELIVERY_MILLIMES, order.getDeliveryMillimes());
        v.put(DbContract.Orders.COL_TOTAL_MILLIMES, order.getTotalMillimes());
        v.put(DbContract.Orders.COL_DELIVERY_ADDRESS, order.getDeliveryAddress());
        v.put(DbContract.Orders.COL_DELIVERY_PHONE, order.getDeliveryPhone());
        v.put(DbContract.Orders.COL_PAYMENT_METHOD, order.getPaymentMethod());
        long now = DateUtils.now();
        v.put(DbContract.Orders.COL_CREATED_AT, now);
        v.put(DbContract.Orders.COL_UPDATED_AT, now);
        return db.insert(DbContract.Orders.TABLE, null, v);
    }

    public void insertOrderItem(SQLiteDatabase db, long orderId, OrderItem item) {
        ContentValues v = new ContentValues();
        v.put(DbContract.OrderItems.COL_ORDER_ID, orderId);
        v.put(DbContract.OrderItems.COL_PRODUCT_ID, item.getProductId());
        v.put(DbContract.OrderItems.COL_PRODUCT_NAME, item.getProductName());
        v.put(DbContract.OrderItems.COL_QUANTITY, item.getQuantity());
        v.put(DbContract.OrderItems.COL_UNIT_PRICE_MILLIMES, item.getUnitPriceMillimes());
        db.insert(DbContract.OrderItems.TABLE, null, v);
    }

    public boolean updateStatus(SQLiteDatabase db, long orderId, OrderStatus status, Long agentId) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Orders.COL_STATUS, status.name());
        v.put(DbContract.Orders.COL_UPDATED_AT, DateUtils.now());
        if (agentId != null) {
            v.put(DbContract.Orders.COL_DELIVERY_AGENT_ID, agentId);
        }
        int rows = db.update(DbContract.Orders.TABLE, v,
                DbContract.Orders.COL_ID + "=?", new String[]{String.valueOf(orderId)});
        return rows > 0;
    }

    public void insertStatusHistory(SQLiteDatabase db, OrderStatusHistory history) {
        ContentValues v = new ContentValues();
        v.put(DbContract.OrderStatusHistoryTable.COL_ORDER_ID, history.getOrderId());
        if (history.getOldStatus() != null) {
            v.put(DbContract.OrderStatusHistoryTable.COL_OLD_STATUS, history.getOldStatus().name());
        }
        v.put(DbContract.OrderStatusHistoryTable.COL_NEW_STATUS, history.getNewStatus().name());
        v.put(DbContract.OrderStatusHistoryTable.COL_CHANGED_BY_USER_ID, history.getChangedByUserId());
        v.put(DbContract.OrderStatusHistoryTable.COL_NOTE, history.getNote());
        v.put(DbContract.OrderStatusHistoryTable.COL_CREATED_AT, history.getCreatedAt());
        db.insert(DbContract.OrderStatusHistoryTable.TABLE, null, v);
    }

    /** Commande livrée contenant le produit pour cet utilisateur (avis client). */
    public Long findDeliveredOrderIdForProduct(long userId, long productId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT o." + DbContract.Orders.COL_ID + " FROM " + DbContract.Orders.TABLE + " o "
                + "INNER JOIN " + DbContract.OrderItems.TABLE + " i ON o."
                + DbContract.Orders.COL_ID + " = i." + DbContract.OrderItems.COL_ORDER_ID
                + " WHERE o." + DbContract.Orders.COL_USER_ID + "=? AND o."
                + DbContract.Orders.COL_STATUS + "=? AND i."
                + DbContract.OrderItems.COL_PRODUCT_ID + "=?"
                + " ORDER BY o." + DbContract.Orders.COL_CREATED_AT + " DESC LIMIT 1";
        try (Cursor c = db.rawQuery(sql, new String[]{
                String.valueOf(userId), OrderStatus.LIVREE.name(), String.valueOf(productId)})) {
            if (c.moveToFirst()) {
                return c.getLong(0);
            }
        }
        return null;
    }

    public List<OrderStatusHistory> findHistory(long orderId) {
        List<OrderStatusHistory> list = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.query(DbContract.OrderStatusHistoryTable.TABLE, null,
                DbContract.OrderStatusHistoryTable.COL_ORDER_ID + "=?",
                new String[]{String.valueOf(orderId)}, null, null,
                DbContract.OrderStatusHistoryTable.COL_CREATED_AT)) {
            while (c.moveToNext()) {
                list.add(mapHistory(c));
            }
        }
        return list;
    }

    public int countByStatus(OrderStatus status) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Orders.TABLE
                + " WHERE " + DbContract.Orders.COL_STATUS + "=?",
                new String[]{status.name()})) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public int countAll() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + DbContract.Orders.TABLE, null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    public boolean isStockRestored(SQLiteDatabase db, long orderId) {
        try (Cursor c = db.query(DbContract.OrderStatusHistoryTable.TABLE,
                new String[]{DbContract.OrderStatusHistoryTable.COL_NOTE},
                DbContract.OrderStatusHistoryTable.COL_ORDER_ID + "=? AND "
                        + DbContract.OrderStatusHistoryTable.COL_NOTE + "=?",
                new String[]{String.valueOf(orderId), "STOCK_RESTORED"},
                null, null, null)) {
            return c.moveToFirst();
        }
    }

    private List<OrderItem> findItemsForOrder(long orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT i.*, (SELECT pi." + DbContract.ProductImages.COL_IMAGE_URL
                + " FROM " + DbContract.ProductImages.TABLE + " pi WHERE pi."
                + DbContract.ProductImages.COL_PRODUCT_ID + " = i."
                + DbContract.OrderItems.COL_PRODUCT_ID
                + " ORDER BY pi." + DbContract.ProductImages.COL_SORT_ORDER
                + " ASC LIMIT 1) AS product_image_url FROM "
                + DbContract.OrderItems.TABLE + " i WHERE i."
                + DbContract.OrderItems.COL_ORDER_ID + "=?";
        try (Cursor c = db.rawQuery(sql, new String[]{String.valueOf(orderId)})) {
            while (c.moveToNext()) {
                items.add(mapItem(c));
            }
        }
        return items;
    }

    /** Supprime le marqueur de restauration stock (réouverture après annulation). */
    public void clearStockRestoredMarker(SQLiteDatabase db, long orderId) {
        db.delete(DbContract.OrderStatusHistoryTable.TABLE,
                DbContract.OrderStatusHistoryTable.COL_ORDER_ID + "=? AND "
                        + DbContract.OrderStatusHistoryTable.COL_NOTE + "=?",
                new String[]{String.valueOf(orderId), "STOCK_RESTORED"});
    }

    private Order mapOrder(Cursor c) {
        Order o = new Order();
        o.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.Orders.COL_ID)));
        o.setUserId(c.getLong(c.getColumnIndexOrThrow(DbContract.Orders.COL_USER_ID)));
        o.setStatus(OrderStatus.fromString(c.getString(c.getColumnIndexOrThrow(DbContract.Orders.COL_STATUS))));
        int agentIdx = c.getColumnIndex(DbContract.Orders.COL_DELIVERY_AGENT_ID);
        if (agentIdx >= 0 && !c.isNull(agentIdx)) {
            o.setDeliveryAgentId(c.getLong(agentIdx));
        }
        o.setSubtotalMillimes(c.getInt(c.getColumnIndexOrThrow(DbContract.Orders.COL_SUBTOTAL_MILLIMES)));
        o.setDeliveryMillimes(c.getInt(c.getColumnIndexOrThrow(DbContract.Orders.COL_DELIVERY_MILLIMES)));
        o.setTotalMillimes(c.getInt(c.getColumnIndexOrThrow(DbContract.Orders.COL_TOTAL_MILLIMES)));
        o.setDeliveryAddress(c.getString(c.getColumnIndexOrThrow(DbContract.Orders.COL_DELIVERY_ADDRESS)));
        o.setDeliveryPhone(c.getString(c.getColumnIndexOrThrow(DbContract.Orders.COL_DELIVERY_PHONE)));
        o.setPaymentMethod(c.getString(c.getColumnIndexOrThrow(DbContract.Orders.COL_PAYMENT_METHOD)));
        o.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Orders.COL_CREATED_AT)));
        o.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.Orders.COL_UPDATED_AT)));
        return o;
    }

    private OrderItem mapItem(Cursor c) {
        OrderItem item = new OrderItem();
        item.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.OrderItems.COL_ID)));
        item.setOrderId(c.getLong(c.getColumnIndexOrThrow(DbContract.OrderItems.COL_ORDER_ID)));
        item.setProductId(c.getLong(c.getColumnIndexOrThrow(DbContract.OrderItems.COL_PRODUCT_ID)));
        item.setProductName(c.getString(c.getColumnIndexOrThrow(DbContract.OrderItems.COL_PRODUCT_NAME)));
        item.setQuantity(c.getInt(c.getColumnIndexOrThrow(DbContract.OrderItems.COL_QUANTITY)));
        item.setUnitPriceMillimes(c.getInt(c.getColumnIndexOrThrow(DbContract.OrderItems.COL_UNIT_PRICE_MILLIMES)));
        int imgIdx = c.getColumnIndex("product_image_url");
        if (imgIdx >= 0 && !c.isNull(imgIdx)) {
            item.setProductImageUrl(c.getString(imgIdx));
        }
        return item;
    }

    private OrderStatusHistory mapHistory(Cursor c) {
        OrderStatusHistory h = new OrderStatusHistory();
        h.setId(c.getLong(c.getColumnIndexOrThrow(DbContract.OrderStatusHistoryTable.COL_ID)));
        h.setOrderId(c.getLong(c.getColumnIndexOrThrow(DbContract.OrderStatusHistoryTable.COL_ORDER_ID)));
        String old = c.getString(c.getColumnIndexOrThrow(DbContract.OrderStatusHistoryTable.COL_OLD_STATUS));
        if (old != null) {
            h.setOldStatus(OrderStatus.fromString(old));
        }
        h.setNewStatus(OrderStatus.fromString(
                c.getString(c.getColumnIndexOrThrow(DbContract.OrderStatusHistoryTable.COL_NEW_STATUS))));
        h.setChangedByUserId(c.getLong(c.getColumnIndexOrThrow(
                DbContract.OrderStatusHistoryTable.COL_CHANGED_BY_USER_ID)));
        h.setNote(c.getString(c.getColumnIndexOrThrow(DbContract.OrderStatusHistoryTable.COL_NOTE)));
        h.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(DbContract.OrderStatusHistoryTable.COL_CREATED_AT)));
        return h;
    }
}