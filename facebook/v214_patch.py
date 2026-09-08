from pathlib import Path

# Keep v2.13 behavior and only change startup rendering.
p = Path('facebook/src/main/java/se/mackan/fblite/MainActivity.java')
s = p.read_text(encoding='utf-8')
s = s.replace('    private static final String APP_VERSION="2.13";\n', '    private static final String APP_VERSION="2.14";\n')

# Never leave the WebView hidden while waiting for Chromium callbacks.
old = 'web.setVisibility(View.INVISIBLE);web.setAlpha(0f);root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));web.postDelayed(()->{if(web!=null&&web.getVisibility()!=View.VISIBLE)revealWeb();},1000);'
new = 'web.setVisibility(View.VISIBLE);web.setAlpha(1f);root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));'
if old not in s:
    raise SystemExit('v2.12 startup anchor not found')
s = s.replace(old, new)

# onPageCommitVisible should only dismiss the splash now; it must not fade the WebView from alpha 0.
old = '    private void revealWeb(){\n        if(web==null)return;\n        web.setVisibility(View.VISIBLE);\n        web.animate().cancel();\n        web.setAlpha(0f);\n        web.animate().alpha(1f).setDuration(120).withEndAction(this::hideSplash).start();\n    }\n'
new = '    private void revealWeb(){\n        if(web!=null){web.animate().cancel();web.setVisibility(View.VISIBLE);web.setAlpha(1f);}\n        hideSplash();\n    }\n'
if old not in s:
    raise SystemExit('revealWeb anchor not found')
s = s.replace(old, new)

# Use a Facebook-blue startup surface instead of a white/light splash.
old = '        boolean d=isDark();\n        splash.setBackgroundColor(d?Color.rgb(17,17,17):Color.WHITE);\n        splash.setTextColor(Color.rgb(24,119,242));\n'
new = '        splash.setBackgroundColor(Color.rgb(8,102,255));\n        splash.setTextColor(Color.WHITE);\n'
if old not in s:
    raise SystemExit('styleSplash anchor not found')
s = s.replace(old, new)

p.write_text(s, encoding='utf-8')

# Also remove Android's white launch-window preview before MainActivity draws.
sp = Path('facebook/src/main/res/values/styles.xml')
xml = sp.read_text(encoding='utf-8')
xml = xml.replace('<item name="android:windowLightStatusBar">false</item>', '<item name="android:windowLightStatusBar">false</item><item name="android:windowBackground">#0866FF</item><item name="android:windowDisablePreview">true</item>')
sp.write_text(xml, encoding='utf-8')

print('Applied Facebook Lite v2.14 startup flash fix')
