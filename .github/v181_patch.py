from pathlib import Path
p=Path('app/src/main/java/se/minekonomi/app/MainActivity.java')
s=p.read_text(encoding='utf-8')
# add lifecycle state fields immediately after class declaration
needle='public class MainActivity extends Activity {'
if needle not in s: raise SystemExit('class marker missing')
s=s.replace(needle, needle+'\n  long biometricBackgroundAt=0L; boolean biometricUnlocked=false; boolean biometricPromptActive=false;',1)
# v1.8 authentication opens content, but lifecycle never re-locks. Replace authenticate method.
start=s.find('  void authenticateThenOpen(){')
end=s.find('\n  ',start+4)
# methods are compact one-line in generated source, so first newline ends method
if start<0: raise SystemExit('authenticateThenOpen missing')
old=s[start:end]
new='''  void authenticateThenOpen(){if(Build.VERSION.SDK_INT<28){biometricUnlocked=true;showOverview();return;}if(biometricPromptActive)return;biometricPromptActive=true;LinearLayout lock=new LinearLayout(this);lock.setOrientation(LinearLayout.VERTICAL);lock.setGravity(Gravity.CENTER);lock.setBackgroundColor(BG);TextView icon=tv("🔐",44,false);TextView title=tv("Min Ekonomi är låst",24,true);TextView info=tv("Verifiera dig för att visa din ekonomi",14,false);info.setTextColor(MUTED);lock.addView(icon);lock.addView(title);lock.addView(info);setContentView(lock);android.os.CancellationSignal cs=new android.os.CancellationSignal();java.util.concurrent.Executor ex=getMainExecutor();android.hardware.biometrics.BiometricPrompt.Builder b=new android.hardware.biometrics.BiometricPrompt.Builder(this).setTitle("Lås upp Min Ekonomi").setSubtitle("Använd fingeravtryck eller skärmlås");if(Build.VERSION.SDK_INT>=30)b.setAllowedAuthenticators(android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG|android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL);else b.setDeviceCredentialAllowed(true);android.hardware.biometrics.BiometricPrompt bp=b.build();bp.authenticate(cs,ex,new android.hardware.biometrics.BiometricPrompt.AuthenticationCallback(){@Override public void onAuthenticationSucceeded(android.hardware.biometrics.BiometricPrompt.AuthenticationResult r){super.onAuthenticationSucceeded(r);biometricPromptActive=false;biometricUnlocked=true;biometricBackgroundAt=0L;showOverview();postSmartAlerts();}@Override public void onAuthenticationError(int code,CharSequence msg){super.onAuthenticationError(code,msg);biometricPromptActive=false;biometricUnlocked=false;}});}'''
s=s[:start]+new+s[end:]
# add lifecycle re-lock. 30 seconds in background triggers authentication again.
insert=s.rfind('\n}')
life='''\n  @Override protected void onStop(){super.onStop();if(p!=null&&p.getBoolean("biometricLock",false)&&biometricUnlocked)biometricBackgroundAt=System.currentTimeMillis();}\n  @Override protected void onStart(){super.onStart();if(p==null)return;if(p.getBoolean("biometricLock",false)){long away=biometricBackgroundAt==0?Long.MAX_VALUE:System.currentTimeMillis()-biometricBackgroundAt;if(!biometricUnlocked||away>=30000L){biometricUnlocked=false;new Handler(Looper.getMainLooper()).postDelayed(()->authenticateThenOpen(),180);}}}\n'''
s=s[:insert]+life+s[insert:]
p.write_text(s,encoding='utf-8')
