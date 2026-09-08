from pathlib import Path

p = Path('facebook/src/main/java/se/mackan/fblite/MainActivity.java')
s = p.read_text(encoding='utf-8')

# Build on top of the v2.9 patch. Only remove the startup white flash.
s = s.replace('    private TextView gear;\n', '    private TextView gear;\n    private TextView splash;\n')
s = s.replace('    private boolean dexMode=false;\n', '    private boolean dexMode=false;\n    private boolean firstPageReady=false;\n')
s = s.replace('    private static final String APP_VERSION="2.9";\n', '    private static final String APP_VERSION="2.10";\n')

anchor = '    private void applyTheme(){boolean d=isDark();web.setBackgroundColor(d?Color.rgb(17,17,17):Color.WHITE);getWindow().setStatusBarColor(d?Color.rgb(17,17,17):Color.WHITE);getWindow().getDecorView().setSystemUiVisibility(d?0:View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);gear.setBackgroundColor(d?0xCC222222:0xCCFFFFFF);gear.setTextColor(d?Color.WHITE:Color.DKGRAY);if(web!=null)web.evaluateJavascript(d?DARK_ON_JS:DARK_OFF_JS,null);}\n'
insert = anchor + '''    private void styleSplash(){\n        if(splash==null)return;\n        boolean d=isDark();\n        splash.setBackgroundColor(d?Color.rgb(17,17,17):Color.WHITE);\n        splash.setTextColor(Color.rgb(24,119,242));\n    }\n    private void hideSplash(){\n        if(firstPageReady||splash==null)return;\n        firstPageReady=true;\n        splash.animate().alpha(0f).setDuration(180).withEndAction(()->{if(splash!=null)splash.setVisibility(View.GONE);}).start();\n    }\n'''
if anchor not in s:
    raise SystemExit('applyTheme anchor not found')
s = s.replace(anchor, insert)

old = 'prefs=getSharedPreferences("fb_lite_prefs",MODE_PRIVATE);dexMode=detectDex()&&getBool("dex",true);FrameLayout root=new FrameLayout(this);root.setBackgroundColor(Color.WHITE);'
new = 'prefs=getSharedPreferences("fb_lite_prefs",MODE_PRIVATE);dexMode=detectDex()&&getBool("dex",true);FrameLayout root=new FrameLayout(this);root.setBackgroundColor(isDark()?Color.rgb(17,17,17):Color.WHITE);'
if old not in s:
    raise SystemExit('root background anchor not found')
s = s.replace(old, new)

old = 'root.addView(progress,new FrameLayout.LayoutParams(-1,6));FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(64,64,Gravity.END|Gravity.BOTTOM);gp.setMargins(0,0,12,18);root.addView(gear,gp);setContentView(root);'
new = '''root.addView(progress,new FrameLayout.LayoutParams(-1,6));FrameLayout.LayoutParams gp=new FrameLayout.LayoutParams(64,64,Gravity.END|Gravity.BOTTOM);gp.setMargins(0,0,12,18);root.addView(gear,gp);\n      splash=new TextView(this);splash.setText("facebook");splash.setTextSize(dexMode?34:30);splash.setGravity(Gravity.CENTER);splash.setElevation(30f);styleSplash();root.addView(splash,new FrameLayout.LayoutParams(-1,-1));\n      setContentView(root);'''
if old not in s:
    raise SystemExit('splash view anchor not found')
s = s.replace(old, new)

old = 'applyDexLayout();applyDexAds();applyPlayback();}\n        @Override public void onReceivedError'
new = 'applyDexLayout();applyDexAds();applyPlayback();v.postDelayed(()->hideSplash(),120);}\n        @Override public void onReceivedError'
if old not in s:
    raise SystemExit('page finished anchor not found')
s = s.replace(old, new)

old = 'if(r!=null&&r.isForMainFrame()){String m=e!=null&&e.getDescription()!=null?e.getDescription().toString():"Kontrollera internetanslutningen.";showLoadError(m);}'
new = 'if(r!=null&&r.isForMainFrame()){hideSplash();String m=e!=null&&e.getDescription()!=null?e.getDescription().toString():"Kontrollera internetanslutningen.";showLoadError(m);}'
if old not in s:
    raise SystemExit('error anchor not found')
s = s.replace(old, new)

# Keep overlay/background synced if theme changes before first page is shown.
s = s.replace('private void showTheme(){String[] a={"Följ systemet","Ljust","Mörkt"};int c=getInt("theme",0);new AlertDialog.Builder(this).setTitle("Tema").setSingleChoiceItems(a,c,(d,w)->{prefs.edit().putInt("theme",w).apply();d.dismiss();applyTheme();web.reload();}).show();}',
'''private void showTheme(){String[] a={"Följ systemet","Ljust","Mörkt"};int c=getInt("theme",0);new AlertDialog.Builder(this).setTitle("Tema").setSingleChoiceItems(a,c,(d,w)->{prefs.edit().putInt("theme",w).apply();d.dismiss();applyTheme();styleSplash();web.reload();}).show();}''')

p.write_text(s, encoding='utf-8')
print('Applied Facebook Lite v2.10 startup splash fix')
