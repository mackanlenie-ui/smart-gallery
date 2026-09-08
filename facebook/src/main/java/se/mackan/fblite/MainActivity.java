package se.mackan.fblite;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
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
    private static final int FILE_PICKER = 2001;
    private static final String HOME = "https://m.facebook.com/";
    private long lastMessengerAttempt = 0L;
    private float downY;
    private boolean pullReady = false;

    private static final String FILTER_JS = "(function(){"+
      "if(window.__fbAdFilterV22)return;window.__fbAdFilterV22=true;"+
      "var css='[aria-label*=Sponsored i],[aria-label*=Sponsrad i],[aria-label*=Sponsrat i]{display:none!important}';"+
      "var s=document.getElementById('adfilter-style-v22');if(!s){s=document.createElement('style');s.id='adfilter-style-v22';s.innerHTML=css;document.documentElement.appendChild(s);}"+
      "function norm(t){return (t||'').replace(/\\s+/g,' ').trim().toLowerCase();}"+
      "function isMarkerText(t){t=norm(t);return t==='ad'||t==='sponsored'||t==='sponsrad'||t==='sponsrat';}"+
      "function hideCard(marker){var card=marker.closest('[role=article],article');if(!card){var n=marker,best=null;for(var i=0;i<9&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect(),txt=n.innerText||'';if(r.width>innerWidth*.72&&r.height>80&&r.height<innerHeight*3.2&&txt.length<9000)best=n;}card=best;}if(card&&card!==document.body&&card!==document.documentElement){card.setAttribute('data-adfilter-hidden','1');card.style.setProperty('display','none','important');}}"+
      "function hidePromoLeaf(e){if(!e||e===document.body||e===document.documentElement)return;var target=e.closest('a,button')||e,n=target;for(var i=0;i<5&&n&&n!==document.body;i++,n=n.parentElement){var r=n.getBoundingClientRect(),cs=getComputedStyle(n);if(r.height>0&&r.height<=180&&r.width>innerWidth*.68&&r.bottom>innerHeight-190&&(cs.position==='fixed'||cs.position==='sticky')){n.style.setProperty('display','none','important');return;}}target.style.setProperty('display','none','important');}"+
      "function hideAppModal(root){var all=root.querySelectorAll?root.querySelectorAll('[role=dialog],div'):[];for(var i=0;i<all.length;i++){var e=all[i],t=norm(e.innerText||e.textContent||'');if(t.indexOf('facebook är bättre i appen')>=0||t.indexOf('facebook is better in the app')>=0){var d=e.closest('[role=dialog]')||e,r=d.getBoundingClientRect();if(d!==document.body&&d!==document.documentElement&&r.height>120&&r.height<innerHeight*.95)d.style.setProperty('display','none','important');}}}"+
      "function cleanPromos(root){var nodes=root.querySelectorAll?root.querySelectorAll('a,button,span,div'):[];for(var i=0;i<nodes.length;i++){var e=nodes[i],t=norm(e.innerText||e.textContent||'');if(t==='open app'||t==='öppna appen'||t==='öppna app'||t==='skaffa appen'||t==='get app'){hidePromoLeaf(e);continue;}if(e.children.length<=2&&(t==='skaffa facebook för android och surfa snabbare.'||t==='skaffa facebook för android och surfa snabbare'||t==='get facebook for android and browse faster.'||t==='get facebook for android and browse faster'))hidePromoLeaf(e);}hideAppModal(root);}"+
      "function clean(root){root=root||document;cleanPromos(root);var nodes=root.querySelectorAll?root.querySelectorAll('span,div,a'):[];for(var i=0;i<nodes.length;i++){var e=nodes[i];if(e.children.length>2)continue;if(isMarkerText(e.innerText||e.textContent||''))hideCard(e);}var arts=root.querySelectorAll?root.querySelectorAll('[role=article],article'):[];for(var j=0;j<arts.length;j++){var a=arts[j];if(a.getAttribute('data-adfilter-hidden'))continue;var lines=(a.innerText||'').split(/\\n+/).map(function(x){return x.trim().toLowerCase();}).filter(Boolean).slice(0,14);if(lines.indexOf('ad')>=0||lines.indexOf('sponsored')>=0||lines.indexOf('sponsrad')>=0||lines.indexOf('sponsrat')>=0){a.setAttribute('data-adfilter-hidden','1');a.style.setProperty('display','none','important');}}}"+
      "function isMsgHref(h){h=(h||'').toLowerCase();return h.indexOf('fb-messenger://')===0||h.indexOf('messenger://')===0||h.indexOf('facebook.com/messages')>=0||h.indexOf('/messages')>=0||h.indexOf('messenger.com')>=0;}"+
      "document.addEventListener('click',function(ev){var a=ev.target&&ev.target.closest?ev.target.closest('a'):null;if(a&&isMsgHref(a.href)){ev.preventDefault();ev.stopPropagation();location.href='fbwrapper://open-messenger';return;}var el=ev.target&&ev.target.closest?ev.target.closest('a,button,[role=button]'):null;if(el){var t=norm(el.innerText||el.textContent||el.getAttribute('aria-label')||'');if(t==='messenger'||t==='meddelanden'||t==='messages'){ev.preventDefault();ev.stopPropagation();location.href='fbwrapper://open-messenger';}}},true);"+
      "function detectMessengerGate(){var t=norm(document.body&&document.body.innerText||'');if(t.indexOf('skaffa messenger-appen')>=0||t.indexOf('switch over to messenger')>=0||t.indexOf('get messenger to read and respond')>=0||t.indexOf('hämta messenger')>=0)location.href='fbwrapper://open-messenger';}"+
      "clean(document);detectMessengerGate();var mo=new MutationObserver(function(ms){for(var i=0;i<ms.length;i++){for(var j=0;j<ms[i].addedNodes.length;j++){var n=ms[i].addedNodes[j];if(n&&n.nodeType===1)clean(n);}}detectMessengerGate();});mo.observe(document.documentElement,{childList:true,subtree:true});setInterval(function(){clean(document);detectMessengerGate();},900);})();";

    private static final String DARK_ON_JS = "(function(){var id='fbwrapper-dark-v22',s=document.getElementById(id);if(!s){s=document.createElement('style');s.id=id;s.textContent='html,body{background:#121212!important;color:#e8e8e8!important} body>div,main,[role=main],[role=feed],[role=article],article,[role=navigation],[role=banner],header,nav,section{background-color:#121212!important;color:#e8e8e8!important} [role=article],article{border-color:#333!important} div,span,p,h1,h2,h3,h4,h5,h6,label{color:inherit} input,textarea,[contenteditable=true]{background:#242424!important;color:#fff!important;border-color:#444!important} a{color:#8ab4f8!important} [role=dialog]{background:#202020!important;color:#fff!important}';document.documentElement.appendChild(s);}document.documentElement.style.backgroundColor='#121212';document.body&&document.body.style.setProperty('background-color','#121212','important');})();";
    private static final String DARK_OFF_JS = "(function(){var s=document.getElementById('fbwrapper-dark-v22');if(s)s.remove();document.documentElement.style.backgroundColor='';if(document.body)document.body.style.removeProperty('background-color');})();";

    private boolean getBool(String key, boolean def){return prefs.getBoolean(key,def);}

    private void applyDarkMode(){
        boolean dark=getBool("dark",false);
        web.setBackgroundColor(dark?Color.rgb(18,18,18):Color.WHITE);
        getWindow().setStatusBarColor(dark?Color.rgb(18,18,18):Color.WHITE);
        getWindow().getDecorView().setSystemUiVisibility(dark?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        gear.setBackgroundColor(dark?0xCC222222:0xCCFFFFFF);
        gear.setTextColor(dark?Color.WHITE:Color.DKGRAY);
        if(web!=null)web.evaluateJavascript(dark?DARK_ON_JS:DARK_OFF_JS,null);
    }

    private boolean hasMessenger(){try{getPackageManager().getPackageInfo("com.facebook.orca",0);return true;}catch(Exception e){return false;}}

    private void openMessenger(){
        if(!getBool("messenger",true)){Toast.makeText(this,"Chatt är avstängd i inställningarna.",Toast.LENGTH_SHORT).show();return;}
        long now=System.currentTimeMillis();if(now-lastMessengerAttempt<1200)return;lastMessengerAttempt=now;
        if(!hasMessenger()){
            Toast.makeText(this,"Messenger är inte installerad.",Toast.LENGTH_SHORT).show();
            if(web!=null)web.postDelayed(()->{if(web.canGoBack())web.goBack();else web.loadUrl(HOME);},120);
            return;
        }
        try{Intent launch=getPackageManager().getLaunchIntentForPackage("com.facebook.orca");if(launch!=null){startActivity(launch);return;}Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse("fb-messenger://threads"));i.setPackage("com.facebook.orca");startActivity(i);}catch(Exception e){Toast.makeText(this,"Kunde inte öppna Messenger.",Toast.LENGTH_SHORT).show();}
    }

    private boolean isMessagesUrl(String url){if(url==null)return false;String u=url.toLowerCase();return u.equals("fbwrapper://open-messenger")||u.startsWith("fb-messenger://")||u.startsWith("messenger://")||u.contains("facebook.com/messages")||u.contains("messenger.com/")||u.contains("/messages?")||u.endsWith("/messages")||u.contains("/messages/");}
    private boolean isFacebookHost(Uri uri){String h=uri.getHost();if(h==null)return true;h=h.toLowerCase();return h.endsWith("facebook.com")||h.endsWith("fbcdn.net")||h.endsWith("fbsbx.com")||h.endsWith("messenger.com");}

    private boolean handleUrl(String url){
        if(url==null||url.isEmpty())return false;
        if(isMessagesUrl(url)){openMessenger();return true;}
        if(url.startsWith("intent://")){try{Intent parsed=Intent.parseUri(url,Intent.URI_INTENT_SCHEME);if("com.facebook.orca".equals(parsed.getPackage()))openMessenger();else web.loadUrl(HOME);}catch(Exception e){web.loadUrl(HOME);}return true;}
        try{Uri u=Uri.parse(url);if(getBool("external",true)&&(url.startsWith("http://")||url.startsWith("https://"))&&!isFacebookHost(u)){startActivity(new Intent(Intent.ACTION_VIEW,u));return true;}}catch(Exception ignored){}
        return false;
    }

    private void showSettings(){
        String[] labels={"Reklamfilter","Mörkt läge","Öppna externa länkar i webbläsaren","Messenger-hantering"};
        boolean[] checked={getBool("adblock",true),getBool("dark",false),getBool("external",true),getBool("messenger",true)};
        new AlertDialog.Builder(this).setTitle("Facebook Lite – inställningar").setMultiChoiceItems(labels,checked,(d,which,isChecked)->checked[which]=isChecked).setPositiveButton("Spara",(d,w)->{
            prefs.edit().putBoolean("adblock",checked[0]).putBoolean("dark",checked[1]).putBoolean("external",checked[2]).putBoolean("messenger",checked[3]).apply();
            applyDarkMode();
            if(web!=null)web.reload();
        }).setNeutralButton("Start",(d,w)->web.loadUrl(HOME)).setNegativeButton("Avbryt",null).show();
    }

    private Intent buildMediaPicker(WebChromeClient.FileChooserParams params){
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);
        String[] accept=params!=null?params.getAcceptTypes():null;boolean video=false,image=false;
        if(accept!=null){for(String a:accept){if(a==null)continue;if(a.contains("video"))video=true;if(a.contains("image"))image=true;}}
        if(video&&!image)i.setType("video/*");else if(image&&!video)i.setType("image/*");else{i.setType("*/*");i.putExtra(Intent.EXTRA_MIME_TYPES,new String[]{"image/*","video/*"});}
        if(params!=null&&params.getMode()==WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE)i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        return i;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override public void onCreate(Bundle b){super.onCreate(b);
        prefs=getSharedPreferences("fb_lite_prefs",MODE_PRIVATE);
        getWindow().setStatusBarColor(Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.WHITE);root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(0,insets.getSystemWindowInsetTop(),0,0);return insets;});root.requestApplyInsets();
        web=new WebView(this);progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
        gear=new TextView(this);gear.setText("⚙");gear.setTextSize(22);gear.setGravity(Gravity.CENTER);gear.setPadding(8,0,8,0);gear.setElevation(10f);gear.setOnClickListener(v->showSettings());
        root.addView(web,new FrameLayout.LayoutParams(-1,-1));FrameLayout.LayoutParams pp=new FrameLayout.LayoutParams(-1,6);root.addView(progress,pp);FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(64,64,Gravity.END|Gravity.BOTTOM);gp.setMargins(0,0,12,18);root.addView(gear,gp);setContentView(root);
        WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setDatabaseEnabled(true);s.setMediaPlaybackRequiresUserGesture(false);s.setSupportZoom(false);s.setCacheMode(WebSettings.LOAD_DEFAULT);s.setAllowFileAccess(true);s.setAllowContentAccess(true);
        CookieManager cm=CookieManager.getInstance();cm.setAcceptCookie(true);cm.setAcceptThirdPartyCookies(web,true);applyDarkMode();
        web.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_DOWN){downY=e.getY();pullReady=false;}else if(e.getAction()==MotionEvent.ACTION_MOVE&&web.getScrollY()==0&&e.getY()-downY>170)pullReady=true;else if(e.getAction()==MotionEvent.ACTION_UP&&pullReady){pullReady=false;Toast.makeText(this,"Uppdaterar…",Toast.LENGTH_SHORT).show();web.reload();}return false;});
        web.setWebChromeClient(new WebChromeClient(){@Override public void onProgressChanged(WebView v,int n){progress.setProgress(n);progress.setVisibility(n<100?View.VISIBLE:View.GONE);}@Override public boolean onShowFileChooser(WebView w,ValueCallback<Uri[]> cb,FileChooserParams params){if(fileCallback!=null)fileCallback.onReceiveValue(null);fileCallback=cb;try{startActivityForResult(buildMediaPicker(params),FILE_PICKER);return true;}catch(Exception e){fileCallback=null;Toast.makeText(MainActivity.this,"Kunde inte öppna bildväljaren.",Toast.LENGTH_SHORT).show();return false;}}});
        web.setWebViewClient(new WebViewClient(){
            @Override public void onPageStarted(WebView v,String u,Bitmap f){progress.setVisibility(View.VISIBLE);if(isMessagesUrl(u)){v.stopLoading();openMessenger();}}
            @Override public void onPageFinished(WebView v,String u){CookieManager.getInstance().flush();if(u==null||u.equals("about:blank")){v.postDelayed(()->v.loadUrl(HOME),250);return;}if(isMessagesUrl(u)){openMessenger();return;}if(getBool("adblock",true))v.evaluateJavascript(FILTER_JS,null);v.evaluateJavascript(getBool("dark",false)?DARK_ON_JS:DARK_OFF_JS,null);}
            @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){return handleUrl(r.getUrl().toString());}
            @Override public boolean shouldOverrideUrlLoading(WebView v,String u){return handleUrl(u);}
            @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){if(!getBool("adblock",true))return super.shouldInterceptRequest(v,r);String u=r.getUrl().toString();if(u.contains("doubleclick.net")||u.contains("googlesyndication.com")||u.contains("googleadservices.com"))return new WebResourceResponse("text/plain","utf-8",null);return super.shouldInterceptRequest(v,r);}
        });
        web.loadUrl(HOME);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){super.onActivityResult(requestCode,resultCode,data);if(requestCode!=FILE_PICKER||fileCallback==null)return;Uri[] result=null;if(resultCode==RESULT_OK&&data!=null){if(data.getClipData()!=null){int n=data.getClipData().getItemCount();result=new Uri[n];for(int i=0;i<n;i++)result[i]=data.getClipData().getItemAt(i).getUri();}else if(data.getData()!=null)result=new Uri[]{data.getData()};}fileCallback.onReceiveValue(result);fileCallback=null;}
    @Override protected void onPause(){CookieManager.getInstance().flush();if(web!=null)web.onPause();super.onPause();}
    @Override protected void onResume(){super.onResume();if(web!=null){web.onResume();String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(HOME);else applyDarkMode();}}
    @Override protected void onDestroy(){CookieManager.getInstance().flush();super.onDestroy();}
    @Override public void onBackPressed(){if(web!=null&&web.canGoBack())web.goBack();else super.onBackPressed();}
}
