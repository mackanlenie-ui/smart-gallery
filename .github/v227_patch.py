from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.6','Min Ekonomi v2.2.7')
# Pay-period card still used the old income/expense calculation. Replace the exact legacy expression.
old='double bills=unpaidBillsBetween(todayCal(),pe[1]),save=plannedSavings();double after=t[0]-t[1]-bills-save;'
new='double bills=unpaidBillsBetween(todayCal(),pe[1]),save=plannedSavings();double after=spendableNowV226();'
if old not in s: raise SystemExit('pay period anchor missing')
s=s.replace(old,new,1)
# Account summary still used raw account balance. Use the same spendable amount as the main overview.
old='double bills=unpaidBillsBetween(todayCal(),pe[1]),save=plannedSavings(),free=bal-bills-save;'
new='double bills=unpaidBillsBetween(todayCal(),pe[1]),save=plannedSavings(),free=spendableNowV226();'
if old not in s: raise SystemExit('account summary anchor missing')
s=s.replace(old,new,1)
# Clarify the label.
s=s.replace('TextView f=tv("Fritt kvar: "+fmt(free),23,true);','TextView f=tv("Fritt att spendera: "+fmt(free),23,true);',1)
p.write_text(s,encoding='utf-8')
