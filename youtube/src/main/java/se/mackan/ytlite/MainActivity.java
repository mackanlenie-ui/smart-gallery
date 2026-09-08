package se.mackan.ytlite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.ActivityInfo;
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
import android.widget.FrameLayout;
import android.widget.ProgressBar;

public class MainActivity extends Activity {
 private WebView web; private ProgressBar progress;
 private static final String JS="(function(){var css='ytm-promoted-sparkles-web-renderer,ytm-companion-ad-renderer,ytm-display-ad-renderer,ytm-promoted-video-renderer,.ad-showing .ytp-ad-overlay-container,#player-ads,.ytp-ad-image-overlay{display:none!important}';var s=document.getElementById('adfilter-style');if(!s){s=document.createElement('style');s.id='adfilter-style';s.innerHTML=css;document.documentElement.appendChild(s);}function clean(){document.querySelectorAll('ytm-promoted-sparkles-web-renderer,ytm-companion-ad-renderer,ytm-display-ad-renderer,ytm-promoted-video-renderer').forEach(e=>e.remove());document.querySelectorAll('.ytp-ad-skip-button,.ytp-ad-skip-button-modern,.ytp-skip-ad-button').forEach(b=>b.click());var v=document.querySelector('video');if(v&&document.querySelector('.ad-showing')){try{v.currentTime=v.duration||v.currentTime;v.playbackRate=16;}catch(e){}}}clean();setInterval(clean,700);})();";
 @SuppressLint("SetJavaScriptEnabled") @Override public void onCreate(Bundle b){super.onCreate(b);FrameLayout root=new FrameLayout(this);web=new WebView(this);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);root.addView(web,new FrameLayout.LayoutParams(-1,-1));FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,6);root.addView(progress,p);setContentView(root);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setSupportZoom(false);s.setUserAgentString(s.getUserAgentString()+" YTWebWrapper/1.0");CookieManager.getInstance().setAcceptCookie(true);CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}});web.setWebViewClient(new WebViewClient(){@Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);}@Override public void onPageFinished(WebView v,String u){v.evaluateJavascript(JS,null);}@Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString();if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com"))return new WebResourceResponse("text/plain","utf-8",null);return super.shouldInterceptRequest(v,r);}});web.loadUrl("https://m.youtube.com/");}
 @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
