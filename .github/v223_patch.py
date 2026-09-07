from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.2','Min Ekonomi v2.2.3')
# Give the CSV export action its own visual group: 14dp above and 22dp below.
needle='csvout.setPadding(csvout.getPaddingLeft(),dp(14),csvout.getPaddingRight(),csvout.getPaddingBottom());csvout.setOnClickListener(v->exportCsvV19());content.addView(csvout);'
repl='csvout.setPadding(csvout.getPaddingLeft(),dp(14),csvout.getPaddingRight(),csvout.getPaddingBottom());LinearLayout.LayoutParams csvLp=new LinearLayout.LayoutParams(-1,-2);csvLp.setMargins(0,dp(14),0,dp(22));csvout.setLayoutParams(csvLp);csvout.setOnClickListener(v->exportCsvV19());content.addView(csvout);'
if needle not in s: raise SystemExit('v2.2.2 CSV spacing anchor missing')
s=s.replace(needle,repl,1)
p.write_text(s,encoding='utf-8')
