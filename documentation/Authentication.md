Authentication
=================

The workload web service uses the Shibboleth Single-Sign-On system to authenticate users via their
university accounts. Once authenticated, the django framework creates a session token which is independent of the shibboleth 
session and which might persist for far longer.

This Django session token also allows a client to authenticate towards the workload web API.
For the Andoid app, which uses the workload web API, the challenge is to obtain such a token.
Since we are dealing the the important university account password here, I decided to not store the password
within android. (This might be an unwarranted security measure, but I am not experienced enough to judge this,
so I decided to go the safe route.)

The app brings its own Android authenticator service which is accessed by the sync adapter and which defines
the account type `tu-dresden.de`. However, the authenticator only stores the Django session cookie.

## Login

The login process is started by opening the `WebLoginActivity.java`. This is an Android web view, 
basically a browser window inside the app.It simply loads the login page of the workload 
website where the user goes through the normal online login process.
This is a bit tedious, especially since the WebLoginActivity does not offer the option to save passwords. 
There is an [open issue](https://github.com/KonstantinSchubert/workload-android/issues/21) for this.

After the user has authenticaed with the workload website, the WebLoginActivity extracts the Django session cookie, stores it
with the authenticator and closes.

The session cookie should be valid for multiple weeks. When the SyncAdapter notices that the authentication does not work any
more, it calls

  sAccountManager.invalidateAuthToken("tu-dresden.de", token);
    
When the expired token is requested from the authenticator, it triggers a notification which prompts the user to log in again.



## CSRF

Android apps *should* not be vulnerable to CSRF attacs. Therefore, the web API does not protect against these.
It does, however, require the special user-agent string `Workload_App_Android_CSRF_EXCEMPT` to be set.
This should be a rather safe approach, unless a user decides to modify the user agent of his browser to this value.
