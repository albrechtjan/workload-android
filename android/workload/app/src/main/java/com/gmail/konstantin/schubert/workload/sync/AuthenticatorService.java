package com.gmail.konstantin.schubert.workload.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A bound Service that instantiates the Authenticator.java
 * when started.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    /**
     * Initializes the class members
     */
    @Override
    public void onCreate() {
        mAuthenticator = new Authenticator(this);
    }

    /**
     * Method that returns the IBinder object which is needed for binding the service.
     * It is called when the service is first bound to a client.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return mAuthenticator.getIBinder();
    }


}
