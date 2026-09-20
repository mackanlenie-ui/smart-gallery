from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
# Paused recurring items must never be posted automatically.
s=s.replace('if(x==null)continue;String id=x.optString("id");','if(x==null||x.optBoolean("paused"))continue;String id=x.optString("id");',1)
# Use clearer recurring terminology without changing the existing DeX/overview layout.
s=s.replace('shell("Fasta belopp",1);TextView add=action("＋  Nytt fast månadsbelopp")','shell("Återkommande poster",1);TextView add=action("＋  Ny återkommande post")',1)
s=s.replace('section("Fasta månadsbelopp")','section("Återkommande poster")',1)
s=s.replace('empty("Inga fasta belopp.")','empty("Inga återkommande poster.")',1)
# Route current-month automatic posting through the v3.0.3 helper.
s=s.replace('void showOverview(){if(monthOffset==0)applyFixed();','void showOverview(){if(monthOffset==0)applyRecurringV303();',1)
# Accounts already derive balance from transactions; make that single source explicit.
s=s.replace('double bal=o.optDouble("start")+accountFlow(o.optString("name"));','double bal=accountBalanceV303(o);',1)
s=s.replace('TextView s=tv("Saldo: "+fmt(bal),23,true);','TextView s=tv("Aktuellt saldo: "+fmt(bal),23,true);',1)
# Centralize bill wording (overdue / today / tomorrow / days left).
old='String status=paused?"Pausad":o.optBoolean("paid")?"Betald":left<0?"Försenad "+Math.abs(left)+" dagar":left==0?"Förfaller idag":left==1?"Förfaller imorgon":left+" dagar kvar";'
new='String status=billStatusV303(o,left,paused);'
if old not in s: raise SystemExit('bill status anchor missing')
s=s.replace(old,new,1)
insert=s.rfind('\n}')
extra=r"""
  double accountBalanceV303(JSONObject account){if(account==null)return 0;return account.optDouble("start")+accountFlow(account.optString("name"));}
  void applyRecurringV303(){applyFixed();}
  String billStatusV303(JSONObject o,int left,boolean paused){if(paused)return "Pausad";if(o.optBoolean("paid"))return "Betald";if(left<0)return "Försenad "+Math.abs(left)+" dagar";if(left==0)return "Förfaller idag";if(left==1)return "Förfaller imorgon";return left+" dagar kvar";}
"""
s=s[:insert]+extra+s[insert:]
p.write_text(s,encoding='utf-8')
g=Path('app/build.gradle')
b=g.read_text(encoding='utf-8').replace('versionCode 29','versionCode 30').replace("versionName '3.0.2'","versionName '3.0.3'")
g.write_text(b,encoding='utf-8')
