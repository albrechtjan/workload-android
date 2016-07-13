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

import com.gmail.konstantin.schubert.workload.activities.WebLoginActivity;

import java.net.HttpCookie;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the session cookie for authentication with the web API.
 *
 * The implementation takes many inspirations from this tutorial:
 * https://developer.android.com/training/sync-adapters/creating-authenticator.html
 */
public class Authenticator extends AbstractAccountAuthenticator {

    private static final String TAG = Authenticator.class.getSimpleName();

    public static final String NAME_COOKIE_DJANGO = "sessionid";
    <-- I am also sending the shibboleth cookie every time
    <-- this might be the reaoson why the website is creating a new django session for every request
            <-- is it also doing that on the browser interface when the shibboleth token is present in the browser?
        <-- then maybe I should fix it in the browser.
            If it does not happen in the browser, then why does it happen with the app?
    does it maybe not recognize the django session token as it is sent by the app?
    maybe for some csrf reasons?
    Context mContext;

    /**
     * Constructor. Calls the super constructor and initializes members
     *
     */
    public Authenticator(Context context) {
        super(context);
        mContext = context;

    }

    /**
     * Looks for the Django session cookie in the cookieString.
     *
     * Returns it in a map if it finds it.
     * Returns null if at least one cookie is missing
     *
     */
    public static Map<String, HttpCookie> getCookiesFromCookieString(String cookieString) {

        Map<String, HttpCookie> cookies = new HashMap<>();

        for (String cookieSubString : cookieString.split(";")) {
            if (cookieSubString.contains(Authenticator.NAME_COOKIE_DJANGO)) {
                cookies.put(Authenticator.NAME_COOKIE_DJANGO, HttpCookie.parse(cookieSubString).get(0));
                return cookies;
            }
        }
        return null;

    }




    @Override
    public Bundle getAuthToken(
            AccountAuthenticatorResponse accountAuthenticatorResponse,
            Account account,
            String authTokenType,
            Bundle bundle) throws NetworkErrorException {
        Log.d(TAG, "called: getAuthToken");

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

        Intent intent = new Intent(mContext, WebLoginActivity.class);
        intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);

        Bundle intentBundle = new Bundle();
        intentBundle.putParcelable(AccountManager.KEY_INTENT, intent);

        return intentBundle;
    }

    /**
     * Inherited method, not supported.
     */
    @Override
    public Bundle editProperties(AccountAuthenticatorResponse r, String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * Inherited method, stubbed out.
     */
    @Override
    public Bundle addAccount(
            AccountAuthenticatorResponse r,
            String s,
            String s2,
            String[] strings,
            Bundle bundle)
            throws NetworkErrorException {
        return null;
    }

    /**
     * Inherited method, stubbed out.
     *
     * Ignore attempts to confirm credentials
     */
    @Override
    public Bundle confirmCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            Bundle bundle) throws NetworkErrorException {
        return null;
    }

    /**
     * Inherited method, not yet supported
     */
    @Override
    public String getAuthTokenLabel(String s) {
        throw new UnsupportedOperationException();
    }

    /**
     * Inherited method, not supported
     */
    @Override
    public Bundle updateCredentials(
            AccountAuthenticatorResponse r,
            Account account,
            String s, Bundle bundle) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    /**
     * Inherited method, not supported
     */
    @Override
    public Bundle hasFeatures(
            AccountAuthenticatorResponse r,
            Account account, String[] strings) throws NetworkErrorException {
        Log.d(TAG, "called: hasFeatures");
        throw new UnsupportedOperationException();
    }


}
