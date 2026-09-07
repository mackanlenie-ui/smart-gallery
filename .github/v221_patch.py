from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
old='getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE);'
new='getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE); if(android.os.Build.VERSION.SDK_INT>=33) setRecentsScreenshotEnabled(false);'
if old not in s: raise SystemExit('FLAG_SECURE anchor missing')
s=s.replace(old,new,1)
s=s.replace('Min Ekonomi v2.2','Min Ekonomi v2.2.1')
p.write_text(s,encoding='utf-8')
