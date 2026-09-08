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

    private static final String FILTER_JS = "(function(){" +
      "if(window.__fbAdFilterV11)return;window.__fbAdFilterV11=true;"+
      "var css='[aria-label*=Sponsored i],[aria-label*=Sponsrad i]{display:none!important}';"+
      "var s=document.getElementById('adfilter-style-v11');if(!s){s=document.createElement('style');s.id='adfilter-style-v11';s.innerHTML=css;document.documentElement.appendChild(s);}"+
      "function isMarkerText(t){t=(t||'').replace(/\\s+/g,' ').trim().toLowerCase();return t==='ad'||t==='sponsored'||t==='sponsrad';}"+
      "function hideCard(marker){"+
        "var card=marker.closest('[role=article],article');"+
        "if(!card){var n=marker,best=null;for(var i=0;i<9&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect();var txt=(n.innerText||'');if(r.width>window.innerWidth*0.72&&r.height>80&&r.height<window.innerHeight*3.2&&txt.length<9000)best=n;}card=best;}"+
        "if(card&&card!==document.body&&card!==document.documentElement){card.setAttribute('data-adfilter-hidden','1');card.style.setProperty('display','none','important');}"+
      "}"+
      "function clean(root){"+
        "root=root||document;"+
        "var nodes=root.querySelectorAll?root.querySelectorAll('span,div,a'):[];"+
        "for(var i=0;i<nodes.length;i++){var e=nodes[i];if(e.children.length>2)continue;var t=e.innerText||e.textContent||'';if(isMarkerText(t))hideCard(e);}"+
        "var arts=root.querySelectorAll?root.querySelectorAll('[role=article],article'):[];"+
        "for(var j=0;j<arts.length;j++){var a=arts[j];if(a.getAttribute('data-adfilter-hidden'))continue;var lines=(a.innerText||'').split(/\\n+/).map(function(x){return x.trim().toLowerCase();}).filter(Boolean).slice(0,12);if(lines.indexOf('ad')>=0||lines.indexOf('sponsored')>=0||lines.indexOf('sponsrad')>=0){a.setAttribute('data-adfilter-hidden','1');a.style.setProperty('display','none','important');}}"+
      "}"+
      "clean(document);"+
      "var mo=new MutationObserver(function(ms){for(var i=0;i<ms.length;i++){for(var j=0;j<ms[i].addedNodes.length;j++){var n=ms[i].addedNodes[j];if(n&&n.nodeType===1)clean(n);}}});"+
      "mo.observe(document.documentElement,{childList:true,subtree:true});"+
      "setInterval(function(){clean(document);},900);"+
      "})();";

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle b){ super.onCreate(b);
        FrameLayout root=new FrameLayout(this); web=new WebView(this); progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        root.addView(web,new FrameLayout.LayoutParams(-1,-1)); FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,6); root.addView(progress,p); setContentView(root);
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false); s.setSupportZoom(false); s.setUserAgentString(s.getUserAgentString()+" FBWebWrapper/1.1");
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}});
        web.setWebViewClient(new WebViewClient(){
          @Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);}
          @Override public void onPageFinished(WebView v,String u){v.evaluateJavascript(FILTER_JS,null);}
          @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString(); if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com")) return new WebResourceResponse("text/plain","utf-8",null); return super.shouldInterceptRequest(v,r);}
        });
        web.loadUrl("https://m.facebook.com/");
    }
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
