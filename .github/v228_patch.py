from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
s=s.replace('Min Ekonomi v2.2.7','Min Ekonomi v2.2.8')
# The overview actually calls addAccountSummaryV18(), so patch that implementation directly.
start=s.find('  void addAccountSummaryV18(){')
if start<0: raise SystemExit('addAccountSummaryV18 missing')
end=s.find('\n  void ',start+5)
if end<0: end=s.rfind('\n}')
block=s[start:end]
# Replace whichever legacy free-balance expression remains in this method.
import re
block2,n=re.subn(r'double free=[^;]+;', 'double free=spendableNowV226();', block, count=1)
if n!=1: raise SystemExit('free balance expression missing in addAccountSummaryV18')
block2=block2.replace('Fritt kvar:', 'Fritt att spendera:')
s=s[:start]+block2+s[end:]
p.write_text(s,encoding='utf-8')
