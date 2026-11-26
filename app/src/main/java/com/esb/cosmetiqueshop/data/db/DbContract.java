package com.esb.cosmetiqueshop.data.db;

/**
 * Constantes tables / colonnes SQLite — source unique pour les DAO.
 */
public final class DbContract {

    private DbContract() {
    }

    public static final class Users {
        public static final String TABLE = "users";
        public static final String COL_ID = "id";
        public static final String COL_EMAIL = "email";
        public static final String COL_PASSWORD_HASH = "password_hash";
        public static final String COL_SALT = "salt";
        public static final String COL_FULL_NAME = "full_name";
        public static final String COL_PHONE = "phone";
        public static final String COL_ROLE = "role";
        public static final String COL_MUST_CHANGE_PASSWORD = "must_change_password";
        public static final String COL_IS_ACTIVE = "is_active";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class Categories {
        public static final String TABLE = "categories";
        public static final String COL_ID = "id";
        public static final String COL_NAME = "name";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_IMAGE_URL = "image_url";
        public static final String COL_SORT_ORDER = "sort_order";
    }

    public static final class Products {
        public static final String TABLE = "products";
        public static final String COL_ID = "id";
        public static final String COL_CATEGORY_ID = "category_id";
        public static final String COL_NAME = "name";
        public static final String COL_DESCRIPTION = "description";
        public static final String COL_PRICE_MILLIMES = "price_millimes";
        public static final String COL_PROMO_PRICE_MILLIMES = "promo_price_millimes";
        public static final String COL_STOCK = "stock";
        public static final String COL_IS_ACTIVE = "is_active";
        public static final String COL_BRAND = "brand";
        public static final String COL_SKIN_TYPE = "skin_type";
        public static final String COL_INGREDIENTS = "ingredients";
        public static final String COL_TIPS = "tips";
        public static final String COL_ALERT_THRESHOLD = "alert_threshold";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class ProductImages {
        public static final String TABLE = "product_images";
        public static final String COL_ID = "id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_IMAGE_URL = "image_url";
        public static final String COL_SORT_ORDER = "sort_order";
    }

    public static final class CartItems {
        public static final String TABLE = "cart_items";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_QUANTITY = "quantity";
    }

    public static final class Favorites {
        public static final String TABLE = "favorites";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class Orders {
        public static final String TABLE = "orders";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_STATUS = "status";
        public static final String COL_SUBTOTAL_MILLIMES = "subtotal_millimes";
        public static final String COL_DELIVERY_MILLIMES = "delivery_millimes";
        public static final String COL_TOTAL_MILLIMES = "total_millimes";
        public static final String COL_DELIVERY_ADDRESS = "delivery_address";
        public static final String COL_DELIVERY_PHONE = "delivery_phone";
        public static final String COL_PAYMENT_METHOD = "payment_method";
        public static final String COL_CREATED_AT = "created_at";
        public static final String COL_UPDATED_AT = "updated_at";
    }

    public static final class OrderItems {
        public static final String TABLE = "order_items";
        public static final String COL_ID = "id";
        public static final String COL_ORDER_ID = "order_id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_PRODUCT_NAME = "product_name";
        public static final String COL_QUANTITY = "quantity";
        public static final String COL_UNIT_PRICE_MILLIMES = "unit_price_millimes";
    }

    public static final class OrderStatusHistoryTable {
        public static final String TABLE = "order_status_history";
        public static final String COL_ID = "id";
        public static final String COL_ORDER_ID = "order_id";
        public static final String COL_OLD_STATUS = "old_status";
        public static final String COL_NEW_STATUS = "new_status";
        public static final String COL_CHANGED_BY_USER_ID = "changed_by_user_id";
        public static final String COL_NOTE = "note";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class Reviews {
        public static final String TABLE = "reviews";
        public static final String COL_ID = "id";
        public static final String COL_PRODUCT_ID = "product_id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_ORDER_ID = "order_id";
        public static final String COL_RATING = "rating";
        public static final String COL_COMMENT = "comment";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class Conversations {
        public static final String TABLE = "conversations";
        public static final String COL_ID = "id";
        public static final String COL_CLIENT_USER_ID = "client_user_id";
        public static final String COL_ADMIN_USER_ID = "admin_user_id";
        public static final String COL_SUBJECT = "subject";
        public static final String COL_UPDATED_AT = "updated_at";
    }

    public static final class Messages {
        public static final String TABLE = "messages";
        public static final String COL_ID = "id";
        public static final String COL_CONVERSATION_ID = "conversation_id";
        public static final String COL_SENDER_USER_ID = "sender_user_id";
        public static final String COL_BODY = "body";
        public static final String COL_IS_READ = "is_read";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class Notifications {
        public static final String TABLE = "notifications";
        public static final String COL_ID = "id";
        public static final String COL_USER_ID = "user_id";
        public static final String COL_TITLE = "title";
        public static final String COL_BODY = "body";
        public static final String COL_TYPE = "type";
        public static final String COL_IS_READ = "is_read";
        public static final String COL_REFERENCE_ID = "reference_id";
        public static final String COL_CREATED_AT = "created_at";
    }

    public static final class AppSettings {
        public static final String TABLE = "app_settings";
        public static final String COL_KEY = "setting_key";
        public static final String COL_VALUE = "setting_value";

        public static final String KEY_DELIVERY_STANDARD = "delivery_standard_millimes";
        public static final String KEY_FREE_FROM = "free_from";
        public static final String KEY_DELIVERY_EXPRESS = "delivery_express_millimes";
    }
}