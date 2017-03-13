//
//  MobihelpPlugin.java
//  FreshdeskSDK
//
//  Copyright (c) 2014 Freshdesk. All rights reserved.
//

package com.freshdesk.mobihelp.android;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.content.Context;
import android.os.Build;
import android.util.Log;


import com.freshdesk.mobihelp.*;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;


public class MobihelpPlugin extends CordovaPlugin {

    private MobihelpConfig mobihelpConfig;
    private Context cordovaActivity;
    private boolean isInitialized = false;



    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView)
    {
        super.initialize(cordova, webView);

        // Use TLSSocketFactory on older Anrdoid versions where TLS 1.2 is not supported
        enableTLS12Compat();
        cordovaActivity = cordova.getActivity();
    }

    private void enableTLS12Compat() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, null, null);
            SSLSocketFactory sslSocketFactory = null;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                sslSocketFactory = new TLSSocketFactory(sslContext.getSocketFactory());
            } else {
                sslSocketFactory = sslContext.getSocketFactory();
            }
            HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactory);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean execute(String action, String rawargs,final CallbackContext callbackContext) throws JSONException
    {

        JSONArray args = new JSONArray(rawargs);

        //Utility functions
        if (action.equals("logToDevice")) {
            Log.v("MOBIHELP",args.getString(0));
            return true;
        }

        // Initialize function
        if (action.equals("init")) {

            JSONObject initArgs = new JSONObject(args.getString(0));

            String domain = initArgs.getString("domain"); domain = "https://".concat(domain);
            String appKey = initArgs.getString("appKey");
            String appSecret = initArgs.getString("appSecret");
            MobihelpConfig mobihelpConfig = new MobihelpConfig(domain, appKey, appSecret);

            //Set values (defaults are set in Javascript wrapper)
            mobihelpConfig.setAppStoreReviewUrl(initArgs.getString("androidAppStoreUrl"));
            mobihelpConfig.setAutoReplyEnabled(initArgs.getBoolean("autoReplyEnabled"));
            mobihelpConfig.setEnhancedPrivacyModeEnabled(initArgs.getBoolean("enhancedPrivacyModeEnabled"));
            mobihelpConfig.setFeedbackType(FeedbackType.valueOf(initArgs.getString("feedbackType")));
            mobihelpConfig.setLaunchCountForReviewPrompt(initArgs.getInt("launchCountForReviewPrompt"));
            mobihelpConfig.setPrefetchSolutions(initArgs.getBoolean("prefetchSolutions"));

            Mobihelp.init(cordovaActivity, mobihelpConfig);

            this.isInitialized = true;
            this.mobihelpConfig = mobihelpConfig;

            callbackContext.success();
            return true;
        }

        //MobihelpSDK must be initialozed before any other method can be called
        if(!isInitialized)
            throw new IllegalStateException("Mobihelp has not been initialized");



        //Mobihelp class functions
        if (action.equals("addCustomData")) {
            if(args.isNull(0))
                throw new IllegalArgumentException("Key for Custom Data not provided.");
            if(args.isNull(1))
                throw new IllegalArgumentException("Value for Custom Data not provided.");

            boolean sensitivity;
            if(args.isNull(2))
                sensitivity = false;
            else
                sensitivity = Boolean.parseBoolean(args.getString(2));

            Mobihelp.addCustomData(args.getString(0),args.getString(1),sensitivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("clearBreadCrumbs")) {
            Mobihelp.clearBreadCrumbs(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("clearCustomData")) {
            Mobihelp.clearCustomData(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("clearUserData")) {
            Mobihelp.clearUserData(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("getUnreadCount")) {
            int count = Mobihelp.getUnreadCount(cordovaActivity);
            callbackContext.success(count);
            return true;
        }
        if (action.equals("getUnreadCountAsync")) {
            UnreadUpdatesCallback unreadUpdatesCallback = new UnreadUpdatesCallback() {
                @Override
                public void onResult(MobihelpCallbackStatus mobihelpCallbackStatus, Integer integer)
                {
                    if (mobihelpCallbackStatus == MobihelpCallbackStatus.STATUS_SUCCESS)
                        callbackContext.success(integer);
                    else
                        callbackContext.error(mobihelpCallbackStatus.toString());
                }
            };
            Mobihelp.getUnreadCountAsync(cordovaActivity, unreadUpdatesCallback);
            return true;
        }
        if (action.equals("leaveBreadCrumb"))  {
            if(args.isNull(0))
                throw new IllegalArgumentException("CrumbDetails not provided.");
            Mobihelp.leaveBreadCrumb(args.getString(0));
            callbackContext.success();
            return true;
        }
        if (action.equals("setUserEmail")) {
            if(args.isNull(0))
                throw new IllegalArgumentException("Email not provided.");
            Mobihelp.setUserEmail(cordovaActivity, args.getString(0));
            callbackContext.success();
            return true;
        }
        if (action.equals("setUserFullName")) {
            if(args.isNull(0))
                throw new IllegalArgumentException("User Name not provided.");
            Mobihelp.setUserFullName(cordovaActivity, args.getString(0));
            callbackContext.success();
            return true;
        }
        if (action.equals("showAppRateDialog")) {
            Mobihelp.showAppRateDialog(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("showConversations")) {
            Mobihelp.showConversations(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("showFeedback")) {
            Mobihelp.showFeedback(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("showSolutions")) {
            Mobihelp.showSolutions(cordovaActivity);
            callbackContext.success();
            return true;
        }
        if (action.equals("showSupport")) {
            Mobihelp.showSupport(cordovaActivity);
            callbackContext.success();
            return true;
        }

        return false;
    }


}
