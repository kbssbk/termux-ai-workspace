package com.beomsoo.sentenceapp;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.Window;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
  private WebView webView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Window window = getWindow();
    window.setStatusBarColor(Color.rgb(4, 15, 29));

    webView = new WebView(this);
    webView.setBackgroundColor(Color.rgb(4, 15, 29));
    WebSettings settings = webView.getSettings();
    settings.setJavaScriptEnabled(true);
    settings.setDomStorageEnabled(true);
    settings.setAllowFileAccess(true);
    settings.setJavaScriptCanOpenWindowsAutomatically(false);
    settings.setMediaPlaybackRequiresUserGesture(true);

    webView.addJavascriptInterface(new AndroidBridge(), "Android");
    webView.setWebViewClient(new WebViewClient() {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        String url = request.getUrl().toString();
        if (url.startsWith("http://") || url.startsWith("https://")) {
          openExternal(url);
          return true;
        }
        return false;
      }

      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
          openExternal(url);
          return true;
        }
        return false;
      }
    });
    setContentView(webView);
    webView.loadUrl("file:///android_asset/index.html");
  }

  private void openExternal(String url) {
    try {
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    } catch (Exception ignored) { }
  }

  @Override
  public void onBackPressed() {
    if (webView != null && webView.canGoBack()) webView.goBack();
    else super.onBackPressed();
  }

  public final class AndroidBridge {
    private static final int MAX_TEXT_BYTES = 8 * 1024 * 1024;
    private static final int MAX_BINARY_BYTES = 40 * 1024 * 1024;

    @JavascriptInterface
    public void openExternal(final String url) {
      if (!OfficialHostPolicy.isAllowed(url)) return;
      runOnUiThread(() -> MainActivity.this.openExternal(url));
    }

    @JavascriptInterface
    public String fetchText(String url) {
      return fetch(url, false, MAX_TEXT_BYTES);
    }

    @JavascriptInterface
    public String fetchBase64(String url) {
      return fetch(url, true, MAX_BINARY_BYTES);
    }

    private String fetch(String rawUrl, boolean base64, int maxBytes) {
      JSONObject out = new JSONObject();
      HttpURLConnection conn = null;
      try {
        if (!OfficialHostPolicy.isAllowed(rawUrl)) throw new SecurityException("허용되지 않은 공식 사이트 주소");
        URL url = new URL(rawUrl);
        conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(25000);
        conn.setRequestMethod("GET");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android) SentenceLibrary/2.0");
        conn.setRequestProperty("Accept-Language", "ko-KR,ko;q=0.9,en;q=0.5");
        int status = conn.getResponseCode();
        String finalUrl = conn.getURL().toString();
        if (!OfficialHostPolicy.isAllowed(finalUrl)) throw new SecurityException("공식 사이트 밖으로 이동한 요청 차단");
        out.put("status", status);
        out.put("url", finalUrl);
        if (status < 200 || status >= 300) {
          out.put("ok", false);
          out.put("error", "HTTP " + status);
          return out.toString();
        }
        byte[] bytes = readLimited(conn.getInputStream(), maxBytes);
        out.put("ok", true);
        out.put("body", base64 ? Base64.encodeToString(bytes, Base64.NO_WRAP) : new String(bytes, StandardCharsets.UTF_8));
        return out.toString();
      } catch (Exception e) {
        try {
          out.put("ok", false);
          out.put("error", e.getClass().getSimpleName() + ": " + String.valueOf(e.getMessage()));
        } catch (Exception ignored) { }
        return out.toString();
      } finally {
        if (conn != null) conn.disconnect();
      }
    }

    private byte[] readLimited(InputStream in, int maxBytes) throws Exception {
      try (InputStream input = in; ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
        byte[] buffer = new byte[16384];
        int total = 0;
        int n;
        while ((n = input.read(buffer)) != -1) {
          total += n;
          if (total > maxBytes) throw new IllegalStateException("응답 크기 제한 초과");
          bos.write(buffer, 0, n);
        }
        return bos.toByteArray();
      }
    }
  }
}
