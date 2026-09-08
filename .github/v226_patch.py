from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.5','Min Ekonomi v2.2.6')
# One source of truth: current account balance minus unpaid bills minus the still-unfunded part of planned goal savings.
insert=s.rfind('\n}')
extra=r'''
  double goalPlanRemainingV226(){double planned=goalPlannedMonthlyV224(),actual=0;JSONArray goals=arr("goals");for(int i=0;i<goals.length();i++){JSONObject g=goals.optJSONObject(i);if(g!=null)actual+=goalSavedThisMonthV225(g.optString("name"));}return Math.max(0,planned-actual);}
  double spendableNowV226(){double bal=0;JSONArray a=arr("accounts");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null)bal+=o.optDouble("start")+accountFlow(o.optString("name"));}String mk=monthKey(Calendar.getInstance());return bal-unpaidBills(mk)-plannedSavings()-goalPlanRemainingV226();}
'''
s=s[:insert]+extra+s[insert:]
# Löneperiod: replace its old income-expense based remainder with the common live spendable value.
s=s.replace('double after=t[0]-t[1]-bills-plannedSavings()-goalPlannedMonthlyV224();','double after=spendableNowV226();',1)
# Account summary free balance: use the exact same number.
s=s.replace('double free=cur[0]-cur[1]-unpaidBills(mk)-plannedSavings()-goalPlannedMonthlyV224();','double free=spendableNowV226();',1)
# Main overview Kvar att spendera for current month: use the same source; historical months keep their historical calculation.
s=s.replace('double bills=unpaidBills(mk),spendable=t[0]-t[1]-bills-(monthOffset==0?goalPlannedMonthlyV224():0);','double bills=unpaidBills(mk),spendable=monthOffset==0?spendableNowV226():t[0]-t[1]-bills;',1)
# v2.2 smart forecast should also use the common current balance logic.
s=s.replace('return t[0]-t[1]-unpaidBills(mk)-plannedSavings()-goalPlannedMonthlyV224()-plannedPurchasesTotalV22();','return spendableNowV226()-plannedPurchasesTotalV22();',1)
p.write_text(s,encoding='utf-8')
