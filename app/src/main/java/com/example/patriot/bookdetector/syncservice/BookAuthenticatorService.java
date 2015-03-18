package com.example.patriot.bookdetector.syncservice;

/**
 * Created by patriot on 3/11/2015.
 */


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * The service which allows the sync adapter framework to access the authenticator.
 */
public class BookAuthenticatorService extends Service {
    // Instance field that stores the authenticator object
    private BookAuthenticator mAuthenticator;
    @Override
    public void onCreate() {
// Create a new authenticator object
        mAuthenticator = new BookAuthenticator(this);
    }
    /*
    * When the system binds to this Service to make the RPC call
    * return the authenticator's IBinder.
    */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }
}


