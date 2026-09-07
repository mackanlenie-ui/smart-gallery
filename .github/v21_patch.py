from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')

def repl(old,new):
    global s
    if old not in s: raise SystemExit('missing '+old[:80])
    s=s.replace(old,new,1)

# Privacy: hide sensitive preview in Android recents/app switcher.
repl('  @Override public void onCreate(Bundle b){','  @Override public void onCreate(Bundle b){\n    getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE,android.view.WindowManager.LayoutParams.FLAG_SECURE);')

# Add quick access to economy calendar from settings without disturbing bottom navigation.
needle='TextView testN=small("🔔 Testa notis",Color.rgb(50,77,101));'
repl(needle,'TextView calV21=small("📅 Ekonomikalender",Color.rgb(50,77,101));calV21.setOnClickListener(v->showEconomyCalendarV21());content.addView(calV21);'+needle)

# Version label.
s=s.replace('Min Ekonomi v2.0','Min Ekonomi v2.1')

insert=s.rfind('\n}')
extra=r'''
  void showEconomyCalendarV21(){
    shell("Ekonomikalender",2);
    Calendar now=Calendar.getInstance();
    TextView month=tv(new java.text.SimpleDateFormat("MMMM yyyy",new Locale("sv","SE")).format(now.getTime()),22,true);content.addView(month);
    section("Kommande händelser");
    java.util.ArrayList<String> ev=new java.util.ArrayList<>();
    int salary=p.getInt("salaryDay",25);ev.add(String.format(Locale.US,"%02d • 💰 Lönedag",salary));
    JSONArray bills=arr("bills");String mk=monthKey(now);
    for(int i=0;i<bills.length();i++){JSONObject o=bills.optJSONObject(i);if(o==null||o.optBoolean("paid")||o.optBoolean("paused"))continue;String due=o.optString("due");if(due.startsWith(mk)&&due.length()>=10)ev.add(due.substring(8,10)+" • 🧾 "+o.optString("name")+"  "+fmt(o.optDouble("amount")));}
    JSONArray fixed=arr("fixed");for(int i=0;i<fixed.length();i++){JSONObject o=fixed.optJSONObject(i);if(o==null||o.optBoolean("paused"))continue;ev.add(String.format(Locale.US,"%02d • 🔁 %s  %s",o.optInt("day",1),o.optString("name"),fmt(o.optDouble("amount"))));}
    java.util.Collections.sort(ev);for(String x:ev){LinearLayout c=card();c.addView(tv(x,15,true));content.addView(c);}if(ev.size()==1)empty("Inga räkningar eller fasta poster denna månad.");
  }

  String economyCoachV21(){
    Calendar now=Calendar.getInstance();String mk=monthKey(now);double[] cur=totals(mk);Calendar prev=(Calendar)now.clone();prev.add(Calendar.MONTH,-1);double[] old=totals(monthKey(prev));
    double diff=old[1]-cur[1];double free=cur[0]-cur[1]-unpaidBills(mk)-plannedSavings();int days=Math.max(1,daysToNextSalary());double daily=free/days;
    String trend=diff>=0?"Du har spenderat "+fmt(diff)+" mindre än förra månaden.":"Du har spenderat "+fmt(-diff)+" mer än förra månaden.";
    return "🧠 Ekonomicoach\n"+trend+"\nMed nuvarande läge har du cirka "+fmt(daily)+" per dag till nästa lön.";
  }

  void scheduleBillRemindersV21(){
    if(!p.getBoolean("notifications",true))return;
    android.app.AlarmManager am=(android.app.AlarmManager)getSystemService(ALARM_SERVICE);if(am==null)return;
    JSONArray a=arr("bills");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null||o.optBoolean("paid")||o.optBoolean("paused"))continue;Date d=parseDate(o.optString("due"));if(d==null)continue;Calendar c=Calendar.getInstance();c.setTime(d);c.add(Calendar.DAY_OF_MONTH,-3);c.set(Calendar.HOUR_OF_DAY,9);c.set(Calendar.MINUTE,0);if(c.getTimeInMillis()<=System.currentTimeMillis())continue;
      android.content.Intent in=new android.content.Intent(this,se.minekonomi.app.ReminderReceiver.class);in.putExtra("title","Räkning om 3 dagar");in.putExtra("text",o.optString("name")+" • "+fmt(o.optDouble("amount")));int id=Math.abs((o.optString("name")+o.optString("due")).hashCode());android.app.PendingIntent pi=android.app.PendingIntent.getBroadcast(this,id,in,android.app.PendingIntent.FLAG_UPDATE_CURRENT|android.app.PendingIntent.FLAG_IMMUTABLE);am.setAndAllowWhileIdle(android.app.AlarmManager.RTC_WAKEUP,c.getTimeInMillis(),pi);
    }
  }
'''
s=s[:insert]+extra+s[insert:]

# Coach is shown when overview is opened. Anchor on an existing overview method call if present.
for anchor in ['section("Ekonomiinsikter");','section("Översikt");']:
    if anchor in s:
        s=s.replace(anchor,'LinearLayout coach=card();TextView coachText=tv(economyCoachV21(),14,true);coachText.setTextColor(ACCENT);coach.addView(coachText);content.addView(coach);'+anchor,1);break
# Schedule reminders whenever smart alerts are checked at launch.
s=s.replace('postSmartAlerts();','postSmartAlerts();scheduleBillRemindersV21();')
p.write_text(s,encoding='utf-8')
