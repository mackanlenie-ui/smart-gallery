from pathlib import Path
s=Path('app/src/main/java/se/minekonomi/app/MainActivity.java').read_text(encoding='utf-8')
checks=[
 ('ACTION_CREATE_DOCUMENT','cloud backup export'),
 ('ACTION_OPEN_DOCUMENT','cloud backup restore'),
 ('backupBeforeRestoreV301','pre-restore safety copy'),
 ('lastManualBackupV301','last backup timestamp'),
 ('getPackageInfo','automatic app version'),
]
missing=[label for token,label in checks if token not in s]
if missing: raise SystemExit('Missing v3.0.1 features: '+', '.join(missing))
print('v3.0.1 source checks passed')
