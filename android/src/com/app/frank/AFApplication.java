package com.app.frank;

import android.app.Application;
import android.util.Log;

import com.appsflyer.AppsFlyerLib;
import com.appsflyer.AppsFlyerConversionListener;

import java.lang.reflect.Array;
import java.util.Map;

public class AFApplication extends Application {
    private static final String AF_DEV_KEY = "cvUiNt3jB4tVWT6jqf9hQg";
    private static Map<String, Object> appConversionData;
    //private static Map<String, String> appAttributeData;

    @Override
    public void onCreate() {
        super.onCreate();
        AppsFlyerConversionListener conversionListener = new AppsFlyerConversionListener() {


            @Override
            public void onConversionDataSuccess(Map<String, Object> conversionData) {

                for (String attrName : conversionData.keySet()) {
                    Log.d("AppsFlyer", "conversion attribute: " + attrName + " = " + conversionData.get(attrName));
                }
                appConversionData = conversionData;
                SplashScreen.getConversionDataFromAF();
            }

            @Override
            public void onConversionDataFail(String errorMessage) {
                Log.d("AppsFlyer", "error getting conversion data: " + errorMessage);
            }

            @Override
            public void onAppOpenAttribution(Map<String, String> conversionData) {

                for (String attrName : conversionData.keySet()) {
                    Log.d("AppsFlyer", "attribute: " + attrName + " = " + conversionData.get(attrName));
                }
                //appAttributeData = conversionData;
                //SplashScreen.getAttributeDataFromAF();
            }

            @Override
            public void onAttributionFailure(String errorMessage) {
                Log.d("AppsFlyer", "error onAttributionFailure : " + errorMessage);
            }
        };

        AppsFlyerLib.getInstance().init(AF_DEV_KEY, conversionListener, getApplicationContext());
        AppsFlyerLib.getInstance().startTracking(this);

    }

    public static Map<String, Object> getConversionData(){
        return appConversionData;
    }
    /*public static Map<String, String> getAttributeData(){
        return appAttributeData;
    }*/
}

