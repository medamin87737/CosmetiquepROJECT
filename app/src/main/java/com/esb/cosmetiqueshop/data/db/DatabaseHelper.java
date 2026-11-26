package com.esb.cosmetiqueshop.data.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.esb.cosmetiqueshop.model.OrderStatus;
import com.esb.cosmetiqueshop.model.UserRole;
import com.esb.cosmetiqueshop.utils.DateUtils;
import com.esb.cosmetiqueshop.utils.DemoSeedImages;
import com.esb.cosmetiqueshop.utils.PasswordHasher;

/**
 * Schéma SQLite complet + données de démonstration.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DB_NAME = "cosmetique_shop.db";
    public static final int DB_VERSION = 8;

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
        createIndexes(db);
        seedDemoData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropAllTables(db);
        onCreate(db);
    }

    private void dropAllTables(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Messages.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Conversations.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Notifications.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Reviews.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.OrderStatusHistoryTable.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.OrderItems.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Orders.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Favorites.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.CartItems.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.ProductImages.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Products.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Categories.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.DeliveryAgents.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.AppSettings.TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DbContract.Users.TABLE);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + DbContract.Users.TABLE + " ("
                + DbContract.Users.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Users.COL_EMAIL + " TEXT NOT NULL UNIQUE,"
                + DbContract.Users.COL_PASSWORD_HASH + " TEXT NOT NULL,"
                + DbContract.Users.COL_SALT + " TEXT NOT NULL,"
                + DbContract.Users.COL_FULL_NAME + " TEXT NOT NULL,"
                + DbContract.Users.COL_PHONE + " TEXT,"
                + DbContract.Users.COL_ROLE + " TEXT NOT NULL,"
                + DbContract.Users.COL_MUST_CHANGE_PASSWORD + " INTEGER NOT NULL DEFAULT 0,"
                + DbContract.Users.COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1,"
                + DbContract.Users.COL_CREATED_AT + " INTEGER NOT NULL"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Categories.TABLE + " ("
                + DbContract.Categories.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Categories.COL_NAME + " TEXT NOT NULL UNIQUE,"
                + DbContract.Categories.COL_DESCRIPTION + " TEXT,"
                + DbContract.Categories.COL_IMAGE_URL + " TEXT,"
                + DbContract.Categories.COL_SORT_ORDER + " INTEGER NOT NULL DEFAULT 0"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Products.TABLE + " ("
                + DbContract.Products.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Products.COL_CATEGORY_ID + " INTEGER NOT NULL,"
                + DbContract.Products.COL_NAME + " TEXT NOT NULL,"
                + DbContract.Products.COL_DESCRIPTION + " TEXT,"
                + DbContract.Products.COL_PRICE_MILLIMES + " INTEGER NOT NULL,"
                + DbContract.Products.COL_PROMO_PRICE_MILLIMES + " INTEGER NOT NULL DEFAULT 0,"
                + DbContract.Products.COL_STOCK + " INTEGER NOT NULL DEFAULT 0,"
                + DbContract.Products.COL_IS_ACTIVE + " INTEGER NOT NULL DEFAULT 1,"
                + DbContract.Products.COL_BRAND + " TEXT,"
                + DbContract.Products.COL_SKIN_TYPE + " TEXT,"
                + DbContract.Products.COL_INGREDIENTS + " TEXT,"
                + DbContract.Products.COL_TIPS + " TEXT,"
                + DbContract.Products.COL_ALERT_THRESHOLD + " INTEGER NOT NULL DEFAULT 10,"
                + DbContract.Products.COL_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Products.COL_CATEGORY_ID + ") REFERENCES "
                + DbContract.Categories.TABLE + "(" + DbContract.Categories.COL_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.ProductImages.TABLE + " ("
                + DbContract.ProductImages.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.ProductImages.COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + DbContract.ProductImages.COL_IMAGE_URL + " TEXT,"
                + DbContract.ProductImages.COL_SORT_ORDER + " INTEGER NOT NULL DEFAULT 0,"
                + "FOREIGN KEY(" + DbContract.ProductImages.COL_PRODUCT_ID + ") REFERENCES "
                + DbContract.Products.TABLE + "(" + DbContract.Products.COL_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.CartItems.TABLE + " ("
                + DbContract.CartItems.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.CartItems.COL_USER_ID + " INTEGER NOT NULL,"
                + DbContract.CartItems.COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + DbContract.CartItems.COL_QUANTITY + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.CartItems.COL_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + DbContract.CartItems.COL_PRODUCT_ID + ") REFERENCES "
                + DbContract.Products.TABLE + "(" + DbContract.Products.COL_ID + ") ON DELETE CASCADE,"
                + "UNIQUE(" + DbContract.CartItems.COL_USER_ID + "," + DbContract.CartItems.COL_PRODUCT_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Favorites.TABLE + " ("
                + DbContract.Favorites.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Favorites.COL_USER_ID + " INTEGER NOT NULL,"
                + DbContract.Favorites.COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + DbContract.Favorites.COL_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Favorites.COL_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + ") ON DELETE CASCADE,"
                + "FOREIGN KEY(" + DbContract.Favorites.COL_PRODUCT_ID + ") REFERENCES "
                + DbContract.Products.TABLE + "(" + DbContract.Products.COL_ID + ") ON DELETE CASCADE,"
                + "UNIQUE(" + DbContract.Favorites.COL_USER_ID + "," + DbContract.Favorites.COL_PRODUCT_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.DeliveryAgents.TABLE + " ("
                + DbContract.DeliveryAgents.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.DeliveryAgents.COL_USER_ID + " INTEGER NOT NULL UNIQUE,"
                + DbContract.DeliveryAgents.COL_VEHICLE_INFO + " TEXT,"
                + DbContract.DeliveryAgents.COL_IS_AVAILABLE + " INTEGER NOT NULL DEFAULT 1,"
                + "FOREIGN KEY(" + DbContract.DeliveryAgents.COL_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Orders.TABLE + " ("
                + DbContract.Orders.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Orders.COL_USER_ID + " INTEGER NOT NULL,"
                + DbContract.Orders.COL_STATUS + " TEXT NOT NULL,"
                + DbContract.Orders.COL_DELIVERY_AGENT_ID + " INTEGER,"
                + DbContract.Orders.COL_SUBTOTAL_MILLIMES + " INTEGER NOT NULL,"
                + DbContract.Orders.COL_DELIVERY_MILLIMES + " INTEGER NOT NULL,"
                + DbContract.Orders.COL_TOTAL_MILLIMES + " INTEGER NOT NULL,"
                + DbContract.Orders.COL_DELIVERY_ADDRESS + " TEXT NOT NULL,"
                + DbContract.Orders.COL_DELIVERY_PHONE + " TEXT NOT NULL,"
                + DbContract.Orders.COL_PAYMENT_METHOD + " TEXT NOT NULL,"
                + DbContract.Orders.COL_CREATED_AT + " INTEGER NOT NULL,"
                + DbContract.Orders.COL_UPDATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Orders.COL_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + "),"
                + "FOREIGN KEY(" + DbContract.Orders.COL_DELIVERY_AGENT_ID + ") REFERENCES "
                + DbContract.DeliveryAgents.TABLE + "(" + DbContract.DeliveryAgents.COL_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.OrderItems.TABLE + " ("
                + DbContract.OrderItems.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.OrderItems.COL_ORDER_ID + " INTEGER NOT NULL,"
                + DbContract.OrderItems.COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + DbContract.OrderItems.COL_PRODUCT_NAME + " TEXT NOT NULL,"
                + DbContract.OrderItems.COL_QUANTITY + " INTEGER NOT NULL,"
                + DbContract.OrderItems.COL_UNIT_PRICE_MILLIMES + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.OrderItems.COL_ORDER_ID + ") REFERENCES "
                + DbContract.Orders.TABLE + "(" + DbContract.Orders.COL_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.OrderStatusHistoryTable.TABLE + " ("
                + DbContract.OrderStatusHistoryTable.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.OrderStatusHistoryTable.COL_ORDER_ID + " INTEGER NOT NULL,"
                + DbContract.OrderStatusHistoryTable.COL_OLD_STATUS + " TEXT,"
                + DbContract.OrderStatusHistoryTable.COL_NEW_STATUS + " TEXT NOT NULL,"
                + DbContract.OrderStatusHistoryTable.COL_CHANGED_BY_USER_ID + " INTEGER NOT NULL,"
                + DbContract.OrderStatusHistoryTable.COL_NOTE + " TEXT,"
                + DbContract.OrderStatusHistoryTable.COL_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.OrderStatusHistoryTable.COL_ORDER_ID + ") REFERENCES "
                + DbContract.Orders.TABLE + "(" + DbContract.Orders.COL_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Reviews.TABLE + " ("
                + DbContract.Reviews.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Reviews.COL_PRODUCT_ID + " INTEGER NOT NULL,"
                + DbContract.Reviews.COL_USER_ID + " INTEGER NOT NULL,"
                + DbContract.Reviews.COL_ORDER_ID + " INTEGER,"
                + DbContract.Reviews.COL_RATING + " INTEGER NOT NULL,"
                + DbContract.Reviews.COL_COMMENT + " TEXT,"
                + DbContract.Reviews.COL_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Reviews.COL_PRODUCT_ID + ") REFERENCES "
                + DbContract.Products.TABLE + "(" + DbContract.Products.COL_ID + "),"
                + "FOREIGN KEY(" + DbContract.Reviews.COL_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Conversations.TABLE + " ("
                + DbContract.Conversations.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Conversations.COL_CLIENT_USER_ID + " INTEGER NOT NULL,"
                + DbContract.Conversations.COL_ADMIN_USER_ID + " INTEGER,"
                + DbContract.Conversations.COL_SUBJECT + " TEXT NOT NULL,"
                + DbContract.Conversations.COL_UPDATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Conversations.COL_CLIENT_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + ")"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Messages.TABLE + " ("
                + DbContract.Messages.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Messages.COL_CONVERSATION_ID + " INTEGER NOT NULL,"
                + DbContract.Messages.COL_SENDER_USER_ID + " INTEGER NOT NULL,"
                + DbContract.Messages.COL_BODY + " TEXT NOT NULL,"
                + DbContract.Messages.COL_IS_READ + " INTEGER NOT NULL DEFAULT 0,"
                + DbContract.Messages.COL_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Messages.COL_CONVERSATION_ID + ") REFERENCES "
                + DbContract.Conversations.TABLE + "(" + DbContract.Conversations.COL_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.Notifications.TABLE + " ("
                + DbContract.Notifications.COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DbContract.Notifications.COL_USER_ID + " INTEGER NOT NULL,"
                + DbContract.Notifications.COL_TITLE + " TEXT NOT NULL,"
                + DbContract.Notifications.COL_BODY + " TEXT,"
                + DbContract.Notifications.COL_TYPE + " TEXT NOT NULL,"
                + DbContract.Notifications.COL_IS_READ + " INTEGER NOT NULL DEFAULT 0,"
                + DbContract.Notifications.COL_REFERENCE_ID + " INTEGER,"
                + DbContract.Notifications.COL_CREATED_AT + " INTEGER NOT NULL,"
                + "FOREIGN KEY(" + DbContract.Notifications.COL_USER_ID + ") REFERENCES "
                + DbContract.Users.TABLE + "(" + DbContract.Users.COL_ID + ") ON DELETE CASCADE"
                + ")");

        db.execSQL("CREATE TABLE " + DbContract.AppSettings.TABLE + " ("
                + DbContract.AppSettings.COL_KEY + " TEXT PRIMARY KEY,"
                + DbContract.AppSettings.COL_VALUE + " TEXT NOT NULL"
                + ")");
    }

    private void createIndexes(SQLiteDatabase db) {
        db.execSQL("CREATE INDEX idx_products_category ON " + DbContract.Products.TABLE
                + "(" + DbContract.Products.COL_CATEGORY_ID + ")");
        db.execSQL("CREATE INDEX idx_orders_user ON " + DbContract.Orders.TABLE
                + "(" + DbContract.Orders.COL_USER_ID + ")");
        db.execSQL("CREATE INDEX idx_orders_status ON " + DbContract.Orders.TABLE
                + "(" + DbContract.Orders.COL_STATUS + ")");
        db.execSQL("CREATE INDEX idx_order_items_order ON " + DbContract.OrderItems.TABLE
                + "(" + DbContract.OrderItems.COL_ORDER_ID + ")");
        db.execSQL("CREATE INDEX idx_messages_conv ON " + DbContract.Messages.TABLE
                + "(" + DbContract.Messages.COL_CONVERSATION_ID + ")");
        db.execSQL("CREATE INDEX idx_notifications_user ON " + DbContract.Notifications.TABLE
                + "(" + DbContract.Notifications.COL_USER_ID + ")");
    }

    private void seedDemoData(SQLiteDatabase db) {
        long now = DateUtils.now();

        long adminId = insertUser(db, "admin@shop.tn", "Admin@2026", "Administrateur Boutique",
                "71112233", UserRole.ADMIN, false, now);
        long client1 = insertUser(db, "sarra.benali@email.tn", "Client@2026", "Sarra Ben Ali",
                "22554488", UserRole.CLIENT, false, now);
        long client2 = insertUser(db, "youssef.trabelsi@email.tn", "Client@2026", "Youssef Trabelsi",
                "99887766", UserRole.CLIENT, false, now);

        long catSoin = insertCategory(db, "Soin visage", "Sérums, crèmes et nettoyants.", 1,
                DemoSeedImages.CATEGORY_URLS[0]);
        long catMakeup = insertCategory(db, "Maquillage", "Teint, yeux et lèvres.", 2,
                DemoSeedImages.CATEGORY_URLS[1]);
        long catCorps = insertCategory(db, "Corps", "Gommages et laits corporels.", 3,
                DemoSeedImages.CATEGORY_URLS[2]);
        long catCheveux = insertCategory(db, "Cheveux", "Shampooings et soins capillaires.", 4,
                DemoSeedImages.CATEGORY_URLS[3]);
        long catParfums = insertCategory(db, "Parfums", "Eaux de parfum et brumes.", 5,
                DemoSeedImages.CATEGORY_URLS[4]);
        long catSolaire = insertCategory(db, "Solaire", "Protection SPF été.", 6,
                DemoSeedImages.CATEGORY_URLS[5]);

        long[] productIds = new long[24];
        productIds[0] = insertProduct(db, catSoin, "Crème Nuit Velours Rose", "Crème réparatrice nuit peaux sensibles.", 89900, 74900, 40, now, "Maison Cosmétique");
        productIds[1] = insertProduct(db, catSoin, "Sérum Éclat Vitamine C", "Sérum antioxydant uniformisant.", 129000, 0, 35, now, "Maison Cosmétique");
        productIds[2] = insertProduct(db, catSoin, "Eau Micellaire Pure", "Démaquillage doux sans rinçage.", 45900, 39900, 60, now, "Pure Glow");
        productIds[3] = insertProduct(db, catSoin, "Masque Argile Rose", "Purifiant une fois par semaine.", 38900, 0, 25, now, "Pure Glow");
        productIds[4] = insertProduct(db, catMakeup, "Fond de Teint Satiné 02", "Couvrance modulable longue tenue.", 67900, 59900, 30, now, "Velvet Lips");
        productIds[5] = insertProduct(db, catMakeup, "Rouge à Lèvres Velours Rubis", "Fini mat confortable.", 42500, 0, 45, now, "Velvet Lips");
        productIds[6] = insertProduct(db, catMakeup, "Mascara Volume Intense", "Cils densifiés sans paquets.", 38900, 34900, 50, now, "Velvet Lips");
        productIds[7] = insertProduct(db, catMakeup, "Palette Ombreurs Nude", "Six teintes mattes et nacrées.", 79900, 0, 20, now, "Nude Atelier");
        productIds[8] = insertProduct(db, catCorps, "Lait Corporel Pêche", "Hydratation 48h parfum délicat.", 52900, 0, 55, now, "Maison Cosmétique");
        productIds[9] = insertProduct(db, catCorps, "Gommage Sucre Vanille", "Peau lisse et douce.", 44900, 39900, 30, now, "Maison Cosmétique");
        productIds[10] = insertProduct(db, catCorps, "Beurre Karité Bio", "Nutrition zones très sèches.", 35900, 0, 40, now, "Karité Tunisie");
        productIds[11] = insertProduct(db, catCorps, "Huile Sèche Pailletée", "Éclat doré pour les soirées.", 62900, 54900, 15, now, "Karité Tunisie");
        productIds[12] = insertProduct(db, catCheveux, "Shampooing Réparateur Silk", "Cheveux abîmés et colorés.", 34900, 0, 70, now, "Silk Hair");
        productIds[13] = insertProduct(db, catCheveux, "Masque Argan Intense", "Nutrition profonde 5 min.", 48900, 42900, 35, now, "Silk Hair");
        productIds[14] = insertProduct(db, catCheveux, "Spray Thermo-Protecteur", "Avant le brushing.", 41900, 0, 40, now, "Silk Hair");
        productIds[15] = insertProduct(db, catCheveux, "Huile de Finition Gloss", "Brillance sans effet gras.", 37900, 0, 45, now, "Silk Hair");
        productIds[16] = insertProduct(db, catParfums, "Eau de Parfum Jasmin", "Notes florales orientales.", 149000, 129000, 20, now, "Jasmin Noir");
        productIds[17] = insertProduct(db, catParfums, "Brume Corps Coco", "Fraîcheur estivale.", 28900, 0, 50, now, "Jasmin Noir");
        productIds[18] = insertProduct(db, catParfums, "Roll-on Vanille Douce", "Format sac à main.", 24900, 21900, 60, now, "Jasmin Noir");
        productIds[19] = insertProduct(db, catParfums, "Coffret Découverte 4x5ml", "Quatre signatures maison.", 99000, 0, 12, now, "Maison Cosmétique");
        productIds[20] = insertProduct(db, catSolaire, "Fluide SPF50 Invisible", "Texture légère visage.", 55900, 49900, 45, now, "Sun Care TN");
        productIds[21] = insertProduct(db, catSolaire, "Lait Solaire Corps SPF30", "Résistant à l'eau.", 48900, 0, 50, now, "Sun Care TN");
        productIds[22] = insertProduct(db, catSolaire, "Stick Zones Sensibles SPF50+", "Lèvres et cicatrices.", 32900, 0, 35, now, "Sun Care TN");
        productIds[23] = insertProduct(db, catSolaire, "Après-Soleil Aloe", "Apaisant après exposition.", 41900, 36900, 40, now, "Sun Care TN");

        for (int i = 0; i < productIds.length; i++) {
            String url = DemoSeedImages.PRODUCT_URLS[i % DemoSeedImages.PRODUCT_URLS.length];
            ContentValues img = new ContentValues();
            img.put(DbContract.ProductImages.COL_PRODUCT_ID, productIds[i]);
            img.put(DbContract.ProductImages.COL_IMAGE_URL, url);
            img.put(DbContract.ProductImages.COL_SORT_ORDER, 0);
            db.insert(DbContract.ProductImages.TABLE, null, img);
        }

        ContentValues setting1 = new ContentValues();
        setting1.put(DbContract.AppSettings.COL_KEY, DbContract.AppSettings.KEY_DELIVERY_STANDARD);
        setting1.put(DbContract.AppSettings.COL_VALUE, "7000");
        db.insert(DbContract.AppSettings.TABLE, null, setting1);

        ContentValues setting2 = new ContentValues();
        setting2.put(DbContract.AppSettings.COL_KEY, DbContract.AppSettings.KEY_FREE_FROM);
        setting2.put(DbContract.AppSettings.COL_VALUE, "150000");
        db.insert(DbContract.AppSettings.TABLE, null, setting2);

        ContentValues setting3 = new ContentValues();
        setting3.put(DbContract.AppSettings.COL_KEY, DbContract.AppSettings.KEY_DELIVERY_EXPRESS);
        setting3.put(DbContract.AppSettings.COL_VALUE, "12000");
        db.insert(DbContract.AppSettings.TABLE, null, setting3);

        // Commande livrée (sans livreur)
        long order1 = insertOrder(db, client1, OrderStatus.LIVREE, null, 127800, 7000, 134800,
                "12 Rue de la Médina, Tunis", "22554488", "ESPECES", now - 86400000L * 5, now - 86400000L * 3);
        insertOrderItem(db, order1, productIds[0], "Crème Nuit Velours Rose", 1, 74900);
        insertOrderItem(db, order1, productIds[5], "Rouge à Lèvres Velours Rubis", 1, 42500);
        insertStatusHistory(db, order1, null, OrderStatus.EN_ATTENTE, adminId, "Création", now - 86400000L * 5);
        insertStatusHistory(db, order1, OrderStatus.EN_ATTENTE, OrderStatus.CONFIRMEE, adminId, null, now - 86400000L * 5 + 3600000);
        insertStatusHistory(db, order1, OrderStatus.CONFIRMEE, OrderStatus.EN_PREPARATION, adminId, null, now - 86400000L * 4);
        insertStatusHistory(db, order1, OrderStatus.EN_PREPARATION, OrderStatus.EN_LIVRAISON, adminId, null, now - 86400000L * 4 + 7200000);
        insertStatusHistory(db, order1, OrderStatus.EN_LIVRAISON, OrderStatus.LIVREE, adminId, null, now - 86400000L * 3);

        // En livraison
        long order2 = insertOrder(db, client2, OrderStatus.EN_LIVRAISON, null, 129000, 7000, 136000,
                "45 Avenue Habib Bourguiba, Sfax", "99887766", "CARTE", now - 86400000L, now - 3600000);
        insertOrderItem(db, order2, productIds[16], "Eau de Parfum Jasmin", 1, 129000);
        insertStatusHistory(db, order2, null, OrderStatus.EN_ATTENTE, client2, null, now - 86400000L);

        // En attente
        long order3 = insertOrder(db, client1, OrderStatus.EN_ATTENTE, null, 55900, 7000, 62900,
                "12 Rue de la Médina, Tunis", "22554488", "ESPECES", now - 7200000, now - 7200000);
        insertOrderItem(db, order3, productIds[20], "Fluide SPF50 Invisible", 1, 49900);

        // Annulée
        long order4 = insertOrder(db, client2, OrderStatus.ANNULEE, null, 38900, 7000, 45900,
                "45 Avenue Habib Bourguiba, Sfax", "99887766", "ESPECES", now - 86400000L * 2, now - 86400000L * 2);
        insertOrderItem(db, order4, productIds[6], "Mascara Volume Intense", 1, 34900);
        insertStatusHistory(db, order4, OrderStatus.EN_ATTENTE, OrderStatus.ANNULEE, adminId, "Client a changé d'avis", now - 86400000L * 2);

        ContentValues fav = new ContentValues();
        fav.put(DbContract.Favorites.COL_USER_ID, client1);
        fav.put(DbContract.Favorites.COL_PRODUCT_ID, productIds[1]);
        fav.put(DbContract.Favorites.COL_CREATED_AT, now);
        db.insert(DbContract.Favorites.TABLE, null, fav);

        ContentValues conv = new ContentValues();
        conv.put(DbContract.Conversations.COL_CLIENT_USER_ID, client1);
        conv.put(DbContract.Conversations.COL_ADMIN_USER_ID, adminId);
        conv.put(DbContract.Conversations.COL_SUBJECT, "Question sur la livraison");
        conv.put(DbContract.Conversations.COL_UPDATED_AT, now);
        long convId = db.insert(DbContract.Conversations.TABLE, null, conv);

        insertMessage(db, convId, client1, "Bonjour, délai pour Sousse ?", 1, now - 600000);
        insertMessage(db, convId, adminId, "24 à 48 h ouvrées.", 0, now - 300000);

        insertNotification(db, client1, "Commande livrée", "Votre commande #1 est livrée.", "ORDER", 0, order1, now - 86400000L * 3);
        insertNotification(db, client2, "En route", "Votre colis est en cours de livraison.", "ORDER", 0, order2, now - 1800000);
        insertNotification(db, adminId, "Nouvelle commande", "Commande #3 en attente.", "ORDER", 0, order3, now - 7200000);

        ContentValues review = new ContentValues();
        review.put(DbContract.Reviews.COL_PRODUCT_ID, productIds[0]);
        review.put(DbContract.Reviews.COL_USER_ID, client1);
        review.put(DbContract.Reviews.COL_ORDER_ID, order1);
        review.put(DbContract.Reviews.COL_RATING, 5);
        review.put(DbContract.Reviews.COL_COMMENT, "Texture légère, peau plus douce.");
        review.put(DbContract.Reviews.COL_CREATED_AT, now - 86400000L * 2);
        db.insert(DbContract.Reviews.TABLE, null, review);
    }

    private long insertUser(SQLiteDatabase db, String email, String plainPassword, String fullName,
                            String phone, UserRole role, boolean mustChange, long createdAt) {
        String salt = PasswordHasher.generateSaltBase64();
        String hash = PasswordHasher.hashPassword(plainPassword, salt);
        ContentValues v = new ContentValues();
        v.put(DbContract.Users.COL_EMAIL, email);
        v.put(DbContract.Users.COL_PASSWORD_HASH, hash);
        v.put(DbContract.Users.COL_SALT, salt);
        v.put(DbContract.Users.COL_FULL_NAME, fullName);
        v.put(DbContract.Users.COL_PHONE, phone);
        v.put(DbContract.Users.COL_ROLE, role.name());
        v.put(DbContract.Users.COL_MUST_CHANGE_PASSWORD, mustChange ? 1 : 0);
        v.put(DbContract.Users.COL_IS_ACTIVE, 1);
        v.put(DbContract.Users.COL_CREATED_AT, createdAt);
        return db.insert(DbContract.Users.TABLE, null, v);
    }

    private long insertDeliveryAgent(SQLiteDatabase db, long userId, String vehicle, int available) {
        ContentValues v = new ContentValues();
        v.put(DbContract.DeliveryAgents.COL_USER_ID, userId);
        v.put(DbContract.DeliveryAgents.COL_VEHICLE_INFO, vehicle);
        v.put(DbContract.DeliveryAgents.COL_IS_AVAILABLE, available);
        return db.insert(DbContract.DeliveryAgents.TABLE, null, v);
    }

    private long insertCategory(SQLiteDatabase db, String name, String desc, int sort, String imageUrl) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Categories.COL_NAME, name);
        v.put(DbContract.Categories.COL_DESCRIPTION, desc);
        v.put(DbContract.Categories.COL_IMAGE_URL, imageUrl);
        v.put(DbContract.Categories.COL_SORT_ORDER, sort);
        return db.insert(DbContract.Categories.TABLE, null, v);
    }

    private long insertProduct(SQLiteDatabase db, long categoryId, String name, String desc,
                               int price, int promo, int stock, long createdAt, String brand) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Products.COL_CATEGORY_ID, categoryId);
        v.put(DbContract.Products.COL_NAME, name);
        v.put(DbContract.Products.COL_DESCRIPTION, desc);
        v.put(DbContract.Products.COL_PRICE_MILLIMES, price);
        v.put(DbContract.Products.COL_PROMO_PRICE_MILLIMES, promo);
        v.put(DbContract.Products.COL_STOCK, stock);
        v.put(DbContract.Products.COL_IS_ACTIVE, 1);
        v.put(DbContract.Products.COL_BRAND, brand);
        v.put(DbContract.Products.COL_SKIN_TYPE, "Tous types");
        v.put(DbContract.Products.COL_INGREDIENTS, "Voir emballage");
        v.put(DbContract.Products.COL_TIPS, "Appliquer selon notice.");
        v.put(DbContract.Products.COL_ALERT_THRESHOLD, 15);
        v.put(DbContract.Products.COL_CREATED_AT, createdAt);
        return db.insert(DbContract.Products.TABLE, null, v);
    }

    private long insertOrder(SQLiteDatabase db, long userId, OrderStatus status, Long agentId,
                             int subtotal, int delivery, int total, String address, String phone,
                             String payment, long createdAt, long updatedAt) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Orders.COL_USER_ID, userId);
        v.put(DbContract.Orders.COL_STATUS, status.name());
        if (agentId != null) {
            v.put(DbContract.Orders.COL_DELIVERY_AGENT_ID, agentId);
        }
        v.put(DbContract.Orders.COL_SUBTOTAL_MILLIMES, subtotal);
        v.put(DbContract.Orders.COL_DELIVERY_MILLIMES, delivery);
        v.put(DbContract.Orders.COL_TOTAL_MILLIMES, total);
        v.put(DbContract.Orders.COL_DELIVERY_ADDRESS, address);
        v.put(DbContract.Orders.COL_DELIVERY_PHONE, phone);
        v.put(DbContract.Orders.COL_PAYMENT_METHOD, payment);
        v.put(DbContract.Orders.COL_CREATED_AT, createdAt);
        v.put(DbContract.Orders.COL_UPDATED_AT, updatedAt);
        return db.insert(DbContract.Orders.TABLE, null, v);
    }

    private void insertOrderItem(SQLiteDatabase db, long orderId, long productId, String name,
                                 int qty, int unitPrice) {
        ContentValues v = new ContentValues();
        v.put(DbContract.OrderItems.COL_ORDER_ID, orderId);
        v.put(DbContract.OrderItems.COL_PRODUCT_ID, productId);
        v.put(DbContract.OrderItems.COL_PRODUCT_NAME, name);
        v.put(DbContract.OrderItems.COL_QUANTITY, qty);
        v.put(DbContract.OrderItems.COL_UNIT_PRICE_MILLIMES, unitPrice);
        db.insert(DbContract.OrderItems.TABLE, null, v);
    }

    private void insertStatusHistory(SQLiteDatabase db, long orderId, OrderStatus oldStatus,
                                     OrderStatus newStatus, long byUser, String note, long at) {
        ContentValues v = new ContentValues();
        v.put(DbContract.OrderStatusHistoryTable.COL_ORDER_ID, orderId);
        if (oldStatus != null) {
            v.put(DbContract.OrderStatusHistoryTable.COL_OLD_STATUS, oldStatus.name());
        }
        v.put(DbContract.OrderStatusHistoryTable.COL_NEW_STATUS, newStatus.name());
        v.put(DbContract.OrderStatusHistoryTable.COL_CHANGED_BY_USER_ID, byUser);
        v.put(DbContract.OrderStatusHistoryTable.COL_NOTE, note);
        v.put(DbContract.OrderStatusHistoryTable.COL_CREATED_AT, at);
        db.insert(DbContract.OrderStatusHistoryTable.TABLE, null, v);
    }

    private void insertMessage(SQLiteDatabase db, long convId, long senderId, String body,
                               int read, long at) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Messages.COL_CONVERSATION_ID, convId);
        v.put(DbContract.Messages.COL_SENDER_USER_ID, senderId);
        v.put(DbContract.Messages.COL_BODY, body);
        v.put(DbContract.Messages.COL_IS_READ, read);
        v.put(DbContract.Messages.COL_CREATED_AT, at);
        db.insert(DbContract.Messages.TABLE, null, v);
    }

    private void insertNotification(SQLiteDatabase db, long userId, String title, String body,
                                    String type, int read, long refId, long at) {
        ContentValues v = new ContentValues();
        v.put(DbContract.Notifications.COL_USER_ID, userId);
        v.put(DbContract.Notifications.COL_TITLE, title);
        v.put(DbContract.Notifications.COL_BODY, body);
        v.put(DbContract.Notifications.COL_TYPE, type);
        v.put(DbContract.Notifications.COL_IS_READ, read);
        v.put(DbContract.Notifications.COL_REFERENCE_ID, refId);
        v.put(DbContract.Notifications.COL_CREATED_AT, at);
        db.insert(DbContract.Notifications.TABLE, null, v);
    }
}