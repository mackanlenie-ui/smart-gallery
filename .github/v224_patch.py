from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.3','Min Ekonomi v2.2.4')
# Goal editor: user chooses how much of the available money to reserve for each savings goal.
old='EditText n=new EditText(this),t=new EditText(this),due=new EditText(this);n.setHint("Målets namn");t.setHint("Målbelopp");t.setInputType(2|8192);'
new='EditText n=new EditText(this),t=new EditText(this),monthly=new EditText(this),due=new EditText(this);n.setHint("Målets namn");t.setHint("Målbelopp");t.setInputType(2|8192);monthly.setHint("Planerat sparande per månad");monthly.setInputType(2|8192);'
if old not in s: raise SystemExit('goal editor anchor missing')
s=s.replace(old,new,1)
old='if(old!=null){n.setText(old.optString("name"));t.setText(String.valueOf(old.optDouble("target")));}LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.addView(n);b.addView(t);b.addView(due);'
new='if(old!=null){n.setText(old.optString("name"));t.setText(String.valueOf(old.optDouble("target")));monthly.setText(String.valueOf(old.optDouble("plannedMonthly",0)));}LinearLayout b=new LinearLayout(this);b.setOrientation(LinearLayout.VERTICAL);b.addView(n);b.addView(t);b.addView(monthly);b.addView(due);'
if old not in s: raise SystemExit('goal dialog fields anchor missing')
s=s.replace(old,new,1)
old='"saved",old!=null?old.optDouble("saved"):0,"due",due.getText().toString())'
new='"saved",old!=null?old.optDouble("saved"):0,"plannedMonthly",Math.max(0,num(monthly)),"due",due.getText().toString())'
if old not in s: raise SystemExit('goal save anchor missing')
s=s.replace(old,new,1)
# Show planned vs recommended on goal page.
old='TextView plan=tv("Måldatum "+due+" • cirka "+fmt(need)+"/månad",12,false);'
new='double chosen=o.optDouble("plannedMonthly",0);TextView plan=tv("Måldatum "+due+" • rekommenderat "+fmt(need)+"/månad"+(chosen>0?" • du har valt "+fmt(chosen)+"/månad":""),12,false);'
if old not in s: raise SystemExit('goal plan text anchor missing')
s=s.replace(old,new,1)
# Helpers and compact overview card.
insert=s.rfind('\n}')
extra=r'''
  double goalPlannedMonthlyV224(){double x=0;JSONArray a=arr("goals");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&o.optDouble("saved")<o.optDouble("target"))x+=Math.max(0,o.optDouble("plannedMonthly",0));}return x;}
  void addGoalOverviewV224(){JSONArray a=arr("goals");if(a.length()==0)return;section("Sparmål");int shown=0;for(int i=0;i<a.length()&&shown<3;i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;double target=o.optDouble("target"),saved=o.optDouble("saved"),left=Math.max(0,target-saved),chosen=o.optDouble("plannedMonthly",0);LinearLayout c=card();TextView title=tv("🎯 "+o.optString("name"),16,true);c.addView(title);TextView sum=tv(fmt(saved)+" av "+fmt(target)+" • "+fmt(left)+" kvar",13,false);sum.setTextColor(MUTED);c.addView(sum);ProgressBar pb=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);pb.setMax(100);pb.setProgress(target<=0?0:(int)Math.min(100,Math.round(saved/target*100)));c.addView(pb,new LinearLayout.LayoutParams(-1,dp(7)));TextView pl=tv(chosen>0?"Planerat denna månad: "+fmt(chosen):"Inget planerat sparande denna månad",13,true);pl.setTextColor(chosen>0?ACCENT:MUTED);c.addView(pl);content.addView(c);shown++;}TextView all=small("Visa alla sparmål",Color.rgb(50,77,101));all.setOnClickListener(v->showGoals());content.addView(all);}
'''
s=s[:insert]+extra+s[insert:]
# Put savings goals between account summary and insights on the current-month overview.
old='addAccountSummaryV18();addInsightsV18();'
new='addAccountSummaryV18();addGoalOverviewV224();addInsightsV18();'
if old not in s: raise SystemExit('overview goal anchor missing')
s=s.replace(old,new,1)
# Reserve only the amount the user chose for goals. Existing global planned savings remains supported.
s=s.replace('double after=t[0]-t[1]-bills-plannedSavings();','double after=t[0]-t[1]-bills-plannedSavings()-goalPlannedMonthlyV224();',1)
s=s.replace('double free=cur[0]-cur[1]-unpaidBills(mk)-plannedSavings();','double free=cur[0]-cur[1]-unpaidBills(mk)-plannedSavings()-goalPlannedMonthlyV224();',1)
s=s.replace('return t[0]-t[1]-unpaidBills(mk)-plannedSavings()-plannedPurchasesTotalV22();','return t[0]-t[1]-unpaidBills(mk)-plannedSavings()-goalPlannedMonthlyV224()-plannedPurchasesTotalV22();',1)
# Main green Kvar att spendera card should also reflect the user's chosen goal reservation.
old='double bills=unpaidBills(mk),spendable=t[0]-t[1]-bills;'
new='double bills=unpaidBills(mk),spendable=t[0]-t[1]-bills-(monthOffset==0?goalPlannedMonthlyV224():0);'
if old not in s: raise SystemExit('spendable anchor missing')
s=s.replace(old,new,1)
p.write_text(s,encoding='utf-8')
