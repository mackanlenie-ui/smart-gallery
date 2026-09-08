package se.mackan.fblite;

import android.annotation.SuppressLint;
import android.app.Activity;
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
    private static final String MESSAGES_FALLBACK = "https://mbasic.facebook.com/messages/";
    private static final String CHROME_UA = "Mozilla/5.0 (Linux; Android 16; SM-S918B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Mobile Safari/537.36";

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

    private boolean openMessengerIfInstalled(String deepLink) {
        try {
            getPackageManager().getPackageInfo("com.facebook.orca", 0);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(deepLink));
            i.setPackage("com.facebook.orca");
            startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean handleUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        if (url.startsWith("fb-messenger://") || url.startsWith("messenger://")) {
            if (!openMessengerIfInstalled(url)) web.loadUrl(MESSAGES_FALLBACK);
            return true;
        }
        if (url.startsWith("intent://")) {
            try {
                Intent parsed = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                String pkg = parsed.getPackage();
                if ("com.facebook.orca".equals(pkg)) {
                    parsed.setPackage("com.facebook.orca");
                    startActivity(parsed);
                } else {
                    web.loadUrl(HOME);
                }
            } catch (Exception e) {
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
        WebSettings s=web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setSupportZoom(false);
        s.setCacheMode(WebSettings.LOAD_DEFAULT);
        s.setUserAgentString(CHROME_UA);

        CookieManager cm=CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web,true);

        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}});
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);}
            @Override public void onPageFinished(WebView v,String u){
                CookieManager.getInstance().flush();
                if (u==null || u.equals("about:blank")) { v.postDelayed(() -> v.loadUrl(HOME), 250); return; }
                v.evaluateJavascript(FILTER_JS,null);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return handleUrl(r.getUrl().toString());}
            @Override public boolean shouldOverrideUrlLoading(WebView v,String u){return handleUrl(u);}
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString(); if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com")) return new WebResourceResponse("text/plain","utf-8",null); return super.shouldInterceptRequest(v,r);}
        });
        web.loadUrl(HOME);
    }

    @Override protected void onPause(){
        CookieManager.getInstance().flush();
        if(web!=null) web.onPause();
        super.onPause();
    }

    @Override protected void onResume(){
        super.onResume();
        if(web!=null){web.onResume();String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(HOME);}
    }

    @Override protected void onDestroy(){
        CookieManager.getInstance().flush();
        super.onDestroy();
    }

    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
