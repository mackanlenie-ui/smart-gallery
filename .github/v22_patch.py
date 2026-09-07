from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.1','Min Ekonomi v2.2')
insert=s.rfind('\n}')
extra=r'''
  double plannedPurchasesTotalV22(){double x=0;JSONArray a=arr("planned");for(int i=0;i<a.length();i++)x+=a.optJSONObject(i)==null?0:a.optJSONObject(i).optDouble("amount");return x;}
  double forecastSalaryV22(){Calendar n=Calendar.getInstance();String mk=monthKey(n);double[] t=totals(mk);return t[0]-t[1]-unpaidBills(mk)-plannedSavings()-plannedPurchasesTotalV22();}
  String smartSummaryV22(){double f=forecastSalaryV22();int d=daysToNextSalaryV21();double safe=f/Math.max(1,d);return "💰 Vad kan jag spendera idag?\n"+fmt(Math.max(0,safe))+"\n\n📈 Beräknat saldo på lönedagen\n"+fmt(f);}
  void togglePrivacyV22(){boolean h=!p.getBoolean("privacyAmounts",false);p.edit().putBoolean("privacyAmounts",h).apply();Toast.makeText(this,h?"Belopp dolda 👁️":"Belopp visas 👁️",Toast.LENGTH_SHORT).show();showOverview();}
  void addV22Overview(){LinearLayout c=card();TextView t=tv(p.getBoolean("privacyAmounts",false)?"🔒 Beloppen är dolda\nTryck på ögat för att visa dem":smartSummaryV22(),16,true);c.addView(t);TextView eye=action(p.getBoolean("privacyAmounts",false)?"👁 Visa belopp":"🙈 Dölj belopp");eye.setOnClickListener(v->togglePrivacyV22());c.addView(eye);content.addView(c);}
  void showSafetyCopiesV22(){shell("Säkerhetskopior",2);section("Automatiska lokala kopior");TextView info=small("Min Ekonomi behåller upp till fem lokala återställningspunkter. En ny kopia skapas när appen startas en ny dag.",Color.rgb(170,180,190));content.addView(info);JSONArray a=arr("autoBackupsV22");for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);if(o==null)continue;LinearLayout c=card();c.addView(tv("💾 "+o.optString("date"),15,true));content.addView(c);}if(a.length()==0)empty("Första kopian skapas automatiskt.");}
  void autoBackupV22(){String today=new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());if(today.equals(p.getString("lastAutoBackupV22","")))return;try{JSONObject b=new JSONObject();b.put("date",today);b.put("tx",arr("tx"));b.put("accounts",arr("accounts"));b.put("fixed",arr("fixed"));b.put("catbudgets",arr("catbudgets"));b.put("goals",arr("goals"));b.put("bills",arr("bills"));b.put("planned",arr("planned"));JSONArray a=arr("autoBackupsV22");a.put(b);while(a.length()>5)a.remove(0);save("autoBackupsV22",a);p.edit().putString("lastAutoBackupV22",today).apply();}catch(Exception e){}}
  void budgetAndPlannedAlertsV22(){if(!p.getBoolean("notifications",true))return;String mk=monthKey(Calendar.getInstance());JSONArray b=arr("catbudgets");for(int i=0;i<b.length();i++){JSONObject o=b.optJSONObject(i);if(o==null)continue;double lim=o.optDouble("amount");if(lim<=0)continue;double spent=categorySpent(mk,o.optString("category"));if(spent>=lim*.8)notifyNow(3100+i,"Budgeten närmar sig gränsen",o.optString("category")+" • "+fmt(spent)+" av "+fmt(lim));}}
'''
s=s[:insert]+extra+s[insert:]
# Add v2.2 card near insights/overview without changing navigation.
for anchor in ['section("Ekonomiinsikter");','section("Översikt");']:
 if anchor in s:s=s.replace(anchor,'addV22Overview();'+anchor,1);break
# Add safety-copy viewer to settings.
anchor='TextView calV21=action("📅 Ekonomikalender");'
if anchor in s:s=s.replace(anchor,'TextView backupsV22=action("💾 Automatiska säkerhetskopior");backupsV22.setOnClickListener(v->showSafetyCopiesV22());content.addView(backupsV22);'+anchor,1)
# Run maintenance after launch alert checks.
s=s.replace('postSmartAlerts();scheduleBillRemindersV21();','postSmartAlerts();scheduleBillRemindersV21();autoBackupV22();budgetAndPlannedAlertsV22();')
p.write_text(s,encoding='utf-8')
