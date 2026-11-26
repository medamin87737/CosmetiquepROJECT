package com.esb.cosmetiqueshop;

import android.app.Application;

import com.esb.cosmetiqueshop.data.db.DatabaseHelper;
import com.esb.cosmetiqueshop.notifications.NotificationHelper;

/**
 * Initialisation application : canal notifications + DB chaude.
 */
public class CosmetiqueApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationHelper.createChannel(this);
        new DatabaseHelper(this).getWritableDatabase();
    }
}