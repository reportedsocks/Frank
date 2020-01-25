package com.app.frank;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.app.frank.notifications.MyBroadcastReceiver;

import androidx.appcompat.app.AppCompatActivity;

public class WebViewActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private WebView mWebView;
    private String link;
    private boolean showEnglishNotifications;

    //8 hours 28800000L
    private final long INTERVAL = 0L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();
        link = intent.getStringExtra("link");
        showEnglishNotifications = intent.getBooleanExtra("showEnglishNotifications", false);
        //Log.d("WebView", "the link webview gets: " + link);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        mWebView = (WebView) findViewById(R.id.webView);



        progressBar.setVisibility(View.VISIBLE);
        mWebView.setWebViewClient(new Browser_home());
        mWebView.setWebChromeClient(new MyChrome());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);


        if(savedInstanceState==null){
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    loadWebsite();
                }
            });
        }
        //creates chanel for notifications
        createNotificationChannel();
        //creates a repeated alarm
        setupAlarm();
    }

    private void setupAlarm() {
        Intent intent = new Intent(WebViewActivity.this, MyBroadcastReceiver.class);

        if(showEnglishNotifications){
            String[] titles = getResources().getStringArray(R.array.english_notifications_title);
            String[] texts = getResources().getStringArray(R.array.english_notifications_text);
            //Log.d("notifications i put", titles.toString());
            intent.putExtra("Notification title", titles);
            intent.putExtra("Notification text", texts);
        } else {
            String[] titles = getResources().getStringArray(R.array.russian_notifications_title);
            String[] texts = getResources().getStringArray(R.array.russian_notifications_text);
            //Log.d("notifications i put", titles.toString());
            intent.putExtra("Notification title", titles);
            intent.putExtra("Notification text", texts);
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(WebViewActivity.this, 987, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        if(alarmManager!= null){
            alarmManager.cancel(pendingIntent);
            //Log.i("alarm", "cancelled alarm");
        }
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL,INTERVAL,  pendingIntent);
        //Log.i("alarm", "alarm was set");
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getResources().getString(R.string.app_name) + " app notifications";
            String description = getResources().getString(R.string.app_name) + " app can send you notifications";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("Chanel_id", name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState )
    {
        super.onSaveInstanceState(outState);
        mWebView.saveState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        mWebView.restoreState(savedInstanceState);
    }

    private void loadWebsite() {
        ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
            //Log.d("WebView", "the link webview loads: " + link);
            mWebView.loadUrl(link);

        } else {
            mWebView.setVisibility(View.GONE);
        }
    }

    class Browser_home extends WebViewClient {

        Browser_home() {
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return false;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            //Log.d("webview", "page started");
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            setTitle(view.getTitle());
            progressBar.setVisibility(View.GONE);
            //Log.d("webview", "page finished");
            super.onPageFinished(view, url);

        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            //Log.d("webview", "ssl error");
            super.onReceivedSslError(view, handler, error);
        }
    }

    private class MyChrome extends WebChromeClient {

        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        MyChrome() {}

        public Bitmap getDefaultVideoPoster()
        {
            if (mCustomView == null) {
                return null;
            }
            return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView()
        {
            ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback)
        {
            if (this.mCustomView != null)
            {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            getWindow().getDecorView().setSystemUiVisibility(3846);
        }
    }
}