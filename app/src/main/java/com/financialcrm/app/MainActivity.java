package com.financialcrm.app;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progress;
    private View offlineView;
    private Button retryButton;

    private static final String APP_URL =
            "https://fulkumari.com.np/sheettoweb/App/Fanancial%20App/Crm";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progress = findViewById(R.id.progress);
        offlineView = findViewById(R.id.offlineView);
        retryButton = findViewById(R.id.retryButton);

        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(false);
        settings.setUseWideViewPort(false);
        settings.setSupportMultipleWindows(false);

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                String url = request.getUrl().toString();

                if (url.startsWith("https://fulkumari.com.np/") ||
                        url.startsWith("http://fulkumari.com.np/")) {

                    view.loadUrl(url);
                    return true;
                }

                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {

                // Only handle the main page error.
                if (request.isForMainFrame()) {
                    progress.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                    showOfflineMessage();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            @Override
            public void onProgressChanged(
                    WebView view,
                    int newProgress) {

                progress.setVisibility(
                        newProgress < 100
                                ? View.VISIBLE
                                : View.GONE
                );
            }
        });

        retryButton.setOnClickListener(v -> loadApp());

        loadApp();
    }

    /**
     * Load website only when internet is available.
     */
    private void loadApp() {

        if (!hasInternetConnection()) {
            showOfflineMessage();
            return;
        }

        offlineView.setVisibility(View.GONE);
        webView.setVisibility(View.VISIBLE);
        progress.setVisibility(View.VISIBLE);

        webView.loadUrl(APP_URL);
    }

    /**
     * Check actual internet/network connection.
     */
    private boolean hasInternetConnection() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(
                        CONNECTIVITY_SERVICE
                );

        if (connectivityManager == null) {
            return false;
        }

        Network network =
                connectivityManager.getActiveNetwork();

        if (network == null) {
            return false;
        }

        NetworkCapabilities capabilities =
                connectivityManager.getNetworkCapabilities(network);

        if (capabilities == null) {
            return false;
        }

        return capabilities.hasCapability(
                NetworkCapabilities.NET_CAPABILITY_INTERNET
        );
    }

    /**
     * Show custom offline screen.
     * Android WebView error page will NOT be shown.
     */
    private void showOfflineMessage() {

        progress.setVisibility(View.GONE);
        webView.setVisibility(View.GONE);
        offlineView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {

        if (webView.getVisibility() == View.VISIBLE &&
                webView.canGoBack()) {

            webView.goBack();

        } else {
            super.onBackPressed();
        }
    }
}
