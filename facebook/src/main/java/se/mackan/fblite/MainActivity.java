package se.mackan.fblite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private WebView web;
    private ProgressBar progress;
    private TextView gear;
    private SharedPreferences prefs;
    private ValueCallback<Uri[]> fileCallback;
    private static final int FILE_PICKER=2001;
    private static final String MOBILE_HOME="https://m.facebook.com/";
    private static final String DESKTOP_HOME="https://www.facebook.com/";
    private long lastMessengerAttempt=0L;
    private float downY;
    private boolean pullReady=false;
    private boolean dexMode=false;

    private static final String FILTER_JS="(function(){if(window.__fbAdFilterV25)return;window.__fbAdFilterV25=true;"+
      "var css='[aria-label*=Sponsored i],[aria-label*=Sponsrad i],[aria-label*=Sponsrat i],[data-adfilter-hidden=\\\"1\\\"]{display:none!important;margin:0!important;padding:0!important;height:0!important;min-height:0!important}';"+
      "var s=document.getElementById('adfilter-style-v25');if(!s){s=document.createElement('style');s.id='adfilter-style-v25';s.innerHTML=css;document.documentElement.appendChild(s);}"+
      "function norm(t){return(t||'').replace(/\\s+/g,' ').trim().toLowerCase()}function marker(t){t=norm(t);return t==='ad'||t==='sponsored'||t==='sponsrad'||t==='sponsrat'}"+
      "function hideCard(e){var c=e.closest('[role=article],article');if(!c){var n=e,b=null;for(var i=0;i<9&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect(),x=n.innerText||'';if(r.width>innerWidth*.58&&r.height>70&&r.height<innerHeight*3.2&&x.length<9000)b=n}c=b}if(c&&c!==document.body&&c!==document.documentElement)c.setAttribute('data-adfilter-hidden','1')}"+
      "function hidePromo(e){if(!e||e===document.body)return;var t=e.closest('a,button')||e,n=t;for(var i=0;i<5&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect(),cs=getComputedStyle(n);if(r.height>0&&r.height<=190&&r.width>innerWidth*.55&&r.bottom>innerHeight-200&&(cs.position==='fixed'||cs.position==='sticky')){n.style.setProperty('display','none','important');return}}t.style.setProperty('display','none','important')}"+
      "function clean(root){root=root||document;var q=root.querySelectorAll?root.querySelectorAll('span,div,a,button'):[];for(var i=0;i<q.length;i++){var e=q[i],t=norm(e.innerText||e.textContent||'');if(t==='open app'||t==='öppna appen'||t==='öppna app'||t==='skaffa appen'||t==='get app'){hidePromo(e);continue}if(t.indexOf('facebook är bättre i appen')>=0||t.indexOf('facebook is better in the app')>=0){var d=e.closest('[role=dialog]')||e;if(d!==document.body)d.style.setProperty('display','none','important')}if(e.children.length<=2&&marker(t))hideCard(e)}var a=root.querySelectorAll?root.querySelectorAll('[role=article],article'):[];for(var j=0;j<a.length;j++){var l=(a[j].innerText||'').split(/\\n+/).map(function(x){return x.trim().toLowerCase()}).filter(Boolean).slice(0,14);if(l.indexOf('ad')>=0||l.indexOf('sponsored')>=0||l.indexOf('sponsrad')>=0||l.indexOf('sponsrat')>=0)a[j].setAttribute('data-adfilter-hidden','1')}}"+
      "function msg(h){h=(h||'').toLowerCase();return h.indexOf('fb-messenger://')===0||h.indexOf('messenger://')===0||h.indexOf('facebook.com/messages')>=0||h.indexOf('/messages')>=0||h.indexOf('messenger.com')>=0}"+
      "document.addEventListener('click',function(ev){var a=ev.target&&ev.target.closest?ev.target.closest('a'):null;if(a&&msg(a.href)){ev.preventDefault();ev.stopPropagation();location.href='fbwrapper://open-messenger'}},true);clean(document);new MutationObserver(function(ms){for(var i=0;i<ms.length;i++)for(var j=0;j<ms[i].addedNodes.length;j++){var n=ms[i].addedNodes[j];if(n&&n.nodeType===1)clean(n)}}).observe(document.documentElement,{childList:true,subtree:true});setInterval(function(){clean(document)},1200)})();";

    private static final String DARK_ON_JS="(function(){var id='fbwrapper-dark-v25',s=document.getElementById(id);if(!s){s=document.createElement('style');s.id=id;s.textContent='html{background:#111!important;filter:invert(1) hue-rotate(180deg)!important}body{background:#fff!important}img,video,picture,canvas,svg image,[style*=background-image],iframe{filter:invert(1) hue-rotate(180deg)!important}';document.documentElement.appendChild(s)}})();";
    private static final String DARK_OFF_JS="(function(){var s=document.getElementById('fbwrapper-dark-v25');if(s)s.remove()})();";
    private static final String DEX_LAYOUT_JS="(function(){var id='fbwrapper-dex-v25',s=document.getElementById(id);if(!s){s=document.createElement('style');s.id=id;s.textContent='html,body{min-width:100%!important;width:100%!important}body{margin:0 auto!important}body>div:first-child,#root,[role=main]{width:100%!important;max-width:1180px!important;margin-left:auto!important;margin-right:auto!important}main,[role=main]{padding-left:24px!important;padding-right:24px!important;box-sizing:border-box!important}[role=feed]{width:min(760px,72vw)!important;max-width:760px!important;margin-left:auto!important;margin-right:auto!important}[role=article],article{max-width:760px!important;margin-left:auto!important;margin-right:auto!important}';document.documentElement.appendChild(s)}var m=document.querySelector('meta[name=viewport]');if(m)m.setAttribute('content','width=device-width, initial-scale=1.0, maximum-scale=1.0');})();";
    private static final String DEX_OFF_JS="(function(){var s=document.getElementById('fbwrapper-dex-v25');if(s)s.remove()})();";
    private static final String PAUSE_JS="(function(){document.documentElement.style.setProperty('animation-play-state','paused','important');document.querySelectorAll('video').forEach(function(v){try{v.pause()}catch(e){}})})();";
    private static final String RESUME_JS="(function(){document.documentElement.style.removeProperty('animation-play-state')})();";

    private boolean getBool(String k,boolean d){return prefs.getBoolean(k,d);} private int getInt(String k,int d){return prefs.getInt(k,d);}
    private boolean systemDark(){return (getResources().getConfiguration().uiMode&Configuration.UI_MODE_NIGHT_MASK)==Configuration.UI_MODE_NIGHT_YES;}
    private boolean isDark(){int m=getInt("theme",0);return m==2||(m==0&&systemDark());}
    private boolean isWifi(){try{ConnectivityManager cm=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);if(cm==null)return false;NetworkCapabilities c=cm.getNetworkCapabilities(cm.getActiveNetwork());return c!=null&&c.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);}catch(Exception e){return false;}}
    private boolean autoplayAllowed(){int m=getInt("autoplay",1);return m==2||(m==1&&isWifi());}
    private boolean detectDex(){Configuration c=getResources().getConfiguration();return c.smallestScreenWidthDp>=600&&c.screenWidthDp>=700;}
    private String homeUrl(){return dexMode?DESKTOP_HOME:MOBILE_HOME;}

    private void applyTheme(){boolean d=isDark();web.setBackgroundColor(d?Color.rgb(17,17,17):Color.WHITE);getWindow().setStatusBarColor(d?Color.rgb(17,17,17):Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(d?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);gear.setBackgroundColor(d?0xCC222222:0xCCFFFFFF);gear.setTextColor(d?Color.WHITE:Color.DKGRAY);if(web!=null)web.evaluateJavascript(d?DARK_ON_JS:DARK_OFF_JS,null);}
    private void applyDexLayout(){if(web!=null)web.evaluateJavascript(dexMode?DEX_LAYOUT_JS:DEX_OFF_JS,null);}
    private void applyPlayback(){if(web==null)return;String js=autoplayAllowed()?"(function(){document.querySelectorAll('video').forEach(function(v){v.preload='metadata'})})();":"(function(){document.querySelectorAll('video').forEach(function(v){v.autoplay=false;v.preload='metadata';try{v.pause()}catch(e){}})})();";web.evaluateJavascript(js,null);}

    private boolean hasMessenger(){try{getPackageManager().getPackageInfo("com.facebook.orca",0);return true;}catch(Exception e){return false;}}
    private void openMessenger(){if(!getBool("messenger",true)){Toast.makeText(this,"Chatt är avstängd.",Toast.LENGTH_SHORT).show();return;}long n=System.currentTimeMillis();if(n-lastMessengerAttempt<1200)return;lastMessengerAttempt=n;if(!hasMessenger()){Toast.makeText(this,"Messenger är inte installerad.",Toast.LENGTH_SHORT).show();if(web!=null)web.postDelayed(()->{if(web.canGoBack())web.goBack();else web.loadUrl(homeUrl());},120);return;}try{Intent i=getPackageManager().getLaunchIntentForPackage("com.facebook.orca");if(i!=null)startActivity(i);}catch(Exception e){Toast.makeText(this,"Kunde inte öppna Messenger.",Toast.LENGTH_SHORT).show();}}
    private boolean isMessagesUrl(String u){if(u==null)return false;u=u.toLowerCase();return u.equals("fbwrapper://open-messenger")||u.startsWith("fb-messenger://")||u.startsWith("messenger://")||u.contains("facebook.com/messages")||u.contains("messenger.com/")||u.contains("/messages?")||u.endsWith("/messages")||u.contains("/messages/");}
    private boolean isFacebookHost(Uri u){String h=u.getHost();if(h==null)return true;h=h.toLowerCase();return h.endsWith("facebook.com")||h.endsWith("fbcdn.net")||h.endsWith("fbsbx.com")||h.endsWith("messenger.com");}
    private boolean handleUrl(String u){if(u==null||u.isEmpty())return false;if(isMessagesUrl(u)){openMessenger();return true;}if(u.startsWith("intent://")){try{Intent p=Intent.parseUri(u,Intent.URI_INTENT_SCHEME);if("com.facebook.orca".equals(p.getPackage()))openMessenger();else web.loadUrl(homeUrl());}catch(Exception e){web.loadUrl(homeUrl());}return true;}try{Uri x=Uri.parse(u);if(getBool("external",true)&&(u.startsWith("http://")||u.startsWith("https://"))&&!isFacebookHost(x)){startActivity(new Intent(Intent.ACTION_VIEW,x));return true;}}catch(Exception ignored){}return false;}

    private void showTheme(){String[] a={"Följ systemet","Ljust","Mörkt"};int c=getInt("theme",0);new AlertDialog.Builder(this).setTitle("Tema").setSingleChoiceItems(a,c,(d,w)->{prefs.edit().putInt("theme",w).apply();d.dismiss();applyTheme();web.reload();}).show();}
    private void showAutoplay(){String[] a={"Av","Endast Wi-Fi","Alltid"};int c=getInt("autoplay",1);new AlertDialog.Builder(this).setTitle("Automatisk video").setSingleChoiceItems(a,c,(d,w)->{prefs.edit().putInt("autoplay",w).apply();d.dismiss();applyPlayback();}).show();}
    private void showSettings(){String[] l={"Reklamfilter","Öppna externa länkar i webbläsaren","Messenger-hantering","Batterispar i bakgrunden","DeX-anpassning på bred skärm"};boolean[] c={getBool("adblock",true),getBool("external",true),getBool("messenger",true),getBool("battery",true),getBool("dex",true)};new AlertDialog.Builder(this).setTitle("Facebook Lite – inställningar").setMultiChoiceItems(l,c,(d,w,b)->c[w]=b).setPositiveButton("Spara",(d,w)->{prefs.edit().putBoolean("adblock",c[0]).putBoolean("external",c[1]).putBoolean("messenger",c[2]).putBoolean("battery",c[3]).putBoolean("dex",c[4]).apply();dexMode=detectDex()&&getBool("dex",true);applyDexLayout();}).setNeutralButton("Tema",(d,w)->showTheme()).setNegativeButton("Video",(d,w)->showAutoplay()).show();}

    private Intent buildMediaPicker(WebChromeClient.FileChooserParams p){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);String[] a=p!=null?p.getAcceptTypes():null;boolean v=false,m=false;if(a!=null)for(String x:a){if(x==null)continue;if(x.contains("video"))v=true;if(x.contains("image"))m=true;}if(v&&!m)i.setType("video/*");else if(m&&!v)i.setType("image/*");else{i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});}if(p!=null&&p.getMode()==WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);return i;}

    @SuppressLint("SetJavaScriptEnabled") @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("fb_lite_prefs",MODE_PRIVATE);dexMode=detectDex()&&getBool("dex",true);FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.WHITE);root.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(0,i.getSystemWindowInsetTop(),0,0);return i;});root.requestApplyInsets();web=new WebView(this);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);gear=new TextView(this);gear.setText("⚙");gear.setTextSize(22);gear.setGravity(Gravity.CENTER);gear.setPadding(8,0,8,0);gear.setElevation(10f);gear.setOnClickListener(v->showSettings());root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(64,64,Gravity.END|Gravity.BOTTOM);gp.setMargins(0,0,12,18);root.addView(gear,gp);setContentView(root);WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setSupportZoom(false);s.setCacheMode(WebSettings.LOAD_DEFAULT);s.setAllowFileAccess(true);s.setAllowContentAccess(true);s.setUseWideViewPort(dexMode);s.setLoadWithOverviewMode(dexMode);if(dexMode){s.setTextZoom(115);s.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36");}CookieManager cm=CookieManager.getInstance();cm.setAcceptCookie(true);cm.setAcceptThirdPartyCookies(web,true);applyTheme();
      web.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getY();pullReady=false;}else if(e.getAction()==MotionEvent.ACTION_MOVE&&web.getScrollY()==0&&e.getY()-downY>170)pullReady=true;else if(e.getAction()==MotionEvent.ACTION_UP&&pullReady){pullReady=false;Toast.makeText(this,"Uppdaterar…",Toast.LENGTH_SHORT).show();web.reload();}return false;});
      web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}@Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams p){if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=cb;try{startActivityForResult(buildMediaPicker(p),FILE_PICKER);return true;}catch(Exception e){fileCallback=null;return false;}}});
      web.setWebViewClient(new WebViewClient(){@Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);if(isMessagesUrl(u)){v.stopLoading();openMessenger();}}@Override public void onPageFinished(WebView v,String u){CookieManager.getInstance().flush();if(u==null||u.equals("about:blank")){v.postDelayed(()->v.loadUrl(homeUrl()),250);return;}if(isMessagesUrl(u)){openMessenger();return;}if(getBool("adblock",true))v.evaluateJavascript(FILTER_JS,null);v.evaluateJavascript(isDark()?DARK_ON_JS:DARK_OFF_JS,null);applyDexLayout();applyPlayback();}@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return handleUrl(r.getUrl().toString());}@Override public boolean shouldOverrideUrlLoading(WebView v,String u){return handleUrl(u);}@Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){String u=r.getUrl().toString().toLowerCase();if(getBool("adblock",true)&&(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com")||u.contains("connect.facebook.net")||u.contains("facebook.com/tr/")||u.contains("facebook.com/ajax/bz")))return new WebResourceResponse("text/plain","utf-8",null);return super.shouldInterceptRequest(v,r);}});web.loadUrl(homeUrl());}

    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(r!=FILE_PICKER||fileCallback==null)return;Uri[] x=null;if(c==RESULT_OK&&d!=null){if(d.getClipData()!=null){int n=d.getClipData().getItemCount();x=new Uri[n];for(int i=0;i<n;i++)x[i]=d.getClipData().getItemAt(i).getUri();}else if(d.getData()!=null)x=new Uri[]{d.getData()};}fileCallback.onReceiveValue(x);fileCallback=null;}
    @Override protected void onPause(){CookieManager.getInstance().flush();if(web!=null){if(getBool("battery",true))web.evaluateJavascript(PAUSE_JS,null);web.onPause();}super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null){web.onResume();String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(homeUrl());else{applyTheme();applyDexLayout();web.evaluateJavascript(RESUME_JS,null);applyPlayback();}}}
    @Override protected void onDestroy(){CookieManager.getInstance().flush();super.onDestroy();}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
