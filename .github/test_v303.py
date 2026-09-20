from pathlib import Path
s=Path('app/src/main/java/se/minekonomi/app/MainActivity.java').read_text(encoding='utf-8')
g=Path('app/build.gradle').read_text(encoding='utf-8')
checks=[
 ('accountBalanceV303','live account balance helper'),
 ('applyRecurringV303','recurring transaction processing'),
 ('Återkommande poster','recurring transaction UI'),
 ('Försenad','overdue bill status'),
 ('Förfaller idag','bill due-today status'),
 ('Betald','paid bill action/status'),
]
missing=[label for token,label in checks if token not in s]
if "versionCode 30" not in g or "versionName '3.0.3'" not in g: missing.append('v3.0.3 version')
if missing: raise SystemExit('Missing v3.0.3 features: '+', '.join(missing))
print('v3.0.3 requirements passed')
