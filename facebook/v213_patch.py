from pathlib import Path
p=Path('facebook/src/main/java/se/mackan/fblite/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('    private static final String APP_VERSION="2.12";\n','    private static final String APP_VERSION="2.13";\n')
# Keep settings object harmlessly available to existing methods, but hide the floating gear.
old='gear.setBackgroundColor(d?0xCC222222:0xCCFFFFFF);gear.setTextColor(d?Color.WHITE:Color.DKGRAY);'
new='if(gear!=null){gear.setVisibility(View.GONE);}'
if old in s:s=s.replace(old,new)
# Hide gear wherever it is created, without touching the working WebView/settings code.
needle='gear.setText("⚙");'
if needle in s:s=s.replace(needle,needle+'gear.setVisibility(View.GONE);')
# DeX should always be automatic; ignore the old preference toggle.
s=s.replace('dexMode=detectDex()&&getBool("dex",true);','dexMode=detectDex();')
s=s.replace('detectDex()&&getBool("dex",true)','detectDex()')
p.write_text(s,encoding='utf-8')
print('Applied v2.13: hidden settings gear, automatic DeX retained')
