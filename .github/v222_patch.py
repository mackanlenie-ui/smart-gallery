from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
# Keep screenshots enabled while hiding the app preview in Recents on supported Android versions.
# Restore v2.1-style biometric relock reliably: prompt on cold start and after 30 seconds away.
s=s.replace('Min Ekonomi v2.2.1','Min Ekonomi v2.2.2')
# v1.8.1 lifecycle used Long.MAX_VALUE for a zero timestamp; after successful auth it reset the timestamp to 0,
# which could immediately be interpreted as a long absence on a subsequent onStart. Use explicit cold-start logic.
old='long away=biometricBackgroundAt==0?Long.MAX_VALUE:System.currentTimeMillis()-biometricBackgroundAt;if(!biometricUnlocked||away>=30000L)'
new='long away=biometricBackgroundAt==0?0L:System.currentTimeMillis()-biometricBackgroundAt;if(!biometricUnlocked||biometricBackgroundAt>0&&away>=30000L)'
if old in s:s=s.replace(old,new,1)
# Add breathing room above the CSV export button without changing the rest of the Posts layout.
old='TextView csvout=small("Exportera transaktioner CSV",Color.rgb(50,77,101));csvout.setOnClickListener(v->exportCsvV19());content.addView(csvout);'
new='TextView csvout=small("Exportera transaktioner CSV",Color.rgb(50,77,101));csvout.setPadding(csvout.getPaddingLeft(),dp(14),csvout.getPaddingRight(),csvout.getPaddingBottom());csvout.setOnClickListener(v->exportCsvV19());content.addView(csvout);'
if old not in s: raise SystemExit('CSV export anchor missing')
s=s.replace(old,new,1)
p.write_text(s,encoding='utf-8')
