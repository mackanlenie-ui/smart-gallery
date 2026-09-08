from pathlib import Path

p = Path('facebook/src/main/java/se/mackan/fblite/MainActivity.java')
s = p.read_text(encoding='utf-8')

# Keep v2.8's working Facebook/DeX/ad-filter code intact and only add stability/polish.
s = s.replace('import android.content.SharedPreferences;\n', 'import android.content.SharedPreferences;\nimport android.app.DownloadManager;\nimport android.os.Environment;\n')
s = s.replace('import android.webkit.WebResourceRequest;\n', 'import android.webkit.WebResourceRequest;\nimport android.webkit.WebResourceError;\n')

s = s.replace('    private static final int FILE_PICKER=2001;\n', '    private static final int FILE_PICKER=2001;\n    private static final String APP_VERSION="2.9";\n')

old = '    private String homeUrl(){return dexMode?DESKTOP_HOME:MOBILE_HOME;}\n'
new = '''    private String homeUrl(){return dexMode?DESKTOP_HOME:MOBILE_HOME;}\n    private void configureDexSettings(){\n        if(web==null)return;\n        WebSettings s=web.getSettings();\n        s.setUseWideViewPort(dexMode);\n        s.setLoadWithOverviewMode(dexMode);\n        s.setTextZoom(dexMode?115:100);\n        if(dexMode)s.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36");\n        else s.setUserAgentString(null);\n    }\n    private void showLoadError(String msg){\n        if(web==null)return;\n        String bg=isDark()?"#111111":"#ffffff",fg=isDark()?"#ffffff":"#111111";\n        String html="<html><head><meta name=viewport content='width=device-width,initial-scale=1'></head><body style='font-family:sans-serif;background:"+bg+";color:"+fg+";display:flex;align-items:center;justify-content:center;height:100vh;margin:0'><div style='text-align:center;padding:28px'><h2>Facebook kunde inte laddas</h2><p>"+msg+"</p><p><a style='display:inline-block;padding:14px 22px;background:#1877f2;color:white;text-decoration:none;border-radius:10px' href='"+homeUrl()+"'>Försök igen</a></p></div></body></html>";\n        web.loadDataWithBaseURL(homeUrl(),html,"text/html","UTF-8",null);\n    }\n'''
if old not in s:
    raise SystemExit('homeUrl anchor not found')
s = s.replace(old, new)

s = s.replace('new AlertDialog.Builder(this).setTitle("Facebook Lite – inställningar")', 'new AlertDialog.Builder(this).setTitle("Facebook Lite v"+APP_VERSION+" – inställningar")')

old = 's.setUseWideViewPort(dexMode);s.setLoadWithOverviewMode(dexMode);if(dexMode){s.setTextZoom(115);s.setUserAgentString("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36");}'
new = 'configureDexSettings();'
if old not in s:
    raise SystemExit('WebSettings DeX anchor not found')
s = s.replace(old, new)

old = 'applyDexLayout();applyDexAds();applyPlayback();}@Override public boolean shouldOverrideUrlLoading'
new = '''applyDexLayout();applyDexAds();applyPlayback();}\n        @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){\n            super.onReceivedError(v,r,e);\n            if(r!=null&&r.isForMainFrame()){String m=e!=null&&e.getDescription()!=null?e.getDescription().toString():"Kontrollera internetanslutningen.";showLoadError(m);}\n        }\n        @Override public boolean shouldOverrideUrlLoading'''
if old not in s:
    raise SystemExit('WebViewClient anchor not found')
s = s.replace(old, new)

old = '}});web.loadUrl(homeUrl());}\n\n    @Override protected void onActivityResult'
new = '''}});\n      web.setDownloadListener((url,userAgent,contentDisposition,mimeType,contentLength)->{\n          try{\n              DownloadManager.Request req=new DownloadManager.Request(Uri.parse(url));\n              if(userAgent!=null)req.addRequestHeader("User-Agent",userAgent);\n              String cookie=CookieManager.getInstance().getCookie(url);if(cookie!=null)req.addRequestHeader("Cookie",cookie);\n              req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);\n              req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,"facebook-download-"+System.currentTimeMillis());\n              DownloadManager dm=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);if(dm!=null){dm.enqueue(req);Toast.makeText(this,"Nedladdning startad",Toast.LENGTH_SHORT).show();}\n          }catch(Exception e){Toast.makeText(this,"Kunde inte starta nedladdningen",Toast.LENGTH_SHORT).show();}\n      });\n      web.loadUrl(homeUrl());}\n\n    @Override protected void onActivityResult'''
if old not in s:
    raise SystemExit('download anchor not found')
s = s.replace(old, new)

old = '@Override protected void onResume(){super.onResume();if(web!=null){web.onResume();String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(homeUrl());else{applyTheme();applyPromoFilter();applyDexLayout();applyDexAds();web.evaluateJavascript(RESUME_JS,null);applyPlayback();}}}'
new = '''@Override protected void onResume(){super.onResume();if(web!=null){\n        boolean nd=detectDex()&&getBool("dex",true);\n        boolean changed=nd!=dexMode;dexMode=nd;configureDexSettings();web.onResume();\n        if(changed){web.loadUrl(homeUrl());return;}\n        String u=web.getUrl();if(u==null||u.equals("about:blank"))web.loadUrl(homeUrl());else{applyTheme();applyPromoFilter();applyDexLayout();applyDexAds();web.evaluateJavascript(RESUME_JS,null);applyPlayback();}\n    }}'''
if old not in s:
    raise SystemExit('onResume anchor not found')
s = s.replace(old, new)

old = '@Override protected void onDestroy(){CookieManager.getInstance().flush();super.onDestroy();}'
new = '@Override protected void onDestroy(){CookieManager.getInstance().flush();if(web!=null){web.stopLoading();web.setWebChromeClient(null);web.setWebViewClient(null);web.destroy();web=null;}super.onDestroy();}'
if old not in s:
    raise SystemExit('onDestroy anchor not found')
s = s.replace(old, new)

p.write_text(s, encoding='utf-8')
print('Applied Facebook Lite v2.9 stability/polish patch')
