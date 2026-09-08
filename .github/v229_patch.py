from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.8','Min Ekonomi v2.2.9')
old='int days=now.getActualMaximum(Calendar.DAY_OF_MONTH);double forecast=day>0?cur/day*days:cur;TextView pr=tv("Prognos för hela månaden: "+fmt(forecast),13,false);'
new='TextView pr=tv("Registrerade utgifter denna månad: "+fmt(cur),13,false);'
if old not in s: raise SystemExit('smart insights forecast anchor missing')
s=s.replace(old,new,1)
p.write_text(s,encoding='utf-8')
