package com.esb.cosmetiqueshop.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.esb.cosmetiqueshop.data.dao.OrderDao;
import com.esb.cosmetiqueshop.data.dao.ProductDao;
import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.data.db.DbContract;
import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.model.UserRole;
import com.esb.cosmetiqueshop.utils.AppExecutors;

import java.util.Calendar;
import java.util.Locale;

public class AdminDashboardRepository {

    public static class DashboardStats {
        public final int totalOrders;
        public final int pendingOrders;
        public final int activeProducts;
        public final int lowStockProducts;
        public final int totalClients;
        public final int revenueMillimes;
        public final float[] revenueLast7Days;
        public final float fulfillmentPercent;
        public final float stockHealthPercent;
        public final float[] statusCounts;
        public final String[] statusLabels;
        public final int deliveredOrders;
        public final float averageTicketMillimes;

        public DashboardStats(int totalOrders, int pendingOrders, int activeProducts,
                              int lowStockProducts, int totalClients, int revenueMillimes,
                              float[] revenueLast7Days, float fulfillmentPercent,
                              float stockHealthPercent, float[] statusCounts, String[] statusLabels,
                              int deliveredOrders, float averageTicketMillimes) {
            this.totalOrders = totalOrders;
            this.pendingOrders = pendingOrders;
            this.activeProducts = activeProducts;
            this.lowStockProducts = lowStockProducts;
            this.totalClients = totalClients;
            this.revenueMillimes = revenueMillimes;
            this.revenueLast7Days = revenueLast7Days;
            this.fulfillmentPercent = fulfillmentPercent;
            this.stockHealthPercent = stockHealthPercent;
            this.statusCounts = statusCounts;
            this.statusLabels = statusLabels;
            this.deliveredOrders = deliveredOrders;
            this.averageTicketMillimes = averageTicketMillimes;
        }
    }

    public interface StatsCallback {
        void onResult(DashboardStats stats);
    }

    private static final int LOW_STOCK_THRESHOLD = 15;

    private final DatabaseHelper dbHelper;
    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final AppExecutors executors;

    public AdminDashboardRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
        orderDao = new OrderDao(dbHelper);
        productDao = new ProductDao(dbHelper);
        executors = AppExecutors.getInstance();
    }

    public void loadStats(StatsCallback callback) {
        executors.diskIO().execute(() -> {
            int clients = 0;
            int revenue = 0;
            int delivered = 0;
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            try (Cursor c = db.rawQuery(
                    "SELECT COUNT(*) FROM " + DbContract.Users.TABLE
                            + " WHERE " + DbContract.Users.COL_ROLE + "=?",
                    new String[]{UserRole.CLIENT.name()})) {
                if (c.moveToFirst()) {
                    clients = c.getInt(0);
                }
            }
            try (Cursor c = db.rawQuery(
                    "SELECT COALESCE(SUM(" + DbContract.Orders.COL_TOTAL_MILLIMES + "),0), COUNT(*) FROM "
                            + DbContract.Orders.TABLE + " WHERE " + DbContract.Orders.COL_STATUS + "=?",
                    new String[]{OrderStatus.LIVREE.name()})) {
                if (c.moveToFirst()) {
                    revenue = c.getInt(0);
                    delivered = c.getInt(1);
                }
            }

            int totalOrders = orderDao.countAll();
            int pending = orderDao.countByStatus(OrderStatus.EN_ATTENTE);
            int activeProducts = productDao.countActive();
            int lowStock = productDao.countLowStock(LOW_STOCK_THRESHOLD);

            float[] last7 = loadRevenueLast7Days(db);
            float fulfillment = totalOrders == 0 ? 0f : (100f * delivered / totalOrders);
            float stockHealth = activeProducts == 0 ? 100f
                    : Math.max(0f, 100f * (activeProducts - lowStock) / activeProducts);

            String[] labels = new String[]{"Attente", "Prépa.", "Livraison", "Livrée"};
            float[] statusCounts = new float[]{
                    orderDao.countByStatus(OrderStatus.EN_ATTENTE),
                    orderDao.countByStatus(OrderStatus.EN_PREPARATION),
                    orderDao.countByStatus(OrderStatus.EN_LIVRAISON),
                    delivered
            };
            float avgTicket = delivered == 0 ? 0f : (float) revenue / delivered;

            DashboardStats stats = new DashboardStats(
                    totalOrders, pending, activeProducts, lowStock, clients, revenue,
                    last7, fulfillment, stockHealth, statusCounts, labels, delivered, avgTicket
            );
            executors.mainThread().execute(() -> callback.onResult(stats));
        });
    }

    private float[] loadRevenueLast7Days(SQLiteDatabase db) {
        float[] days = new float[7];
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -6);
        long start = cal.getTimeInMillis();

        try (Cursor c = db.rawQuery(
                "SELECT " + DbContract.Orders.COL_CREATED_AT + "," + DbContract.Orders.COL_TOTAL_MILLIMES
                        + " FROM " + DbContract.Orders.TABLE
                        + " WHERE " + DbContract.Orders.COL_STATUS + "=? AND "
                        + DbContract.Orders.COL_CREATED_AT + ">=?",
                new String[]{OrderStatus.LIVREE.name(), String.valueOf(start)})) {
            while (c.moveToNext()) {
                long ts = c.getLong(0);
                int amount = c.getInt(1);
                int idx = (int) ((ts - start) / 86400000L);
                if (idx >= 0 && idx < 7) {
                    days[idx] += amount;
                }
            }
        }
        return days;
    }
}
