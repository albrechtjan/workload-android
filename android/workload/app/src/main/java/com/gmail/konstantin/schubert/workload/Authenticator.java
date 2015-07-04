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
        // for now ignore attempts to add Account
        throws NetworkErrorException{
            return null;
        }


    // Ignore attempts to confirm credentials
    // Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }
    // Getting an authentication token is not supported
    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse r,
            Account account,
            String s,
            Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
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
