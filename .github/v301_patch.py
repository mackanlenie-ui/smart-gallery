from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.10','Min Ekonomi v3.0.1')
# Track successful manual backups.
s=s.replace('Toast.makeText(this,"Backup sparad.",Toast.LENGTH_LONG).show();','p.edit().putString("lastManualBackupV301",new SimpleDateFormat("yyyy-MM-dd HH:mm",SV).format(new Date())).apply();Toast.makeText(this,"Backup sparad.",Toast.LENGTH_LONG).show();',1)
# Always preserve the current state before an imported backup replaces it.
s=s.replace('void restoreBackup(JSONObject root){try{SharedPreferences.Editor ed=p.edit();','void restoreBackup(JSONObject root){try{backupBeforeRestoreV301();SharedPreferences.Editor ed=p.edit();',1)
# Replace settings with clearer cloud-safe backup controls and automatic version display.
start=s.find('  void showSettings(){')
end=s.find('\n  void authenticateThenOpen()',start)
if start<0 or end<0: raise SystemExit('showSettings anchor missing')
new=r'''  void showSettings(){shell("Inställningar",2);section("Ekonomi");TextView pay=small("Lönedag "+salaryDay()+":e • planerat sparande "+fmt(plannedSavings()),Color.rgb(50,77,101));pay.setOnClickListener(v->paySettingsDialog());content.addView(pay);section("Säkerhet");TextView bio=small((p.getBoolean("biometricLock",false)?"✓ ":"")+"Biometriskt applås",Color.rgb(50,77,101));bio.setOnClickListener(v->{boolean nv=!p.getBoolean("biometricLock",false);p.edit().putBoolean("biometricLock",nv).apply();Toast.makeText(this,nv?"Biometriskt applås aktiverat.":"Biometriskt applås avstängt.",Toast.LENGTH_LONG).show();showSettings();});content.addView(bio);section("Notiser");TextView no=small((p.getBoolean("notifications",true)?"✓ ":"")+"Räknings- och budgetnotiser",Color.rgb(50,77,101));no.setOnClickListener(v->{p.edit().putBoolean("notifications",!p.getBoolean("notifications",true)).apply();showSettings();});content.addView(no);section("Backup & data");TextView info=tv("Spara backup till valfri plats via Android – till exempel Google Drive, OneDrive, USB eller telefonen.",13,false);info.setTextColor(MUTED);info.setPadding(dp(2),0,dp(2),dp(10));content.addView(info);LinearLayout bk=new LinearLayout(this);TextView ex=small("☁ Spara backup…",Color.rgb(50,77,101)),im=small("↻ Återställ backup…",Color.rgb(50,77,101));ex.setOnClickListener(v->startExport());im.setOnClickListener(v->startImport());bk.addView(ex,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.setMargins(dp(8),0,0,0);bk.addView(im,lp);content.addView(bk);String last=p.getString("lastManualBackupV301","");TextView ls=tv(last.isEmpty()?"Ingen manuell backup sparad ännu.":"Senaste backup: "+last,12,false);ls.setTextColor(MUTED);ls.setPadding(dp(2),dp(8),0,dp(8));content.addView(ls);if(p.contains("preRestoreBackupV301")){TextView undo=small("Återställ data från före senaste import",Color.rgb(50,77,101));undo.setOnClickListener(v->restorePreImportV301());content.addView(undo);}TextView csv=small("Importera bank-CSV",Color.rgb(50,77,101));csv.setOnClickListener(v->startCsvImport());content.addView(csv);section("Om appen");TextView ver=tv("Min Ekonomi v"+appVersionV301()+"\nDin ekonomi. Enkelt och privat.",14,false);ver.setTextColor(MUTED);content.addView(ver);}'''
s=s[:start]+new+s[end:]
insert=s.rfind('\n}')
extra=r'''
  String appVersionV301(){try{return getPackageManager().getPackageInfo(getPackageName(),0).versionName;}catch(Exception e){return "3.0.1";}}
  void backupBeforeRestoreV301(){p.edit().putString("preRestoreBackupV301",backupJson().toString()).putString("preRestoreBackupTimeV301",new SimpleDateFormat("yyyy-MM-dd HH:mm",SV).format(new Date())).apply();}
  void restorePreImportV301(){try{String raw=p.getString("preRestoreBackupV301","");if(raw.isEmpty())return;JSONObject root=new JSONObject(raw);new AlertDialog.Builder(this).setTitle("Återställ föregående data?").setMessage("Detta återställer läget från precis före din senaste backup-import.").setNegativeButton("Avbryt",null).setPositiveButton("Återställ",(d,w)->{p.edit().remove("preRestoreBackupV301").apply();restoreBackup(root);}).show();}catch(Exception e){Toast.makeText(this,"Kunde inte återställa föregående data.",Toast.LENGTH_LONG).show();}}
'''
s=s[:insert]+extra+s[insert:]
p.write_text(s,encoding='utf-8')
