package com.financialcrm.app;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private WebView webView;
    private ProgressBar progress;
    private View offlineView;
    private Button retryButton;

    private static final String APP_URL =
            "https://fulkumari.com.np/sheettoweb/App/Fanancial%20App/Crm";

    private static final int PERMISSION_REQUEST_CODE = 1001;
    private static final int FILE_CHOOSER_REQUEST_CODE = 1002;

    private ValueCallback<Uri[]> filePathCallback;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progress = findViewById(R.id.progress);
        offlineView = findViewById(R.id.offlineView);
        retryButton = findViewById(R.id.retryButton);

        // Ask Camera + File/Photo permission
        requestAppPermissions();

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

        // Camera / microphone support
        settings.setMediaPlaybackRequiresUserGesture(false);

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
            public void onPageStarted(
                    WebView view,
                    String url,
                    android.graphics.Bitmap favicon) {

                progress.setVisibility(View.VISIBLE);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url) {

                progress.setVisibility(View.GONE);
                offlineView.setVisibility(View.GONE);
                webView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onReceivedError(
                    WebView view,
                    WebResourceRequest request,
                    WebResourceError error) {

                // Only show offline screen for main page
                if (request.isForMainFrame()) {

                    progress.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);

                    showOfflineMessage();
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient() {

            /**
             * Website camera permission
             */
            @Override
            public void onPermissionRequest(
                    android.webkit.PermissionRequest request) {

                runOnUiThread(() -> {

                    String[] resources = request.getResources();

                    ArrayList<String> allowedResources =
                            new ArrayList<>();

                    for (String resource : resources) {

                        if (android.webkit.PermissionRequest
                                .RESOURCE_VIDEO_CAPTURE
                                .equals(resource)) {

                            allowedResources.add(resource);
                        }

                        if (android.webkit.PermissionRequest
                                .RESOURCE_AUDIO_CAPTURE
                                .equals(resource)) {

                            allowedResources.add(resource);
                        }
                    }

                    if (!allowedResources.isEmpty()) {

                        request.grant(
                                allowedResources.toArray(
                                        new String[0]
                                )
                        );

                    } else {
                        request.deny();
                    }
                });
            }

            /**
             * HTML file/image upload
             */
            @Override
            public boolean onShowFileChooser(
                    WebView webView,
                    ValueCallback<Uri[]> filePathCallback,
                    FileChooserParams fileChooserParams) {

                if (MainActivity.this.filePathCallback != null) {

                    MainActivity.this.filePathCallback
                            .onReceiveValue(null);
                }

                MainActivity.this.filePathCallback =
                        filePathCallback;

                try {

                    Intent intent =
                            fileChooserParams.createIntent();

                    startActivityForResult(
                            intent,
                            FILE_CHOOSER_REQUEST_CODE
                    );

                } catch (Exception e) {

                    MainActivity.this.filePathCallback = null;

                    Toast.makeText(
                            MainActivity.this,
                            "File खोल्न सकिएन",
                            Toast.LENGTH_SHORT
                    ).show();

                    return false;
                }

                return true;
            }

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
     * Camera + Photo/File permissions
     */
    private void requestAppPermissions() {

        ArrayList<String> permissions =
                new ArrayList<>();

        // Camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (checkSelfPermission(
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.CAMERA
                );
            }
        }

        // Android 13+
        if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.TIRAMISU) {

            if (checkSelfPermission(
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.READ_MEDIA_IMAGES
                );
            }

        }
        // Android 12 and below
        else if (Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.M) {

            if (checkSelfPermission(
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(
                        Manifest.permission.READ_EXTERNAL_STORAGE
                );
            }
        }

        if (!permissions.isEmpty()) {

            requestPermissions(
                    permissions.toArray(
                            new String[0]
                    ),
                    PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * Permission result
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                PERMISSION_REQUEST_CODE) {

            boolean allGranted = true;

            for (int result : grantResults) {

                if (result !=
                        PackageManager.PERMISSION_GRANTED) {

                    allGranted = false;
                    break;
                }
            }

            if (!allGranted) {

                Toast.makeText(
                        this,
                        "Camera र Photo/File permission आवश्यक छ 🙏",
                        Toast.LENGTH_LONG
                ).show();
            }
        }
    }

    /**
     * File chooser result
     */
    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (requestCode ==
                FILE_CHOOSER_REQUEST_CODE) {

            if (filePathCallback == null) {
                return;
            }

            Uri[] results = null;

            if (resultCode == RESULT_OK) {

                if (data != null) {

                    String dataString =
                            data.getDataString();

                    if (dataString != null) {

                        results = new Uri[]{
                                Uri.parse(dataString)
                        };

                    } else if (data.getClipData() != null) {

                        int count =
                                data.getClipData()
                                        .getItemCount();

                        results =
                                new Uri[count];

                        for (int i = 0;
                             i < count;
                             i++) {

                            results[i] =
                                    data.getClipData()
                                            .getItemAt(i)
                                            .getUri();
                        }
                    }
                }
            }

            filePathCallback.onReceiveValue(results);

            filePathCallback = null;
        }
    }

    /**
     * Load website only when Internet exists
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
     * Check Internet connection
     */
    private boolean hasInternetConnection() {

        ConnectivityManager connectivityManager =
                (ConnectivityManager)
                        getSystemService(
                                CONNECTIVITY_SERVICE
                        );

        if (
