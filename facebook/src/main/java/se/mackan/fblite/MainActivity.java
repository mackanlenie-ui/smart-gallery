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
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar progress;
    private static final String HOME = "https://m.facebook.com/";

    private static final String FILTER_JS = "(function(){" +
      "if(window.__fbAdFilterV18)return;window.__fbAdFilterV18=true;"+
      "var css='[aria-label*=Sponsored i],[aria-label*=Sponsrad i],[aria-label*=Sponsrat i]{display:none!important}';"+
      "var s=document.getElementById('adfilter-style-v18');if(!s){s=document.createElement('style');s.id='adfilter-style-v18';s.innerHTML=css;document.documentElement.appendChild(s);}"+
      "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"+
      "function isMarkerText(t){t=norm(t);return t==='ad'||t==='sponsored'||t==='sponsrad'||t==='sponsrat';}"+
      "function hideCard(marker){var card=marker.closest('[role=article],article');if(!card){var n=marker,best=null;for(var i=0;i<9&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect();var txt=(n.innerText||'');if(r.width>window.innerWidth*0.72&&r.height>80&&r.height<window.innerHeight*3.2&&txt.length<9000)best=n;}card=best;}if(card&&card!==document.body&&card!==document.documentElement){card.setAttribute('data-adfilter-hidden','1');card.style.setProperty('display','none','important');}}"+
      "function hidePromoLeaf(e){if(!e||e===document.body||e===document.documentElement)return;var target=e.closest('a,button');if(target){var r=target.getBoundingClientRect();if(r.height>0&&r.height<=160){target.style.setProperty('display','none','important');return;}}var p=e.parentElement;if(p&&p!==document.body){var pr=p.getBoundingClientRect();var pt=norm(p.innerText||p.textContent||'');if(pr.height>0&&pr.height<=160&&pt.length<220){p.style.setProperty('display','none','important');return;}}e.style.setProperty('display','none','important');}"+
      "function hideAppModal(root){var all=root.querySelectorAll?root.querySelectorAll('[role=dialog],div'):[];for(var i=0;i<all.length;i++){var e=all[i],t=norm(e.innerText||e.textContent||'');if(t.indexOf('facebook är bättre i appen')>=0||t.indexOf('facebook is better in the app')>=0){var d=e.closest('[role=dialog]')||e;var r=d.getBoundingClientRect();if(d!==document.body&&d!==document.documentElement&&r.height>120&&r.height<window.innerHeight*0.95){d.style.setProperty('display','none','important');}}}}"+
      "function cleanPromos(root){var nodes=root.querySelectorAll?root.querySelectorAll('a,button,span,div'):[];for(var i=0;i<nodes.length;i++){var e=nodes[i];if(e.children.length>2)continue;var t=norm(e.innerText||e.textContent||'');if(t==='open app'||t==='öppna app'||t==='skaffa appen'||t==='get app'||t==='skaffa facebook för android och surfa snabbare.'||t==='skaffa facebook för android och surfa snabbare'||t==='get facebook for android and browse faster.'||t==='get facebook for android and browse faster'){hidePromoLeaf(e);}}hideAppModal(root);}"+
      "function clean(root){root=root||document;cleanPromos(root);var nodes=root.querySelectorAll?root.querySelectorAll('span,div,a'):[];for(var i=0;i<nodes.length;i++){var e=nodes[i];if(e.children.length>2)continue;var t=e.innerText||e.textContent||'';if(isMarkerText(t))hideCard(e);}var arts=root.querySelectorAll?root.querySelectorAll('[role=article],article'):[];for(var j=0;j<arts.length;j++){var a=arts[j];if(a.getAttribute('data-adfilter-hidden'))continue;var lines=(a.innerText||'').split(/\\n+/).map(function(x){return x.trim().toLowerCase();}).filter(Boolean).slice(0,14);if(lines.indexOf('ad')>=0||lines.indexOf('sponsored')>=0||lines.indexOf('sponsrad')>=0||lines.indexOf('sponsrat')>=0){a.setAttribute('data-adfilter-hidden','1');a.style.setProperty('display','none','important');}}}"+
      "function isMsgHref(h){h=(h||'').toLowerCase();return h.indexOf('fb-messenger://')===0||h.indexOf('messenger://')===0||h.indexOf('facebook.com/messages')>=0||h.indexOf('/messages')>=0||h.indexOf('messenger.com')>=0;}"+
      "document.addEventListener('click',function(ev){var a=ev.target&&ev.target.closest?ev.target.closest('a'):null;if(a&&isMsgHref(a.href)){ev.preventDefault();ev.stopPropagation();location.href='fbwrapper://open-messenger';return;}var el=ev.target&&ev.target.closest?ev.target.closest('a,button,[role=button]'):null;if(el){var t=norm(el.innerText||el.textContent||el.getAttribute('aria-label')||'');if(t==='messenger'||t==='meddelanden'||t==='messages'){ev.preventDefault();ev.stopPropagation();location.href='fbwrapper://open-messenger';}}},true);"+
      "function detectMessengerGate(){var t=norm(document.body&&document.body.innerText||'');if(t.indexOf('skaffa messenger-appen')>=0||t.indexOf('switch over to messenger')>=0||t.indexOf('get messenger to read and respond')>=0){location.href='fbwrapper://open-messenger';}}"+
      "clean(document);detectMessengerGate();var mo=new MutationObserver(function(ms){for(var i=0;i<ms.length;i++){for(var j=0;j<ms[i].addedNodes.length;j++){var n=ms[i].addedNodes[j];if(n&&n.nodeType===1)clean(n);}}detectMessengerGate();});mo.observe(document.documentElement,{childList:true,subtree:true});setInterval(function(){clean(document);detectMessengerGate();},900);})();";

    private boolean hasMessenger() {
        try {
            getPackageManager().getPackageInfo("com.facebook.orca", 0);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void openMessenger() {
        if (!hasMessenger()) {
            Toast.makeText(this, "Messenger behövs för att öppna chatten.", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Intent launch = getPackageManager().getLaunchIntentForPackage("com.facebook.orca");
            if (launch != null) {
                startActivity(launch);
                return;
            }
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("fb-messenger://threads"));
            i.setPackage("com.facebook.orca");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "Kunde inte öppna Messenger.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isMessagesUrl(String url) {
        if (url == null) return false;
        String u = url.toLowerCase();
        return u.equals("fbwrapper://open-messenger") || u.startsWith("fb-messenger://") || u.startsWith("messenger://") ||
               u.contains("facebook.com/messages") || u.contains("messenger.com/") ||
               u.contains("/messages?") || u.endsWith("/messages") || u.contains("/messages/");
    }

    private boolean handleUrl(String url) {
        if (url == null || url.isEmpty()) return false;
        if (isMessagesUrl(url)) {
            openMessenger();
            return true;
        }
        if (url.startsWith("intent://")) {
            try {
                Intent parsed = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if ("com.facebook.orca".equals(parsed.getPackage())) openMessenger();
                else web.loadUrl(HOME);
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

        CookieManager cm=CookieManager.getInstance();
        cm.setAcceptCookie(true);
        cm.setAcceptThirdPartyCookies(web,true);

        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}});
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);if(isMessagesUrl(u)){v.stopLoading();openMessenger();}}
            @Override public void onPageFinished(WebView v,String u){
                CookieManager.getInstance().flush();
                if (u==null || u.equals("about:blank")) { v.postDelayed(() -> v.loadUrl(HOME), 250); return; }
                if(isMessagesUrl(u)){openMessenger();return;}
                v.evaluateJavascript(FILTER_JS,null);
            }
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return handleUrl(r.getUrl().toString());}
            @Override public boolean shouldOverrideUrlLoading(WebView v,String u){return handleUrl(u);}
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString(); if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com")) return new WebResourceResponse("text/plain","utf-8",null); return super.shouldInterceptRequest(v,r);}
        });
        web.loadUrl(HOME);
    }

    @Override protected void onPause(){CookieManager.getInstance().flush();if(web!=null) web.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null){web.onResume();String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(HOME);}}
    @Override protected void onDestroy(){CookieManager.getInstance().flush();super.onDestroy();}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
