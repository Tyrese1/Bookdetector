package com.example.patriot.bookdetector.syncservice;

/**
 * Created by patriot on 3/11/2015.
 */


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BookSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static BookSyncAdapter sBookSyncAdapter = null;
    @Override
    public void onCreate() {
        Log.d("BookSyncService", "onCreate - BookSyncService");
        synchronized (sSyncAdapterLock) {
            if (sBookSyncAdapter == null) {
                sBookSyncAdapter = new BookSyncAdapter(getApplicationContext(), true);
            }
        }
    }
    @Override
    public IBinder onBind(Intent intent) {
        return sBookSyncAdapter.getSyncAdapterBinder();
    }
}