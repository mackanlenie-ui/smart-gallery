package se.mackan.fblite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.FrameLayout;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar progress;
    private static final String FILTER_JS = "javascript:(function(){" +
      "var css='[aria-label*=Sponsored i],[aria-label*=Sponsrad i]{display:none!important}';"+
      "var s=document.getElementById('adfilter-style');if(!s){s=document.createElement('style');s.id='adfilter-style';s.innerHTML=css;document.documentElement.appendChild(s);}"+
      "function clean(){document.querySelectorAll('div,article').forEach(function(e){var t=(e.innerText||'').trim();if((t.indexOf('Sponsored')>=0||t.indexOf('Sponsrad')>=0)&&t.length<2200){var a=e.closest('[role=article]')||e.closest('article')||e;if(a)a.style.display='none';}});}clean();setInterval(clean,1500);})();";

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle b){ super.onCreate(b);
        FrameLayout root=new FrameLayout(this); web=new WebView(this); progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        root.addView(web,new FrameLayout.LayoutParams(-1,-1)); FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,6); root.addView(progress,p); setContentView(root);
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false); s.setSupportZoom(false); s.setUserAgentString(s.getUserAgentString()+" FBWebWrapper/1.0");
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}});
        web.setWebViewClient(new WebViewClient(){
          @Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);}
          @Override public void onPageFinished(WebView v,String u){v.evaluateJavascript(FILTER_JS.substring(11),null);}
          @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString(); if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com")) return new WebResourceResponse("text/plain","utf-8",null); return super.shouldInterceptRequest(v,r);}
        });
        web.loadUrl("https://m.facebook.com/");
    }
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
