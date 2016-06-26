package com.gmail.konstantin.schubert.workload.sync;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * HTTP Client class that connects to the web API.
 * All http traffic is managed by this class.
 *
 * \todo: The apache.http library which is used in this class is deprecated, and the design of the
 * \todo: class with the global .response attribute is also quite - strange.
 * \todo: Migrate to http://developer.android.com/about/versions/marshmallow/android-6.0-changes.html#behavior-apache-http-client
 * \todo: I would like to use PATCH as keyword instead of PUT
 * \todo: Place the POST and PUT/PATCH data as json in body?
 */
public class RestClient {

    public enum RequestMethod {
        GET,
        POST,
        PUT
        //DELETE not used
    }

    public final static String TAG = "RestClient";
    public int responseCode = 0;
    public String message;
    public String response;

    /**
     * Takes an InputStram object, converts it to a string and returns it.
     *
     */
    private static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
            is.close();
        } catch (IOException e) {
        }
        return sb.toString();
    }

    /**
     * Builds the org.apache.http.client.methods
     *
     * @param method Enum, defined above. Defines the request method (GET, POST, ...)
     * @param url Url to which the HTTP request is made.
     * @param headers the HTTP headers
     * @param params Optional url parameters
     * @throws Exception
     * \todo: Implement payload for POST and PUT
     */
    public void Execute(RequestMethod method, String url, ArrayList<NameValuePair> headers, ArrayList<NameValuePair> params) throws Exception {
        Log.d(TAG, "Executing Rest Method " + method.toString() + " on " + url);
        switch (method) {
            case GET: {
                String combinedParams = "";
                if (params != null) {
                    combinedParams += "?";
                    for (NameValuePair p : params) {
                        String paramString = p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
                        if (combinedParams.length() > 1)
                            combinedParams += "&" + paramString;
                        else
                            combinedParams += paramString;
                    }
                }
                HttpGet request = new HttpGet(url + combinedParams);
                // add headers
                if (headers != null) {
                    headers = addCommonHeaderField(headers);
                    for (NameValuePair h : headers)
                        request.addHeader(h.getName(), h.getValue());
                }
                executeRequest(request, url);
                break;
            }
            case POST: {
                HttpPost request = new HttpPost(url);
                // add headers
                if (headers != null) {
                    headers = addCommonHeaderField(headers);
                    for (NameValuePair h : headers)
                        request.addHeader(h.getName(), h.getValue());
                }
                if (params != null)
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                executeRequest(request, url);
                break;
            }
            case PUT: {
                HttpPut request = new HttpPut(url);
                if (headers != null) {
                    headers = addCommonHeaderField(headers);
                    for (NameValuePair h : headers) {
                        request.addHeader(h.getName(), h.getValue());
                    }
                }
                if (params != null)
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                executeRequest(request, url);
                break;
                // where do we send the stuff that is to be updated?
            }
        }
    }

    /**
     * Extends the list of headers with those that are used on every request.
     */
    private ArrayList<NameValuePair> addCommonHeaderField(ArrayList<NameValuePair> _header) {
        _header.add(new BasicNameValuePair("Content-Type", "application/x-www-form-urlencoded"));
        _header.add(new BasicNameValuePair("User-Agent", "Workload_App_Android_CSRF_EXCEMPT"));
        return _header;
    }

    /**
     * Metho that actually executes the http requests.
     *
     * It also converts the response stream to a string and assigns the result to the global
     * .result field of this class.
     */
    private void executeRequest(HttpUriRequest request, String url) throws IOException, ClientProtocolException {
        HttpClient client = new DefaultHttpClient();
        HttpResponse httpResponse;

        httpResponse = client.execute(request);
        responseCode = httpResponse.getStatusLine().getStatusCode();
        message = httpResponse.getStatusLine().getReasonPhrase();
        HttpEntity entity = httpResponse.getEntity();

        if (entity != null) {
            InputStream instream = entity.getContent();
            response = convertStreamToString(instream);
            instream.close();
        }

    }


}