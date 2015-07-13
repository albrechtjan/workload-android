package com.gmail.konstantin.schubert.workload;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;


public class Authenticator extends AbstractAccountAuthenticator{
    public Authenticator(Context context){
        super(context);
//        https://stackoverflow.com/questions/3606596/android-start-activity-from-service
//        make sure that the authenticator really only asks the user to log in when it is used
//        it should never ask to log in when the app is not running, that is just mean
    }

    @Override
    public Bundle editProperties(
            AccountAuthenticatorResponse r, String s){
        //editing properties not yet supported
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(
        AccountAuthenticatorResponse r,
        String s,
        String s2,
        String [] strings,
        Bundle bundle)
        // Until I am comfortable handling the users credentials in the app,
        // I will keep ignoring their attempts to add an account
        throws NetworkErrorException{
            return null;
        }



    // Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle) throws NetworkErrorException {
//Many servers support some notion of an authentication token, which can be used to authenticate a request to the server without sending the user's actual password.
// (Auth tokens are normally created with a separate request which does include the user's credentials.)
// AccountManager can generate auth tokens for applications, so the application doesn't need to handle passwords directly.
// Auth tokens are normally reusable and CACHED BY THE ACCOUNT MANAGER!!!!!!!, but must be refreshed periodically.
// It's the responsibility of applications to invalidate auth tokens when they
// stop working so the AccountManager knows it needs to regenerate them.

    //TODO:      we do not need to cached, just get an auth token!!!
        return Bundle.EMPTY;
    }

    @Override
    public String getAuthTokenLabel(String s){


        // not yet implemented
        throw new UnsupportedOperationException();
    }
    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
    // Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account, String[] strings) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }


}
