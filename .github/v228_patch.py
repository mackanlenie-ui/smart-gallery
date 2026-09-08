from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.7','Min Ekonomi v2.2.8')
# The overview actually calls addAccountSummaryV18(), so patch that implementation directly.
start=s.find('  void addAccountSummaryV18(){')
if start<0: raise SystemExit('addAccountSummaryV18 missing')
end=s.find('\n  void ',start+5)
if end<0: end=s.rfind('\n}')
block=s[start:end]
old='double bills=unpaidBillsBetween(todayCal(),pe[1]),save=plannedSavings(),free=bal-bills-save;'
new='double bills=unpaidBillsBetween(todayCal(),pe[1]),save=plannedSavings(),free=spendableNowV226();'
if old not in block: raise SystemExit('legacy free expression missing in addAccountSummaryV18')
block=block.replace(old,new,1)
old_label='TextView f=tv("Fritt kvar: "+fmt(free),23,true);'
new_label='TextView f=tv("Fritt att spendera: "+fmt(free),23,true);'
if old_label not in block: raise SystemExit('free label missing in addAccountSummaryV18')
block=block.replace(old_label,new_label,1)
s=s[:start]+block+s[end:]
p.write_text(s,encoding='utf-8')
