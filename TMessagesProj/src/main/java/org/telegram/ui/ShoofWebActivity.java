package org.telegram.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import org.telegram.messenger.UserConfig;

public class ShoofWebActivity extends Activity {

    private WebView webView;
    private static final String SHOOF_URL = "https://shoof-tv.net";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        RelativeLayout layout = new RelativeLayout(this);
        layout.setBackgroundColor(0xFF000000);

        webView = new WebView(this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(params);

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setAllowFileAccess(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        // حقن Telegram.WebApp للنجوم والدفع
        long userId = UserConfig.getInstance(UserConfig.selectedAccount).clientUserId;
        webView.addJavascriptInterface(new TelegramBridge(userId), "TelegramBridge");

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // حقن window.Telegram.WebApp
                view.evaluateJavascript(
                    "window.Telegram = { WebApp: {" +
                    "  initData: 'source=apk&user_id=' + TelegramBridge.getUserId()," +
                    "  initDataUnsafe: { user: { id: TelegramBridge.getUserId() } }," +
                    "  version: '7.0'," +
                    "  platform: 'android'," +
                    "  colorScheme: 'dark'," +
                    "  themeParams: { bg_color: '#000000', text_color: '#ffffff', button_color: '#cc0000' }," +
                    "  isExpanded: true," +
                    "  viewportHeight: window.innerHeight," +
                    "  viewportStableHeight: window.innerHeight," +
                    "  ready: function() {}," +
                    "  expand: function() {}," +
                    "  close: function() {}," +
                    "  openInvoice: function(url, cb) { TelegramBridge.openInvoice(url); }," +
                    "  openLink: function(url) { TelegramBridge.openLink(url); }," +
                    "  showAlert: function(msg, cb) { alert(msg); if(cb) cb(); }," +
                    "  showConfirm: function(msg, cb) { if(cb) cb(confirm(msg)); }," +
                    "  HapticFeedback: { impactOccurred: function() {}, notificationOccurred: function() {}, selectionChanged: function() {} }" +
                    "} };", null);
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        layout.addView(webView);
        setContentView(layout);
        webView.loadUrl(SHOOF_URL);
    }

    public class TelegramBridge {
        private long userId;
        TelegramBridge(long id) { this.userId = id; }

        @JavascriptInterface
        public long getUserId() { return userId; }

        @JavascriptInterface
        public void openInvoice(String url) {
            // TODO: ربط بنظام الدفع الأصلي في تيليجرام
        }

        @JavascriptInterface
        public void openLink(String url) {
            runOnUiThread(() -> webView.loadUrl(url));
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()) {
                webView.goBack();
            }
            // لا نخرج من التطبيق - نمنع الرجوع لتيليجرام
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onResume() { super.onResume(); if (webView != null) webView.onResume(); }

    @Override
    protected void onPause() { super.onPause(); if (webView != null) webView.onPause(); }

    @Override
    protected void onDestroy() { super.onDestroy(); if (webView != null) { webView.destroy(); webView = null; } }
}
