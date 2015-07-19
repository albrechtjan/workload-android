package com.gmail.konstantin.schubert.workload.sync;


import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.gmail.konstantin.schubert.workload.R;

import android.webkit.CookieManager;
import java.net.HttpCookie;



public class WebLoginActivity extends Activity {

    private WebView mWebView;
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_login);
        mAccountAuthenticatorResponse = getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);
        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("https://survey.zqa.tu-dresden.de/app/shib/login/?next=/app/workload/api/blank/");

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {

                //TODO:<<maybe check the cookies only on certain urls?>>
                Bundle response = new Bundle();
                String cookieStrings = CookieManager.getInstance().getCookie(url);

                if (Authenticator.getCookiesFromCookieString(cookieStrings) != null){
                    response.putString(AccountManager.KEY_ACCOUNT_NAME, "default_account");
                    response.putString(AccountManager.KEY_ACCOUNT_TYPE, getResources().getString(R.string.account_type));
                    response.putString(AccountManager.KEY_AUTHTOKEN, cookieStrings);
                    mAccountAuthenticatorResponse.onResult(response);
                }


            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}

