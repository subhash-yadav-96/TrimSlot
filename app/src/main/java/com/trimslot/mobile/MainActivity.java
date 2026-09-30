package com.trimslot.mobile.v9;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private WebView webView;
    private ProgressBar progress;
    private LinearLayout errorLayout;

    private static final String HOME_URL =
        "https://script.google.com/macros/s/AKfycbzU8s0DobXjngzJeXjHKtSPQ_jvN04qILn33Cc1WmH87yyumOB2QKB--BUwvFEvSYkLTw/exec";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        progress = findViewById(R.id.progress);
        errorLayout = findViewById(R.id.errorLayout);
        Button retry = findViewById(R.id.retryButton);

        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setSupportZoom(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setLoadWithOverviewMode(true);
        s.setUseWideViewPort(true);
        s.setJavaScriptCanOpenWindowsAutomatically(true);
        s.setSupportMultipleWindows(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        CookieManager cm = CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(webView, true);

        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // Do not block target=_blank/window.open navigation.
        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {
            @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
                progress.setVisibility(View.VISIBLE);
                errorLayout.setVisibility(View.GONE);
            }

            @Override public void onPageFinished(WebView view, String url) {
                progress.setVisibility(View.GONE);
                installMobileFixes(view);
            }

            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    progress.setVisibility(View.GONE);
                    errorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        retry.setOnClickListener(v -> loadHome());
        loadHome();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override public void handleOnBackPressed() {
                if (webView.canGoBack()) webView.goBack();
                else finish();
            }
        });
    }

    private void installMobileFixes(WebView view) {
        String js =
            "(function(){" +
            "try{" +
            "var m=document.querySelector('meta[name=viewport]');" +
            "if(!m){m=document.createElement('meta');m.name='viewport';document.head.appendChild(m);}" +
            "m.content='width=device-width,initial-scale=1.0,maximum-scale=1.0,user-scalable=no';" +
            "document.documentElement.style.width='100%';document.body.style.width='100%';" +
            "document.body.style.maxWidth='100vw';document.body.style.overflowX='hidden';" +

            // Make Account controls reliably respond to touch/click in WebView.
            "if(!window.__trimslotAccountFix){" +
            "window.__trimslotAccountFix=true;" +
            "document.addEventListener('touchend',function(e){" +
            "var el=e.target;" +
            "for(var i=0;el&&i<5;i++,el=el.parentElement){" +
            "var t=(el.innerText||el.textContent||'').trim();" +
            "var a=(el.getAttribute&&((el.getAttribute('aria-label')||'')+' '+(el.getAttribute('title')||''))).trim();" +
            "if((/^Account$/i.test(t)||/^Account/i.test(a)) && " +
            "(el.tagName==='BUTTON'||el.tagName==='A'||el.getAttribute('role')==='button'||el.onclick)){" +
            "e.preventDefault();e.stopPropagation();" +
            "try{el.focus();}catch(x){}" +
            "try{el.click();}catch(x){}" +
            "break;}" +
            "}" +
            "},true);" +
            "}" +
            "}catch(e){}" +
            "})();";
        view.evaluateJavascript(js, null);
    }

    private void loadHome() {
        errorLayout.setVisibility(View.GONE);
        progress.setVisibility(View.VISIBLE);
        webView.loadUrl(HOME_URL);
    }
}
