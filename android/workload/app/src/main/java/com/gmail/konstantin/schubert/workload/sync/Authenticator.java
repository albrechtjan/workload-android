package com.gmail.konstantin.schubert.workload.sync;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;


public class Authenticator extends AbstractAccountAuthenticator{

    private static final String TAG = Authenticator.class.getSimpleName();

    //the following (parts of) key names also work as bundle keys for the httpcookie
    public static final String NAME_COOKIE_DJANGO = "csrftoken";
    public static final String NAME_COOKIE_CSRF = "sessionid";

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
        String authTokenType,
        Bundle bundle) throws NetworkErrorException {
        Log.d(TAG,"called: getAuthToken");

        //About storing passwords and getting the session token automatically:
        // http://stackoverflow.com/a/21491948/1375015

        // about the authTokenType:
        // In principle we are dealing, in the current setup, with 3 auth tokens: NAME_PART_COOKIE_SHIBBOLETH, NAME_COOKIE_DJANGO, NAME_COOKIE_CSRF.
        // What I am doing currently is that I am defining a single auth token as the bundle that
        // contains these three. Of course it would be cleaner to let the AccountManager manage these 3
        // independently. But then, getAuthToken would have to be called 3 times and thus 3 intents would be issued
        // for login. The user would have to log in 3 times and ... at this point it is clear that this makes not sense.
        // A realistic alternative would be to launch the intent only once and to cache the auth tokens in the Authenticator.
        // But since it is the AccountManager's job to cache the tokens and since he is the one being notified when they are
        // invalid, this would in my eyes break the design in a much more sever way than encapsulating the 3 tokens into
        // a single one ever would.


        // We are currently unwilling to store passwords and currently unable to login without user interaction.
        // Thus we must create an Intent that launches the web-view, allows the user to log in an then

        Intent intent = new Intent(mContext,WebLoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE,accountAuthenticatorResponse);

        Bundle intentBundle = new Bundle();
        intentBundle.putParcelable(AccountManager.KEY_INTENT,intent);

        return intentBundle;
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

    public static Map<String,HttpCookie> getCookiesFromCookieString(String cookieString){
        // Looks for the necessary cookies in the cookieString. Returns them in a map if it finds them.
        // Returns null if at least one cookie is missing
        Map<String,HttpCookie> cookies = new HashMap<>();
        for (String key : new String[]{Authenticator.NAME_COOKIE_CSRF, Authenticator.NAME_COOKIE_DJANGO}) {
            Boolean found = false;
            for (String cookieSubString : cookieString.split(";")) {
                if (cookieSubString.contains(key)) {
                    cookies.put(key, HttpCookie.parse(cookieSubString).get(0));
                    found = true;
                }
            }
            if (!found) {
                // There was a key we need but we did not find it in the cookies.
                return null;
            }
        }
        return cookies;

    }


}
