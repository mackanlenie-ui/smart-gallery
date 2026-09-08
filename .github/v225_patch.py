from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.4','Min Ekonomi v2.2.5')
# Savings should reduce account balance without being counted as consumption.
old='else if("Utgift".equals(t)||"TransferOut".equals(t))x-=o.optDouble("amount");'
new='else if("Utgift".equals(t)||"TransferOut".equals(t)||"Sparande".equals(t))x-=o.optDouble("amount");'
if old not in s: raise SystemExit('accountFlow anchor missing')
s=s.replace(old,new,1)
# Replace goalDeposit so it picks an account, updates the goal and writes a dedicated savings transaction.
start=s.find(' void goalDeposit(int ix){')
if start<0: raise SystemExit('goalDeposit missing')
end=s.find(' void deleteGoalV20(int ix){',start)
if end<0: raise SystemExit('goalDeposit end missing')
new_method=''' void goalDeposit(int ix){JSONArray goals=arr("goals");JSONObject goal=goals.optJSONObject(ix);if(goal==null)return;EditText e=new EditText(this);e.setHint("Belopp");e.setInputType(2|8192);Spinner acc=new Spinner(this);String[] accounts=accountNames();acc.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,accounts));LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.addView(e);box.addView(acc);new AlertDialog.Builder(this).setTitle("Lägg till sparande").setView(box).setNegativeButton("Avbryt",null).setPositiveButton("Lägg till",(d,w)->{double amount=num(e);if(amount<=0)return;String account=acc.getSelectedItem()==null?"Lönekonto":acc.getSelectedItem().toString();try{goal.put("saved",goal.optDouble("saved")+amount);}catch(Exception ignored){}save("goals",goals);JSONArray tx=arr("tx");tx.put(j("name","Sparande till "+goal.optString("name"),"amount",amount,"type","Sparande","category","Sparande","account",account,"date",today(),"fromGoal",true,"goalName",goal.optString("name")));save("tx",tx);Toast.makeText(this,"Sparandet är registrerat",Toast.LENGTH_SHORT).show();showGoals();}).show();}'''
s=s[:start]+new_method+s[end:]
# Show how much has actually been saved this month on each overview goal card.
old='TextView pl=tv(chosen>0?"Planerat denna månad: "+fmt(chosen):"Inget planerat sparande denna månad",13,true);pl.setTextColor(chosen>0?ACCENT:MUTED);c.addView(pl);'
new='double actual=goalSavedThisMonthV225(o.optString("name"));TextView pl=tv((chosen>0?"Planerat denna månad: "+fmt(chosen):"Inget planerat sparande denna månad")+"\\nSparat denna månad: "+fmt(actual),13,true);pl.setTextColor(chosen>0||actual>0?ACCENT:MUTED);c.addView(pl);'
if old not in s: raise SystemExit('overview goal text anchor missing')
s=s.replace(old,new,1)
insert=s.rfind('\n}')
extra=r'''
  double goalSavedThisMonthV225(String goalName){double x=0;String mk=monthKey(Calendar.getInstance());JSONArray a=arr("tx");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null&&"Sparande".equals(o.optString("type"))&&goalName.equals(o.optString("goalName"))&&o.optString("date").startsWith(mk))x+=o.optDouble("amount");}return x;}
'''
s=s[:insert]+extra+s[insert:]
p.write_text(s,encoding='utf-8')
