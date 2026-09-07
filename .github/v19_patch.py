from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
# v1.9: make the overview more advisory with a traffic-light economy status.
needle='if(monthOffset==0){addPayPeriodCard();addAccountSummaryV18();addInsightsV18();addPlannedSummary();}'
repl='if(monthOffset==0){addEconomyStatusV19();addPayPeriodCard();addAccountSummaryV18();addInsightsV18();addPlannedSummary();}'
if needle not in s: raise SystemExit('overview marker missing')
s=s.replace(needle,repl,1)
# Add status/prognosis card before the existing v1.8 helpers.
marker='  void addAccountSummaryV18(){'
if marker not in s: raise SystemExit('helper marker missing')
method='''  void addEconomyStatusV19(){Calendar[] per=salaryPeriod();double[] t=totalsBetween(per[0],per[1]);double bills=unpaidBillsBetween(todayCal(),per[1]);double after=t[0]-t[1]-bills-plannedSavings();int days=Math.max(1,daysRemaining(per[1]));double daily=after/days;double budget=p.getFloat("budget",0);double monthSpent=totals(monthKey(Calendar.getInstance()))[1];double ratio=budget>0?monthSpent/budget:0;int color;String state,msg;if(after<0||ratio>=1){color=RED;state="Rött läge";msg="Planerade kostnader är högre än utrymmet. Se över köp och räkningar.";}else if(daily<50||ratio>=0.8){color=AMBER;state="Gult läge";msg="Lite marginal kvar. Cirka "+fmt(daily)+" per dag fram till nästa lön.";}else{color=ACCENT;state="Grönt läge";msg="Du ligger bra till. Cirka "+fmt(daily)+" per dag fram till nästa lön.";}section("Ekonomiläge");LinearLayout c=card();TextView a=tv("●  "+state,20,true);a.setTextColor(color);c.addView(a);TextView b=tv(msg,14,false);b.setTextColor(MUTED);b.setPadding(0,dp(8),0,dp(5));c.addView(b);TextView z=tv("Prognos efter räkningar och planerat sparande: "+fmt(after),13,true);z.setTextColor(after>=0?ACCENT:RED);c.addView(z);content.addView(c);} 
'''
s=s.replace(marker,method+marker,1)
# Add CSV export entry to Settings beside existing bank import.
needle='csv.setOnClickListener(v->startCsvImport());'
if needle in s:
    s=s.replace(needle,needle+'TextView csvout=small("Exportera transaktioner CSV",Color.rgb(50,77,101));csvout.setOnClickListener(v->exportCsvV19());data.addView(csvout);',1)
# CSV export through Android share sheet, no extra storage permission.
insert=s.rfind('\n}')
extra='''\n  void exportCsvV19(){try{StringBuilder b=new StringBuilder("Datum;Beskrivning;Typ;Kategori;Konto;Belopp\\n");JSONArray a=arr("tx");for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o==null)continue;b.append(o.optString("date")).append(';').append(o.optString("name").replace(";",",")).append(';').append(o.optString("type")).append(';').append(o.optString("category")).append(';').append(o.optString("account")).append(';').append(String.format(Locale.US,"%.2f",o.optDouble("amount"))).append('\\n');}Intent it=new Intent(Intent.ACTION_SEND);it.setType("text/csv");it.putExtra(Intent.EXTRA_SUBJECT,"Min Ekonomi – transaktioner.csv");it.putExtra(Intent.EXTRA_TEXT,b.toString());startActivity(Intent.createChooser(it,"Exportera CSV"));}catch(Exception e){Toast.makeText(this,"Kunde inte exportera CSV",Toast.LENGTH_LONG).show();}}\n'''
s=s[:insert]+extra+s[insert:]
p.write_text(s,encoding='utf-8')
