from pathlib import Path

p = Path('facebook/src/main/java/se/mackan/fblite/MainActivity.java')
s = p.read_text(encoding='utf-8')

# v2.11: keep v2.10 behavior intact and only prevent WebView from becoming visible
# before Chromium has committed the first rendered frame.
s = s.replace('    private static final String APP_VERSION="2.10";\n', '    private static final String APP_VERSION="2.11";\n')

anchor = '''    private void hideSplash(){\n        if(firstPageReady||splash==null)return;\n        firstPageReady=true;\n        splash.animate().alpha(0f).setDuration(180).withEndAction(()->{if(splash!=null)splash.setVisibility(View.GONE);}).start();\n    }\n'''
insert = anchor + '''    private void revealWeb(){\n        if(web==null)return;\n        web.setVisibility(View.VISIBLE);\n        web.animate().cancel();\n        web.setAlpha(0f);\n        web.animate().alpha(1f).setDuration(120).withEndAction(this::hideSplash).start();\n    }\n'''
if anchor not in s:
    raise SystemExit('hideSplash anchor not found')
s = s.replace(anchor, insert)

old = 'root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));'
new = 'web.setVisibility(View.INVISIBLE);web.setAlpha(0f);root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));'
if old not in s:
    raise SystemExit('web add anchor not found')
s = s.replace(old, new)

# v2.10 hid the splash from onPageFinished, which can happen before the first frame is committed.
s = s.replace('applyDexLayout();applyDexAds();applyPlayback();v.postDelayed(()->hideSplash(),120);}', 'applyDexLayout();applyDexAds();applyPlayback();}')

old = '        @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){\n'
new = '        @Override public void onPageCommitVisible(WebView v,String u){super.onPageCommitVisible(v,u);revealWeb();}\n        @Override public void onReceivedError(WebView v,WebResourceRequest r,WebResourceError e){\n'
if old not in s:
    raise SystemExit('WebViewClient error anchor not found')
s = s.replace(old, new)

# Keep the overlay on top while navigating during the initial startup only.
s = s.replace('web.setOnTouchListener((v,e)->{', 'web.setBackgroundColor(isDark()?Color.rgb(17,17,17):Color.WHITE);web.setOnTouchListener((v,e)->{')

p.write_text(s, encoding='utf-8')
print('Applied Facebook Lite v2.11 first-frame white-flash fix')
