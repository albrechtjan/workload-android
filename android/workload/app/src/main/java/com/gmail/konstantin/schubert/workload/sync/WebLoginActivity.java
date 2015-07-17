package com.gmail.konstantin.schubert.workload.sync;


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
import java.util.ArrayList;
import java.util.List;


public class WebLoginActivity extends Activity {

    private WebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_login);
        mWebView = (WebView) findViewById(R.id.activity_main_webview);
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mWebView.loadUrl("https://survey.zqa.tu-dresden.de/app/shib/login/?next=/app/workload/api/blank/");

        mWebView.setWebViewClient(new WebViewClient() {

            public void onPageFinished(WebView view, String url) {

                <<maybe check the cookies only on certain urls?>>

                String cookieStrings = CookieManager.getInstance().getCookie(url);
                List<HttpCookie> cookies = new ArrayList<>();
                for(String cookieString : cookieStrings.split(";")) {
                    List<HttpCookie> parsed = HttpCookie.parse(cookieString);
                    cookies.add(parsed.get(0));
                }

                <<check if we have all the cookies, if so, add them to the AccountAuthenticatorResponse
                        that (should have been?) passed in the intent>>
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

