from pathlib import Path

p = Path('facebook/src/main/java/se/mackan/fblite/MainActivity.java')
s = p.read_text(encoding='utf-8')

s = s.replace('    private static final String APP_VERSION="2.11";\n', '    private static final String APP_VERSION="2.12";\n')

# v2.11 waits for onPageCommitVisible. Keep that path, but add a guaranteed
# fallback so the WebView can never remain invisible on devices where Chromium
# does not deliver the callback reliably.
old = 'web.setVisibility(View.INVISIBLE);web.setAlpha(0f);root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));'
new = 'web.setVisibility(View.INVISIBLE);web.setAlpha(0f);root.addView(web,new FrameLayout.LayoutParams(-1,-1));root.addView(progress,new FrameLayout.LayoutParams(-1,6));web.postDelayed(()->{if(web!=null&&web.getVisibility()!=View.VISIBLE)revealWeb();},1000);'
if old not in s:
    raise SystemExit('v2.11 WebView startup anchor not found')
s = s.replace(old, new)

p.write_text(s, encoding='utf-8')
print('Applied Facebook Lite v2.12 startup reveal failsafe')
