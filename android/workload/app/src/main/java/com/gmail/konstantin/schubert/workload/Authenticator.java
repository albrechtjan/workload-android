package com.gmail.konstantin.schubert.workload;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;


public class Authenticator extends AbstractAccountAuthenticator{
    private static final String TAG = Authenticator.class.getSimpleName();
    Context mContext;

    public Authenticator(Context context){
        super(context);
        mContext = context;

    }

    @Override
    public Bundle editProperties(
            AccountAuthenticatorResponse r, String s){
        Log.d(TAG, "called: editProperties");
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
            Log.d(TAG, "called: addAccount");
            return null;
        }



    // Ignore attempts to confirm credentials
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
            Log.d(TAG,"called:confirmCredentials");
        return null;
    }

    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse accountAuthenticatorResponse,
            Account account,
            String s,
            Bundle bundle) throws NetworkErrorException {
        Log.d(TAG,"called: getAuthToken");

//        The standard pattern for implementing any of the abstract methods is the following:
//
//        If the supplied arguments are enough for the authenticator to fully satisfy the request then it will do so and return a Bundle that contains the results.
//                If the authenticator needs information from the user to satisfy the request then it will create an Intent to an activity that will prompt the user for the information and then carry out the request. This intent must be returned in a Bundle as key KEY_INTENT.
//                The activity needs to return the final result when it is complete so the Intent should contain the AccountAuthenticatorResponse as KEY_ACCOUNT_MANAGER_RESPONSE. The activity must then call onResult(Bundle) or onError(int, String) when it is complete.
//        If the authenticator cannot synchronously process the request and return a result then it may choose to return null and then use the AccountManagerResponse to send the result when it has completed the request.

//Many servers support some notion of an authentication token, which can be used to authenticate a request to the server without sending the user's actual password.
// (Auth tokens are normally created with a separate request which does include the user's credentials.)
// AccountManager can generate auth tokens for applications, so the application doesn't need to handle passwords directly.
// Auth tokens are normally reusable and CACHED BY THE ACCOUNT MANAGER!!!!!!!, but must be refreshed periodically.
// It's the responsibility of applications to invalidate auth tokens when they
// stop working so the AccountManager knows it needs to regenerate them.


            // We are currently unwilling to store passwords and currently unable to login without user interaction.
            // Thus we must create an Intent that launches the web-view, allows the user to log in an then

            Intent intent = new Intent(mContext,????.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,accountAuthenticatorResponse);
        //                The activity needs to return the final result when it is complete so the Intent should contain the AccountAuthenticatorResponse as KEY_ACCOUNT_MANAGER_RESPONSE. The activity must then call onResult(Bundle) or onError(int, String) when it is complete.

            Bundle intentBundle = new Bundle();
            intentBundle.putParcelable(AccountManager.KEY_INTENT,intent);

            return intentBundle;
    }

    @Override
    public String getAuthTokenLabel(String s){
        Log.d(TAG,"called: getAuthTokenLabel");


        // not yet implemented
        throw new UnsupportedOperationException();
    }
    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
            Log.d(TAG,"called: updateCredentials");
        throw new UnsupportedOperationException();
    }
    // Checking features for the account is not supported
    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account, String[] strings) throws NetworkErrorException {
            Log.d(TAG,"called: hasFeatures");
        throw new UnsupportedOperationException();
    }


}
