package se.minekonomi.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG = Color.rgb(15,23,32), CARD = Color.rgb(28,39,50), TEXT = Color.WHITE, MUTED = Color.rgb(170,182,194), ACCENT = Color.rgb(71,200,134), RED = Color.rgb(255,107,107);
    private SharedPreferences prefs;
    private LinearLayout content;
    private final NumberFormat money = NumberFormat.getCurrencyInstance(new Locale("sv","SE"));

    @Override public void onCreate(Bundle b){ super.onCreate(b); prefs=getSharedPreferences("min_ekonomi",MODE_PRIVATE); showOverview(); }

    private TextView tv(String s,int sp,boolean bold){ TextView v=new TextView(this); v.setText(s); v.setTextColor(TEXT); v.setTextSize(sp); v.setTypeface(null,bold?Typeface.BOLD:Typeface.NORMAL); v.setPadding(dp(4),dp(6),dp(4),dp(6)); return v; }
    private Button btn(String s){ Button b=new Button(this); b.setText(s); b.setAllCaps(false); b.setTextSize(14); return b; }
    private int dp(int x){ return (int)(x*getResources().getDisplayMetrics().density+.5f); }
    private double parse(EditText e){ try{return Double.parseDouble(e.getText().toString().replace(',','.'));}catch(Exception x){return 0;} }

    private void shell(String title){
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG); root.setPadding(dp(14),dp(12),dp(14),dp(10));
        TextView h=tv(title,28,true); root.addView(h);
        HorizontalScrollView hs=new HorizontalScrollView(this); hs.setHorizontalScrollBarEnabled(false); LinearLayout nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL);
        String[] names={"Översikt","Transaktioner","Budget","Räkningar","Statistik"};
        View.OnClickListener[] ls={v->showOverview(),v->showTransactions(),v->showBudget(),v->showBills(),v->showStats()};
        for(int i=0;i<names.length;i++){ Button b=btn(names[i]); b.setOnClickListener(ls[i]); nav.addView(b); }
        hs.addView(nav); root.addView(hs,new LinearLayout.LayoutParams(-1,dp(58)));
        ScrollView sv=new ScrollView(this); content=new LinearLayout(this); content.setOrientation(LinearLayout.VERTICAL); content.setPadding(0,dp(8),0,dp(16)); sv.addView(content); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1)); setContentView(root);
    }

    private LinearLayout card(){ LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setPadding(dp(16),dp(14),dp(16),dp(14)); c.setBackgroundColor(CARD); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2); p.setMargins(0,0,0,dp(10)); c.setLayoutParams(p); return c; }
    private void metric(String label,String value,int color){ LinearLayout c=card(); TextView a=tv(label,14,false); a.setTextColor(MUTED); TextView b=tv(value,25,true); b.setTextColor(color); c.addView(a); c.addView(b); content.addView(c); }

    private JSONArray arr(String key){ try{return new JSONArray(prefs.getString(key,"[]"));}catch(Exception e){return new JSONArray();} }
    private void save(String key,JSONArray a){ prefs.edit().putString(key,a.toString()).apply(); }
    private String now(){ return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date()); }
    private boolean thisMonth(String d){ return d!=null && d.startsWith(new SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(new Date())); }

    private double[] totals(){ double in=0,out=0; JSONArray a=arr("tx"); for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i); if(thisMonth(o.optString("date"))){ if(o.optString("type").equals("Inkomst")) in+=o.optDouble("amount"); else out+=o.optDouble("amount"); }}catch(Exception ignored){} return new double[]{in,out}; }

    private void showOverview(){ shell("Min Ekonomi"); double[] t=totals(); double left=t[0]-t[1]; metric("Inkomster denna månad",money.format(t[0]),ACCENT); metric("Utgifter denna månad",money.format(t[1]),RED); metric("Kvar",money.format(left),left>=0?ACCENT:RED);
        double budget=prefs.getFloat("budget",0); if(budget>0){ metric("Budget kvar",money.format(budget-t[1]),budget>=t[1]?ACCENT:RED); }
        Button add=btn("＋ Lägg till transaktion"); add.setOnClickListener(v->transactionDialog()); content.addView(add,new LinearLayout.LayoutParams(-1,dp(58)));
        TextView s=tv("Senaste",20,true); s.setPadding(dp(4),dp(18),0,dp(8)); content.addView(s); JSONArray a=arr("tx"); int start=Math.max(0,a.length()-5); for(int i=a.length()-1;i>=start;i--) addTxRow(a,i,false);
    }

    private void showTransactions(){ shell("Transaktioner"); Button add=btn("＋ Ny transaktion"); add.setOnClickListener(v->transactionDialog()); content.addView(add,new LinearLayout.LayoutParams(-1,dp(58))); JSONArray a=arr("tx"); if(a.length()==0) empty("Inga transaktioner ännu."); for(int i=a.length()-1;i>=0;i--) addTxRow(a,i,true); }
    private void addTxRow(JSONArray a,int i,boolean deletable){ try{JSONObject o=a.getJSONObject(i); LinearLayout c=card(); LinearLayout line=new LinearLayout(this); line.setOrientation(LinearLayout.HORIZONTAL); LinearLayout text=new LinearLayout(this); text.setOrientation(LinearLayout.VERTICAL); TextView n=tv(o.optString("name"),17,true); TextView sub=tv(o.optString("category")+" • "+o.optString("date"),13,false); sub.setTextColor(MUTED); text.addView(n); text.addView(sub); line.addView(text,new LinearLayout.LayoutParams(0,-2,1)); TextView m=tv((o.optString("type").equals("Inkomst")?"+ ":"− ")+money.format(o.optDouble("amount")),17,true); m.setTextColor(o.optString("type").equals("Inkomst")?ACCENT:RED); line.addView(m); c.addView(line); if(deletable){ Button del=btn("Ta bort"); final int idx=i; del.setOnClickListener(v->{ JSONArray x=arr("tx"); JSONArray nArr=new JSONArray(); for(int j=0;j<x.length();j++) if(j!=idx) try{nArr.put(x.getJSONObject(j));}catch(Exception ignored){} save("tx",nArr); showTransactions(); }); c.addView(del); } content.addView(c);}catch(Exception ignored){} }

    private void transactionDialog(){
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL); box.setPadding(dp(20),dp(6),dp(20),0);
        EditText name=new EditText(this); name.setHint("Beskrivning, t.ex. ICA"); EditText amount=new EditText(this); amount.setHint("Belopp"); amount.setInputType(2|8192); Spinner type=new Spinner(this); type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Utgift","Inkomst"})); Spinner cat=new Spinner(this); cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Mat","Boende","Bil/transport","Abonnemang","Nöjen","Hälsa","Shopping","Lön","Övrigt"}));
        box.addView(name); box.addView(amount); box.addView(type); box.addView(cat);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Ny transaktion").setView(box).setNegativeButton("Avbryt",null).setPositiveButton("Spara",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{ if(name.getText().toString().trim().isEmpty()||parse(amount)<=0){Toast.makeText(this,"Fyll i namn och belopp",Toast.LENGTH_SHORT).show();return;} try{JSONObject o=new JSONObject(); o.put("name",name.getText().toString().trim()); o.put("amount",parse(amount)); o.put("type",type.getSelectedItem().toString()); o.put("category",cat.getSelectedItem().toString()); o.put("date",now()); JSONArray a=arr("tx"); a.put(o); save("tx",a); d.dismiss(); showOverview();}catch(Exception ignored){} })); d.show();
    }

    private void showBudget(){ shell("Budget"); double b=prefs.getFloat("budget",0), spent=totals()[1]; metric("Månadsbudget",money.format(b),TEXT); metric("Förbrukat",money.format(spent),RED); metric("Kvar",money.format(b-spent),b>=spent?ACCENT:RED); Button edit=btn("Ändra månadsbudget"); edit.setOnClickListener(v->budgetDialog()); content.addView(edit,new LinearLayout.LayoutParams(-1,dp(58))); categorySpend(); }
    private void budgetDialog(){ EditText e=new EditText(this); e.setHint("t.ex. 15000"); e.setInputType(2|8192); e.setText(prefs.getFloat("budget",0)>0?String.valueOf(prefs.getFloat("budget",0)):""); new AlertDialog.Builder(this).setTitle("Månadsbudget").setView(e).setNegativeButton("Avbryt",null).setPositiveButton("Spara",(d,w)->{prefs.edit().putFloat("budget",(float)parse(e)).apply();showBudget();}).show(); }
    private void categorySpend(){ Map<String,Double> m=new LinkedHashMap<>(); JSONArray a=arr("tx"); for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i); if(thisMonth(o.optString("date"))&&o.optString("type").equals("Utgift")) m.put(o.optString("category"),m.getOrDefault(o.optString("category"),0.0)+o.optDouble("amount"));}catch(Exception ignored){} TextView h=tv("Utgifter per kategori",20,true); h.setPadding(0,dp(18),0,dp(8)); content.addView(h); if(m.isEmpty()) empty("Ingen förbrukning att visa."); for(Map.Entry<String,Double> e:m.entrySet()){LinearLayout c=card(); LinearLayout l=new LinearLayout(this); TextView n=tv(e.getKey(),16,true); TextView v=tv(money.format(e.getValue()),16,true); l.addView(n,new LinearLayout.LayoutParams(0,-2,1)); l.addView(v); c.addView(l); content.addView(c);} }

    private void showBills(){ shell("Räkningar"); Button add=btn("＋ Lägg till räkning"); add.setOnClickListener(v->billDialog()); content.addView(add,new LinearLayout.LayoutParams(-1,dp(58))); JSONArray a=arr("bills"); if(a.length()==0) empty("Inga räkningar inlagda."); for(int i=0;i<a.length();i++){ try{JSONObject o=a.getJSONObject(i); LinearLayout c=card(); TextView n=tv(o.optString("name"),17,true); TextView sub=tv("Förfaller "+o.optString("due"),13,false); sub.setTextColor(MUTED); TextView am=tv(money.format(o.optDouble("amount")),20,true); am.setTextColor(RED); c.addView(n);c.addView(sub);c.addView(am); final int idx=i; Button paid=btn("Markera betald / ta bort"); paid.setOnClickListener(v->{JSONArray x=arr("bills"),y=new JSONArray(); for(int j=0;j<x.length();j++) if(j!=idx)try{y.put(x.getJSONObject(j));}catch(Exception ignored){} save("bills",y);showBills();}); c.addView(paid); content.addView(c);}catch(Exception ignored){} } }
    private void billDialog(){ LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL); EditText n=new EditText(this);n.setHint("Räkning"); EditText a=new EditText(this);a.setHint("Belopp");a.setInputType(2|8192); EditText due=new EditText(this);due.setHint("Förfallodatum, t.ex. 2026-09-28"); box.addView(n);box.addView(a);box.addView(due); new AlertDialog.Builder(this).setTitle("Ny räkning").setView(box).setNegativeButton("Avbryt",null).setPositiveButton("Spara",(d,w)->{if(n.getText().toString().trim().isEmpty()||parse(a)<=0)return; try{JSONObject o=new JSONObject();o.put("name",n.getText().toString().trim());o.put("amount",parse(a));o.put("due",due.getText().toString().trim());JSONArray x=arr("bills");x.put(o);save("bills",x);showBills();}catch(Exception ignored){}}).show(); }

    private void showStats(){ shell("Statistik"); JSONArray a=arr("tx"); Map<String,double[]> months=new TreeMap<>(Collections.reverseOrder()); for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i);String d=o.optString("date");if(d.length()<7)continue;String m=d.substring(0,7);double[] v=months.getOrDefault(m,new double[]{0,0});if(o.optString("type").equals("Inkomst"))v[0]+=o.optDouble("amount");else v[1]+=o.optDouble("amount");months.put(m,v);}catch(Exception ignored){} if(months.isEmpty()) empty("Statistik visas när du lagt in transaktioner."); for(Map.Entry<String,double[]> e:months.entrySet()){LinearLayout c=card();c.addView(tv(e.getKey(),18,true));TextView i=tv("In: "+money.format(e.getValue()[0]),15,false);i.setTextColor(ACCENT);TextView o=tv("Ut: "+money.format(e.getValue()[1]),15,false);o.setTextColor(RED);TextView r=tv("Resultat: "+money.format(e.getValue()[0]-e.getValue()[1]),17,true);c.addView(i);c.addView(o);c.addView(r);content.addView(c);} categorySpend(); }

    private void empty(String s){ TextView e=tv(s,16,false); e.setTextColor(MUTED); e.setGravity(Gravity.CENTER); e.setPadding(dp(8),dp(30),dp(8),dp(30)); content.addView(e); }
}
