package se.mackan.fblite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
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
    private static final String HOME = "https://m.facebook.com/";
    private static final String MESSAGES = "https://m.facebook.com/messages/";

    private static final String FILTER_JS = "(function(){" +
      "if(window.__fbAdFilterV13)return;window.__fbAdFilterV13=true;"+
      "var css='[aria-label*=Sponsored i],[aria-label*=Sponsrad i]{display:none!important}';"+
      "var s=document.getElementById('adfilter-style-v13');if(!s){s=document.createElement('style');s.id='adfilter-style-v13';s.innerHTML=css;document.documentElement.appendChild(s);}"+
      "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"+
      "function isMarkerText(t){t=norm(t);return t==='ad'||t==='sponsored'||t==='sponsrad';}"+
      "function hideCard(marker){var card=marker.closest('[role=article],article');if(!card){var n=marker,best=null;for(var i=0;i<9&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect();var txt=(n.innerText||'');if(r.width>window.innerWidth*0.72&&r.height>80&&r.height<window.innerHeight*3.2&&txt.length<9000)best=n;}card=best;}if(card&&card!==document.body&&card!==document.documentElement){card.setAttribute('data-adfilter-hidden','1');card.style.setProperty('display','none','important');}}"+
      "function hidePromoLeaf(e){if(!e||e===document.body||e===document.documentElement)return;var target=e.closest('a,button');if(target){var r=target.getBoundingClientRect();if(r.height>0&&r.height<=140){target.style.setProperty('display','none','important');return;}}var p=e.parentElement;if(p&&p!==document.body){var pr=p.getBoundingClientRect();var pt=norm(p.innerText||p.textContent||'');if(pr.height>0&&pr.height<=140&&pt.length<180){p.style.setProperty('display','none','important');return;}}e.style.setProperty('display','none','important');}"+
      "function cleanPromos(root){var nodes=root.querySelectorAll?root.querySelectorAll('a,button,span,div'):[];for(var i=0;i<nodes.length;i++){var e=nodes[i];if(e.children.length>2)continue;var t=norm(e.innerText||e.textContent||'');if(t==='open app'||t==='öppna app'||t==='skaffa facebook för android och surfa snabbare.'||t==='skaffa facebook för android och surfa snabbare'||t==='get facebook for android and browse faster.'||t==='get facebook for android and browse faster'){hidePromoLeaf(e);}}}"+
      "function clean(root){root=root||document;cleanPromos(root);var nodes=root.querySelectorAll?root.querySelectorAll('span,div,a'):[];for(var i=0;i<nodes.length;i++){var e=nodes[i];if(e.children.length>2)continue;var t=e.innerText||e.textContent||'';if(isMarkerText(t))hideCard(e);}var arts=root.querySelectorAll?root.querySelectorAll('[role=article],article'):[];for(var j=0;j<arts.length;j++){var a=arts[j];if(a.getAttribute('data-adfilter-hidden'))continue;var lines=(a.innerText||'').split(/\\n+/).map(function(x){return x.trim().toLowerCase();}).filter(Boolean).slice(0,12);if(lines.indexOf('ad')>=0||lines.indexOf('sponsored')>=0||lines.indexOf('sponsrad')>=0){a.setAttribute('data-adfilter-hidden','1');a.style.setProperty('display','none','important');}}}"+
      "clean(document);var mo=new MutationObserver(function(ms){for(var i=0;i<ms.length;i++){for(var j=0;j<ms[i].addedNodes.length;j++){var n=ms[i].addedNodes[j];if(n&&n.nodeType===1)clean(n);}}});mo.observe(document.documentElement,{childList:true,subtree:true});setInterval(function(){clean(document);},900);})();";

    private boolean handleUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        if (url.startsWith("fb-messenger://") || url.startsWith("messenger://")) {
            try {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(i);
            } catch (ActivityNotFoundException ex) {
                web.loadUrl(MESSAGES);
            }
            return true;
        }
        if (url.startsWith("intent://")) {
            try {
                Intent i = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                startActivity(i);
            } catch (Exception ex) {
                web.loadUrl(HOME);
            }
            return true;
        }
        return false;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle b){ super.onCreate(b);
        FrameLayout root=new FrameLayout(this); web=new WebView(this); progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        root.addView(web,new FrameLayout.LayoutParams(-1,-1)); FrameLayout.LayoutParams p=new FrameLayout.LayoutParams(-1,6); root.addView(progress,p); setContentView(root);
        WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setDatabaseEnabled(true); s.setMediaPlaybackRequiresUserGesture(false); s.setSupportZoom(false); s.setUserAgentString(s.getUserAgentString()+" FBWebWrapper/1.4");
        CookieManager.getInstance().setAcceptCookie(true); CookieManager.getInstance().setAcceptThirdPartyCookies(web,true);
        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}});
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);}
            @Override public void onPageFinished(WebView v,String u){
                if (u==null || u.equals("about:blank")) { v.postDelayed(() -> v.loadUrl(HOME), 250); return; }
                v.evaluateJavascript(FILTER_JS,null);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return handleUrl(r.getUrl().toString());}
            @Override public boolean shouldOverrideUrlLoading(WebView v,String u){return handleUrl(u);}
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString(); if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com")) return new WebResourceResponse("text/plain","utf-8",null); return super.shouldInterceptRequest(v,r);}
        });
        web.loadUrl(HOME);
    }
    @Override protected void onResume(){super.onResume();if(web!=null){String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(HOME);}}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
