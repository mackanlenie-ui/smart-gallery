package se.minekonomi.app;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private final int BG=Color.rgb(10,18,27), SURFACE=Color.rgb(20,31,42), CARD=Color.rgb(27,40,53), CARD2=Color.rgb(34,49,63);
    private final int TEXT=Color.WHITE, MUTED=Color.rgb(166,180,193), ACCENT=Color.rgb(71,200,134), RED=Color.rgb(255,108,117), BLUE=Color.rgb(112,169,255);
    private SharedPreferences prefs;
    private LinearLayout content;
    private final NumberFormat money=NumberFormat.getCurrencyInstance(new Locale("sv","SE"));

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("min_ekonomi",MODE_PRIVATE);
        ensureRecurringTransactions();
        showOverview();
    }

    private int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    private TextView tv(String s,int sp,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(TEXT);v.setTextSize(sp);v.setTypeface(null,bold?Typeface.BOLD:Typeface.NORMAL);return v;}
    private GradientDrawable bg(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    private double parse(EditText e){try{return Double.parseDouble(e.getText().toString().trim().replace(',','.'));}catch(Exception x){return 0;}}
    private String format(double v){return money.format(v).replace(" ", " ");}
    private JSONArray arr(String key){try{return new JSONArray(prefs.getString(key,"[]"));}catch(Exception e){return new JSONArray();}}
    private void save(String key,JSONArray a){prefs.edit().putString(key,a.toString()).apply();}
    private String now(){return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(new Date());}
    private String currentMonthKey(){return new SimpleDateFormat("yyyy-MM",Locale.getDefault()).format(new Date());}
    private boolean thisMonth(String d){return d!=null&&d.startsWith(currentMonthKey());}
    private String monthTitle(){String s=new SimpleDateFormat("MMMM yyyy",new Locale("sv","SE")).format(new Date());return s.substring(0,1).toUpperCase(new Locale("sv","SE"))+s.substring(1);}

    private TextView action(String label){
        TextView b=tv(label,15,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(14),dp(13),dp(14),dp(13));b.setBackground(bg(ACCENT,18));b.setTextColor(Color.rgb(5,28,20));b.setClickable(true);b.setFocusable(true);return b;
    }
    private TextView smallAction(String label,int color){TextView b=tv(label,13,true);b.setGravity(Gravity.CENTER);b.setPadding(dp(12),dp(10),dp(12),dp(10));b.setBackground(bg(color,14));return b;}

    private void shell(String title,int selected){
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);
        LinearLayout header=new LinearLayout(this);header.setOrientation(LinearLayout.VERTICAL);header.setPadding(dp(20),dp(12),dp(20),dp(8));
        TextView h=tv(title,29,true);header.addView(h);
        if(selected==0){TextView m=tv(monthTitle(),14,false);m.setTextColor(MUTED);m.setPadding(0,dp(4),0,0);header.addView(m);}
        root.addView(header);
        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);sv.setClipToPadding(false);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);content.setPadding(dp(16),dp(10),dp(16),dp(24));sv.addView(content,new ScrollView.LayoutParams(-1,-2));
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(bottomNav(selected),new LinearLayout.LayoutParams(-1,dp(70)));
        setContentView(root);
    }

    private View bottomNav(int selected){
        LinearLayout nav=new LinearLayout(this);nav.setOrientation(LinearLayout.HORIZONTAL);nav.setPadding(dp(5),dp(5),dp(5),dp(7));nav.setBackgroundColor(SURFACE);
        String[] icons={"⌂","↕","◎","▤","▥"};String[] labels={"Översikt","Poster","Budget","Räkningar","Statistik"};
        View.OnClickListener[] ls={v->showOverview(),v->showTransactions(),v->showBudget(),v->showBills(),v->showStats()};
        for(int i=0;i<labels.length;i++){
            LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.setGravity(Gravity.CENTER);t.setPadding(dp(2),dp(2),dp(2),dp(2));
            if(i==selected)t.setBackground(bg(Color.rgb(34,61,56),16));
            TextView ic=tv(icons[i],22,true);ic.setGravity(Gravity.CENTER);ic.setTextColor(i==selected?ACCENT:MUTED);
            TextView la=tv(labels[i],10,i==selected);la.setGravity(Gravity.CENTER);la.setTextColor(i==selected?ACCENT:MUTED);
            t.addView(ic);t.addView(la);t.setOnClickListener(ls[i]);nav.addView(t,new LinearLayout.LayoutParams(0,-1,1));
        }
        return nav;
    }

    private LinearLayout card(){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(bg(CARD,22));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(12));c.setLayoutParams(p);return c;}
    private void section(String s){TextView h=tv(s,20,true);h.setPadding(dp(2),dp(12),0,dp(10));content.addView(h);}
    private void empty(String s){TextView e=tv(s,15,false);e.setTextColor(MUTED);e.setGravity(Gravity.CENTER);e.setPadding(dp(8),dp(28),dp(8),dp(28));content.addView(e);}

    private double[] totals(){double in=0,out=0;JSONArray a=arr("tx");for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i);if(thisMonth(o.optString("date"))){if("Inkomst".equals(o.optString("type")))in+=o.optDouble("amount");else out+=o.optDouble("amount");}}catch(Exception ignored){}return new double[]{in,out};}

    private void showOverview(){
        ensureRecurringTransactions();shell("Min Ekonomi",0);double[] t=totals();double left=t[0]-t[1];double budget=prefs.getFloat("budget",0);
        LinearLayout hero=card();hero.setPadding(dp(20),dp(19),dp(20),dp(18));hero.setBackground(bg(Color.rgb(25,54,49),26));
        TextView l=tv("Kvar denna månad",14,false);l.setTextColor(Color.rgb(184,216,207));hero.addView(l);
        TextView value=tv(format(left),34,true);value.setTextColor(left>=0?ACCENT:RED);value.setPadding(0,dp(4),0,dp(12));hero.addView(value);
        LinearLayout pair=new LinearLayout(this);pair.setOrientation(LinearLayout.HORIZONTAL);
        pair.addView(miniMetric("Inkomster",format(t[0]),ACCENT),new LinearLayout.LayoutParams(0,-2,1));
        pair.addView(miniMetric("Utgifter",format(t[1]),RED),new LinearLayout.LayoutParams(0,-2,1));hero.addView(pair);
        if(budget>0){TextView b=tv("Budget använd: "+Math.min(999,(int)Math.round((t[1]/budget)*100))+" %",12,false);b.setTextColor(MUTED);b.setPadding(0,dp(12),0,dp(5));hero.addView(b);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress((int)Math.min(100,Math.round((t[1]/budget)*100)));hero.addView(p,new LinearLayout.LayoutParams(-1,dp(6)));}
        content.addView(hero);

        TextView add=action("＋  Lägg till transaktion");add.setOnClickListener(v->transactionDialog(-1));LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(-1,-2);ap.setMargins(0,dp(2),0,dp(8));content.addView(add,ap);

        section("Kommande räkningar");addUpcomingBills();
        section("Senaste");JSONArray a=arr("tx");if(a.length()==0)empty("Inga transaktioner ännu.");int start=Math.max(0,a.length()-4);for(int i=a.length()-1;i>=start;i--)addTxRow(a,i,false);
    }

    private View miniMetric(String label,String val,int color){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);TextView a=tv(label,12,false);a.setTextColor(MUTED);TextView b=tv(val,17,true);b.setTextColor(color);b.setPadding(0,dp(3),0,0);c.addView(a);c.addView(b);return c;}

    private void addUpcomingBills(){JSONArray a=arr("bills");if(a.length()==0){empty("Inga kommande räkningar.");return;}int shown=0;for(int i=0;i<a.length()&&shown<3;i++)try{JSONObject o=a.getJSONObject(i);LinearLayout c=card();LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);TextView n=tv(o.optString("name"),16,true);TextView d=tv("Förfaller "+o.optString("due"),12,false);d.setTextColor(MUTED);txt.addView(n);txt.addView(d);row.addView(txt,new LinearLayout.LayoutParams(0,-2,1));TextView am=tv(format(o.optDouble("amount")),16,true);am.setTextColor(RED);row.addView(am);c.addView(row);content.addView(c);shown++;}catch(Exception ignored){} }

    private void showTransactions(){shell("Transaktioner",1);TextView add=action("＋  Ny transaktion");add.setOnClickListener(v->transactionDialog(-1));content.addView(add);section("Alla poster");JSONArray a=arr("tx");if(a.length()==0)empty("Inga transaktioner ännu.");for(int i=a.length()-1;i>=0;i--)addTxRow(a,i,true);}

    private void addTxRow(JSONArray a,int i,boolean controls){
        try{JSONObject o=a.getJSONObject(i);LinearLayout c=card();LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);
            TextView icon=tv(categoryIcon(o.optString("category")),20,true);icon.setGravity(Gravity.CENTER);icon.setBackground(bg(CARD2,16));row.addView(icon,new LinearLayout.LayoutParams(dp(44),dp(44)));
            LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);txt.setPadding(dp(12),0,dp(8),0);TextView n=tv(o.optString("name"),16,true);TextView sub=tv(o.optString("category")+" • "+o.optString("date")+(o.optBoolean("recurring")?" • återkommande":""),12,false);sub.setTextColor(MUTED);txt.addView(n);txt.addView(sub);row.addView(txt,new LinearLayout.LayoutParams(0,-2,1));
            boolean inc="Inkomst".equals(o.optString("type"));TextView m=tv((inc?"+ ":"− ")+format(o.optDouble("amount")),16,true);m.setTextColor(inc?ACCENT:RED);row.addView(m);c.addView(row);
            if(controls){LinearLayout actions=new LinearLayout(this);actions.setPadding(0,dp(10),0,0);TextView edit=smallAction("Redigera",Color.rgb(48,69,88));TextView del=smallAction("Ta bort",Color.rgb(92,48,54));final int idx=i;edit.setOnClickListener(v->transactionDialog(idx));del.setOnClickListener(v->confirmDeleteTx(idx));actions.addView(edit,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams dlp=new LinearLayout.LayoutParams(0,-2,1);dlp.setMargins(dp(8),0,0,0);actions.addView(del,dlp);c.addView(actions);}content.addView(c);
        }catch(Exception ignored){}
    }

    private String categoryIcon(String c){if(c.equals("Mat"))return "🍴";if(c.equals("Boende"))return "⌂";if(c.equals("Bil/transport"))return "➜";if(c.equals("Abonnemang"))return "◉";if(c.equals("Nöjen"))return "★";if(c.equals("Hälsa"))return "+";if(c.equals("Shopping"))return "▣";if(c.equals("Lön"))return "↑";return "•";}

    private void confirmDeleteTx(int idx){new AlertDialog.Builder(this).setTitle("Ta bort transaktion?").setMessage("Det går inte att ångra.").setNegativeButton("Avbryt",null).setPositiveButton("Ta bort",(d,w)->{JSONArray x=arr("tx"),y=new JSONArray();for(int j=0;j<x.length();j++)if(j!=idx)try{y.put(x.getJSONObject(j));}catch(Exception ignored){}save("tx",y);showTransactions();}).show();}

    private void transactionDialog(int editIndex){
        JSONObject old=null;JSONArray all=arr("tx");if(editIndex>=0)try{old=all.getJSONObject(editIndex);}catch(Exception ignored){}
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(20),dp(4),dp(20),0);
        EditText name=new EditText(this);name.setHint("Beskrivning, t.ex. ICA");EditText amount=new EditText(this);amount.setHint("Belopp");amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        Spinner type=new Spinner(this);String[] types={"Utgift","Inkomst"};type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,types));
        String[] cats={"Mat","Boende","Bil/transport","Abonnemang","Nöjen","Hälsa","Shopping","Lön","Övrigt"};Spinner cat=new Spinner(this);cat.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,cats));
        CheckBox recurring=new CheckBox(this);recurring.setText("Återkommande varje månad");
        if(old!=null){name.setText(old.optString("name"));amount.setText(String.valueOf(old.optDouble("amount")));type.setSelection("Inkomst".equals(old.optString("type"))?1:0);for(int i=0;i<cats.length;i++)if(cats[i].equals(old.optString("category")))cat.setSelection(i);recurring.setChecked(old.optBoolean("recurring"));}
        box.addView(name);box.addView(amount);box.addView(type);box.addView(cat);box.addView(recurring);
        final JSONObject original=old;AlertDialog d=new AlertDialog.Builder(this).setTitle(editIndex>=0?"Redigera transaktion":"Ny transaktion").setView(box).setNegativeButton("Avbryt",null).setPositiveButton("Spara",null).create();
        d.setOnShowListener(x->d.getButton(-1).setOnClickListener(v->{if(name.getText().toString().trim().isEmpty()||parse(amount)<=0){Toast.makeText(this,"Fyll i namn och belopp",Toast.LENGTH_SHORT).show();return;}try{JSONObject o=new JSONObject();o.put("name",name.getText().toString().trim());o.put("amount",parse(amount));o.put("type",type.getSelectedItem().toString());o.put("category",cat.getSelectedItem().toString());o.put("date",original!=null?original.optString("date",now()):now());o.put("recurring",recurring.isChecked());if(recurring.isChecked())o.put("series",original!=null&&original.has("series")?original.optString("series"):UUID.randomUUID().toString());JSONArray src=arr("tx");if(editIndex>=0){JSONArray y=new JSONArray();for(int j=0;j<src.length();j++){if(j==editIndex)y.put(o);else y.put(src.getJSONObject(j));}save("tx",y);}else{src.put(o);save("tx",src);}d.dismiss();showTransactions();}catch(Exception ignored){}}));d.show();
    }

    private void ensureRecurringTransactions(){
        JSONArray a=arr("tx");String month=currentMonthKey();Set<String> current=new HashSet<>();Map<String,JSONObject> latest=new HashMap<>();
        for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i);if(!o.optBoolean("recurring"))continue;String s=o.optString("series");if(s.isEmpty()){s=UUID.randomUUID().toString();o.put("series",s);}if(thisMonth(o.optString("date")))current.add(s);JSONObject prev=latest.get(s);if(prev==null||o.optString("date").compareTo(prev.optString("date"))>0)latest.put(s,o);}catch(Exception ignored){}
        boolean changed=false;for(Map.Entry<String,JSONObject> e:latest.entrySet())if(!current.contains(e.getKey()))try{JSONObject src=e.getValue();JSONObject n=new JSONObject(src.toString());String old=src.optString("date");String day=old.length()>=10?old.substring(8,10):"01";n.put("date",month+"-"+day);a.put(n);changed=true;}catch(Exception ignored){}if(changed)save("tx",a);
    }

    private void showBudget(){
        shell("Budget",2);double b=prefs.getFloat("budget",0),spent=totals()[1],left=b-spent;
        LinearLayout c=card();c.addView(tv("Månadsbudget",13,false));TextView val=tv(format(b),28,true);val.setPadding(0,dp(3),0,dp(10));c.addView(val);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(100);p.setProgress(b>0?(int)Math.min(100,Math.round(spent/b*100)):0);c.addView(p,new LinearLayout.LayoutParams(-1,dp(7)));TextView sub=tv("Förbrukat "+format(spent)+"   •   Kvar "+format(left),13,false);sub.setTextColor(left>=0?MUTED:RED);sub.setPadding(0,dp(10),0,0);c.addView(sub);content.addView(c);
        TextView edit=action("Ändra månadsbudget");edit.setOnClickListener(v->budgetDialog());content.addView(edit);section("Utgifter per kategori");categorySpend(false);
    }
    private void budgetDialog(){EditText e=new EditText(this);e.setHint("t.ex. 15000");e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);float v=prefs.getFloat("budget",0);if(v>0)e.setText(String.valueOf(v));new AlertDialog.Builder(this).setTitle("Månadsbudget").setView(e).setNegativeButton("Avbryt",null).setPositiveButton("Spara",(d,w)->{prefs.edit().putFloat("budget",(float)parse(e)).apply();showBudget();}).show();}

    private Map<String,Double> categoryMap(){Map<String,Double> m=new LinkedHashMap<>();JSONArray a=arr("tx");for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i);if(thisMonth(o.optString("date"))&&"Utgift".equals(o.optString("type")))m.put(o.optString("category"),m.getOrDefault(o.optString("category"),0.0)+o.optDouble("amount"));}catch(Exception ignored){}return m;}
    private void categorySpend(boolean compact){Map<String,Double> m=categoryMap();if(m.isEmpty()){empty("Ingen förbrukning att visa.");return;}double max=1;for(double v:m.values())max=Math.max(max,v);for(Map.Entry<String,Double> e:m.entrySet()){LinearLayout c=card();LinearLayout r=new LinearLayout(this);TextView n=tv(categoryIcon(e.getKey())+"  "+e.getKey(),15,true);TextView v=tv(format(e.getValue()),15,true);r.addView(n,new LinearLayout.LayoutParams(0,-2,1));r.addView(v);c.addView(r);ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(1000);p.setProgress((int)(e.getValue()/max*1000));LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(5));lp.setMargins(0,dp(8),0,0);c.addView(p,lp);content.addView(c);}}

    private void showBills(){shell("Räkningar",3);TextView add=action("＋  Lägg till räkning");add.setOnClickListener(v->billDialog(-1));content.addView(add);section("Kommande");JSONArray a=arr("bills");if(a.length()==0)empty("Inga räkningar inlagda.");for(int i=0;i<a.length();i++)addBillRow(a,i);}
    private void addBillRow(JSONArray a,int i){try{JSONObject o=a.getJSONObject(i);LinearLayout c=card();LinearLayout row=new LinearLayout(this);row.setGravity(Gravity.CENTER_VERTICAL);LinearLayout txt=new LinearLayout(this);txt.setOrientation(LinearLayout.VERTICAL);TextView n=tv(o.optString("name"),16,true);TextView sub=tv("Förfaller "+o.optString("due")+(o.optBoolean("recurring")?" • varje månad":""),12,false);sub.setTextColor(MUTED);txt.addView(n);txt.addView(sub);row.addView(txt,new LinearLayout.LayoutParams(0,-2,1));TextView am=tv(format(o.optDouble("amount")),17,true);am.setTextColor(RED);row.addView(am);c.addView(row);LinearLayout actions=new LinearLayout(this);actions.setPadding(0,dp(10),0,0);final int idx=i;TextView paid=smallAction("Betald",Color.rgb(38,77,61));paid.setOnClickListener(v->payBill(idx));TextView edit=smallAction("Redigera",Color.rgb(48,69,88));edit.setOnClickListener(v->billDialog(idx));actions.addView(paid,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(0,-2,1);ep.setMargins(dp(8),0,0,0);actions.addView(edit,ep);c.addView(actions);content.addView(c);}catch(Exception ignored){}}
    private void billDialog(int editIndex){JSONArray all=arr("bills");JSONObject old=null;if(editIndex>=0)try{old=all.getJSONObject(editIndex);}catch(Exception ignored){}LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(18),0,dp(18),0);EditText n=new EditText(this);n.setHint("Räkning");EditText a=new EditText(this);a.setHint("Belopp");a.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);EditText due=new EditText(this);due.setHint("Förfallodatum, t.ex. 2026-09-28");CheckBox recurring=new CheckBox(this);recurring.setText("Återkommer varje månad");if(old!=null){n.setText(old.optString("name"));a.setText(String.valueOf(old.optDouble("amount")));due.setText(old.optString("due"));recurring.setChecked(old.optBoolean("recurring"));}box.addView(n);box.addView(a);box.addView(due);box.addView(recurring);final JSONObject original=old;new AlertDialog.Builder(this).setTitle(editIndex>=0?"Redigera räkning":"Ny räkning").setView(box).setNegativeButton("Avbryt",null).setPositiveButton("Spara",(d,w)->{if(n.getText().toString().trim().isEmpty()||parse(a)<=0)return;try{JSONObject o=new JSONObject();o.put("name",n.getText().toString().trim());o.put("amount",parse(a));o.put("due",due.getText().toString().trim());o.put("recurring",recurring.isChecked());JSONArray src=arr("bills");if(editIndex>=0){JSONArray y=new JSONArray();for(int j=0;j<src.length();j++)y.put(j==editIndex?o:src.getJSONObject(j));save("bills",y);}else{src.put(o);save("bills",src);}showBills();}catch(Exception ignored){}}).show();}
    private void payBill(int idx){JSONArray x=arr("bills");try{JSONObject o=x.getJSONObject(idx);if(o.optBoolean("recurring")){o.put("due",nextMonth(o.optString("due")));x.put(idx,o);save("bills",x);}else{JSONArray y=new JSONArray();for(int j=0;j<x.length();j++)if(j!=idx)y.put(x.getJSONObject(j));save("bills",y);}showBills();}catch(Exception ignored){}}
    private String nextMonth(String date){try{SimpleDateFormat f=new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault());Date d=f.parse(date);Calendar c=Calendar.getInstance();c.setTime(d);c.add(Calendar.MONTH,1);return f.format(c.getTime());}catch(Exception e){Calendar c=Calendar.getInstance();c.add(Calendar.MONTH,1);return new SimpleDateFormat("yyyy-MM-dd",Locale.getDefault()).format(c.getTime());}}

    private void showStats(){shell("Statistik",4);double[] t=totals();LinearLayout top=card();TextView h=tv("Den här månaden",16,true);top.addView(h);LinearLayout row=new LinearLayout(this);row.setPadding(0,dp(12),0,0);row.addView(miniMetric("In",format(t[0]),ACCENT),new LinearLayout.LayoutParams(0,-2,1));row.addView(miniMetric("Ut",format(t[1]),RED),new LinearLayout.LayoutParams(0,-2,1));row.addView(miniMetric("Resultat",format(t[0]-t[1]),t[0]-t[1]>=0?ACCENT:RED),new LinearLayout.LayoutParams(0,-2,1));top.addView(row);content.addView(top);section("Kategorier");categorySpend(true);section("Månad för månad");JSONArray a=arr("tx");Map<String,double[]> months=new TreeMap<>(Collections.reverseOrder());for(int i=0;i<a.length();i++)try{JSONObject o=a.getJSONObject(i);String d=o.optString("date");if(d.length()<7)continue;String m=d.substring(0,7);double[] v=months.getOrDefault(m,new double[]{0,0});if("Inkomst".equals(o.optString("type")))v[0]+=o.optDouble("amount");else v[1]+=o.optDouble("amount");months.put(m,v);}catch(Exception ignored){}if(months.isEmpty())empty("Statistik visas när du lagt in transaktioner.");for(Map.Entry<String,double[]> e:months.entrySet()){LinearLayout c=card();TextView mm=tv(e.getKey(),16,true);c.addView(mm);LinearLayout r=new LinearLayout(this);r.setPadding(0,dp(8),0,0);r.addView(miniMetric("In",format(e.getValue()[0]),ACCENT),new LinearLayout.LayoutParams(0,-2,1));r.addView(miniMetric("Ut",format(e.getValue()[1]),RED),new LinearLayout.LayoutParams(0,-2,1));r.addView(miniMetric("Netto",format(e.getValue()[0]-e.getValue()[1]),e.getValue()[0]>=e.getValue()[1]?ACCENT:RED),new LinearLayout.LayoutParams(0,-2,1));c.addView(r);content.addView(c);}}
}
