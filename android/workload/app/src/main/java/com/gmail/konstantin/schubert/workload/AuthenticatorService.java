package com.gmail.konstantin.schubert.workload;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by kon on 01/05/15.
 */
public class AuthenticatorService extends Service {

    private Authenticator mAuthenticator;

    @Override
    public void onCreate(){
        mAuthenticator = new Authenticator(this);
    }

    @Override
    public IBinder onBind(Intent intent){
        return mAuthenticator.getIBinder();
    }


}
