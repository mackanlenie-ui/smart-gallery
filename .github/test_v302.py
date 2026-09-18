from pathlib import Path
s=Path('app/src/main/java/se/minekonomi/app/MainActivity.java').read_text(encoding='utf-8')
checks=[
 ('Automatiska säkerhetskopior','automatic local backups'),
 ('Ekonomikalender','economy calendar'),
 ('Testa notis','notification test'),
 ('Exportera transaktioner CSV','transaction CSV export'),
 ('Spara backup','manual backup export'),
 ('Återställ backup','manual backup restore'),
 ('lastManualBackupV301','last backup timestamp'),
 ('getPackageInfo','automatic app version'),
]
missing=[label for token,label in checks if token not in s]
if missing: raise SystemExit('Missing v3.0.2 settings features: '+', '.join(missing))
print('v3.0.2 settings checks passed')
