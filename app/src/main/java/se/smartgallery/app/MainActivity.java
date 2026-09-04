package se.smartgallery.app;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.*;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    static final int REQ=44;
    final int BG=Color.rgb(11,15,20), CARD=Color.rgb(24,30,38), ACC=Color.rgb(123,231,255);
    LinearLayout root, top; GridView grid; TextView info; EditText search; ArrayList<Item> all=new ArrayList<>(), shown=new ArrayList<>();
    GalleryAdapter adapter; ExecutorService pool=Executors.newFixedThreadPool(4); SharedPreferences prefs;
    String filter="Alla";

    static class Item {
        long id,date,size; String name,path,mime; Uri uri;
        Item(long i,long d,long s,String n,String p,String m,Uri u){id=i;date=d;size=s;name=n;path=p;mime=m;uri=u;}
        boolean video(){return mime!=null&&mime.startsWith("video/");}
    }

    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("gallery",0);build();request();}
    @Override protected void onDestroy(){pool.shutdownNow();super.onDestroy();}

    TextView tv(String s,int sp,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(Color.WHITE);v.setTypeface(null,bold?1:0);return v;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(CARD));return b;}

    void build(){
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(14,18,14,8);root.setBackgroundColor(BG);
        TextView title=tv("SMART GALLERY",27,true);root.addView(title);
        TextView sub=tv("Sortera • hitta • städa • lokalt på telefonen",14,false);sub.setTextColor(ACC);root.addView(sub);

        search=new EditText(this);search.setHint("Sök filnamn, album eller mapp…");search.setSingleLine(true);search.setTextColor(Color.WHITE);search.setHintTextColor(Color.GRAY);root.addView(search,new LinearLayout.LayoutParams(-1,-2));
        search.setOnEditorActionListener((v,a,e)->{applyFilter();return true;});

        HorizontalScrollView hs=new HorizontalScrollView(this);top=new LinearLayout(this);top.setOrientation(LinearLayout.HORIZONTAL);
        String[] modes={"Alla","Kamera","Skärmbilder","Videor","Favoriter","Stora filer","Dubbletter","Album"};
        for(String m:modes){Button b=button(m);b.setOnClickListener(v->{filter=m;applyFilter();});top.addView(b);}
        hs.addView(top);root.addView(hs,new LinearLayout.LayoutParams(-1,-2));

        info=tv("Läser bilder…",14,false);info.setPadding(4,8,4,8);info.setTextColor(Color.LTGRAY);root.addView(info);
        grid=new GridView(this);grid.setNumColumns(getResources().getConfiguration().smallestScreenWidthDp>=600?6:3);grid.setHorizontalSpacing(5);grid.setVerticalSpacing(5);grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        adapter=new GalleryAdapter();grid.setAdapter(adapter);root.addView(grid,new LinearLayout.LayoutParams(-1,0,1));
        grid.setOnItemClickListener((p,v,pos,id)->open(shown.get(pos)));
        grid.setOnItemLongClickListener((p,v,pos,id)->{favoriteDialog(shown.get(pos));return true;});

        LinearLayout bar=new LinearLayout(this);bar.setOrientation(LinearLayout.HORIZONTAL);
        Button refresh=button("↻ Uppdatera");refresh.setOnClickListener(v->load());bar.addView(refresh,new LinearLayout.LayoutParams(0,-2,1));
        Button clean=button("🧹 Städa");clean.setOnClickListener(v->cleanPage());bar.addView(clean,new LinearLayout.LayoutParams(0,-2,1));
        Button albums=button("📁 Album");albums.setOnClickListener(v->{filter="Album";applyFilter();});bar.addView(albums,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(bar);
        setContentView(root);
    }

    void request(){
        ArrayList<String> q=new ArrayList<>();
        if(Build.VERSION.SDK_INT>=33){if(checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES)!=PackageManager.PERMISSION_GRANTED)q.add(Manifest.permission.READ_MEDIA_IMAGES);if(checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO)!=PackageManager.PERMISSION_GRANTED)q.add(Manifest.permission.READ_MEDIA_VIDEO);}else if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)q.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        if(q.isEmpty())load();else requestPermissions(q.toArray(new String[0]),REQ);
    }
    @Override public void onRequestPermissionsResult(int r,String[]p,int[]g){super.onRequestPermissionsResult(r,p,g);if(r==REQ)load();}

    void load(){info.setText("Läser mediebibliotek…");pool.execute(()->{
        ArrayList<Item> list=new ArrayList<>();
        Uri u=MediaStore.Files.getContentUri("external");
        ArrayList<String> cols=new ArrayList<>(Arrays.asList(MediaStore.Files.FileColumns._ID,MediaStore.Files.FileColumns.DISPLAY_NAME,MediaStore.Files.FileColumns.MIME_TYPE,MediaStore.Files.FileColumns.SIZE,MediaStore.Files.FileColumns.DATE_MODIFIED,MediaStore.Files.FileColumns.MEDIA_TYPE));
        if(Build.VERSION.SDK_INT>=29)cols.add(MediaStore.Files.FileColumns.RELATIVE_PATH);else cols.add(MediaStore.Files.FileColumns.DATA);
        String sel=MediaStore.Files.FileColumns.MEDIA_TYPE+"=? OR "+MediaStore.Files.FileColumns.MEDIA_TYPE+"=?";
        String[] args={"1","3"};
        try(Cursor c=getContentResolver().query(u,cols.toArray(new String[0]),sel,args,MediaStore.Files.FileColumns.DATE_MODIFIED+" DESC")){
            if(c!=null){int id=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID),nm=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME),mi=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE),sz=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.SIZE),dt=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_MODIFIED),mt=c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MEDIA_TYPE),pa=c.getColumnIndexOrThrow(Build.VERSION.SDK_INT>=29?MediaStore.Files.FileColumns.RELATIVE_PATH:MediaStore.Files.FileColumns.DATA);while(c.moveToNext()){
                long x=c.getLong(id);int type=c.getInt(mt);Uri item=type==3?Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,""+x):Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,""+x);list.add(new Item(x,c.getLong(dt),c.getLong(sz),c.getString(nm),c.getString(pa),c.getString(mi),item));
            }}
        }catch(Exception e){}
        runOnUiThread(()->{all=list;applyFilter();});
    });}

    void applyFilter(){
        String q=search.getText().toString().trim().toLowerCase(Locale.ROOT);shown.clear();
        if(filter.equals("Album")){showAlbums(q);return;}
        HashMap<String,Integer> dup=new HashMap<>();for(Item i:all){String k=i.size+"|"+(i.name==null?"":i.name.toLowerCase(Locale.ROOT));dup.put(k,dup.getOrDefault(k,0)+1);}
        for(Item i:all){String text=((i.name==null?"":i.name)+" "+(i.path==null?"":i.path)).toLowerCase(Locale.ROOT);if(!q.isEmpty()&&!text.contains(q))continue;boolean ok=true;
            if(filter.equals("Kamera"))ok=text.contains("dcim/camera");
            else if(filter.equals("Skärmbilder"))ok=text.contains("screenshot")||text.contains("screenshots");
            else if(filter.equals("Videor"))ok=i.video();
            else if(filter.equals("Favoriter"))ok=prefs.getBoolean("fav_"+i.uri,false);
            else if(filter.equals("Stora filer"))ok=i.size>=20L*1024*1024;
            else if(filter.equals("Dubbletter")){String k=i.size+"|"+(i.name==null?"":i.name.toLowerCase(Locale.ROOT));ok=dup.getOrDefault(k,0)>1;}
            if(ok)shown.add(i);
        }
        adapter.notifyDataSetChanged();info.setText(filter+" • "+shown.size()+" objekt"+(filter.equals("Dubbletter")?" • kandidater, inget raderas automatiskt":""));
    }

    void showAlbums(String q){
        LinkedHashMap<String,Integer> m=new LinkedHashMap<>();for(Item i:all){String p=i.path==null?"Okänd mapp":i.path;String name=p;String[] parts=p.replace('\\','/').split("/");if(parts.length>0&&parts[parts.length-1].length()>0)name=parts[parts.length-1];else if(parts.length>1)name=parts[parts.length-2];if(!q.isEmpty()&&!name.toLowerCase(Locale.ROOT).contains(q))continue;m.put(name,m.getOrDefault(name,0)+1);}String[] arr=new String[m.size()];int k=0;for(Map.Entry<String,Integer>e:m.entrySet())arr[k++]=e.getKey()+"  •  "+e.getValue();new AlertDialog.Builder(this).setTitle("Album / mappar").setItems(arr,(d,w)->{String album=arr[w].split("  •  ")[0];search.setText(album);filter="Alla";applyFilter();}).setPositiveButton("Stäng",null).show();info.setText("Album • "+m.size()+" mappar");adapter.notifyDataSetChanged();}

    void open(Item i){try{Intent x=new Intent(Intent.ACTION_VIEW);x.setDataAndType(i.uri,i.mime);x.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(x);}catch(Exception e){Toast.makeText(this,"Kan inte öppna filen",Toast.LENGTH_SHORT).show();}}
    void favoriteDialog(Item i){boolean f=prefs.getBoolean("fav_"+i.uri,false);new AlertDialog.Builder(this).setTitle(i.name).setMessage((i.video()?"Video":"Bild")+"\n"+human(i.size)+"\n"+(i.path==null?"":i.path)).setPositiveButton(f?"Ta bort favorit":"★ Favorit",(d,w)->{prefs.edit().putBoolean("fav_"+i.uri,!f).apply();applyFilter();}).setNeutralButton("Öppna",(d,w)->open(i)).setNegativeButton("Stäng",null).show();}

    void cleanPage(){
        long large=0;int screenshots=0,dups=0;HashMap<String,Integer> m=new HashMap<>();for(Item i:all){if(i.size>=20L*1024*1024)large+=i.size;String t=((i.name==null?"":i.name)+" "+(i.path==null?"":i.path)).toLowerCase(Locale.ROOT);if(t.contains("screenshot"))screenshots++;String k=i.size+"|"+(i.name==null?"":i.name.toLowerCase(Locale.ROOT));m.put(k,m.getOrDefault(k,0)+1);}for(int n:m.values())if(n>1)dups+=n;
        final int s=screenshots,d=dups;final long l=large;new AlertDialog.Builder(this).setTitle("🧹 Smart städning").setMessage("Skärmbilder: "+s+"\nStora filer ≥20 MB: "+human(l)+"\nMöjliga dubbletter: "+d+"\n\nSmart Gallery raderar aldrig automatiskt. Välj en kategori och granska själv.").setPositiveButton("Stora filer",(x,w)->{filter="Stora filer";applyFilter();}).setNeutralButton("Dubbletter",(x,w)->{filter="Dubbletter";applyFilter();}).setNegativeButton("Stäng",null).show();
    }

    String human(long b){if(b<1024*1024)return (b/1024)+" KB";if(b<1024L*1024*1024)return String.format(Locale.ROOT,"%.1f MB",b/1048576.0);return String.format(Locale.ROOT,"%.2f GB",b/1073741824.0);}

    class GalleryAdapter extends BaseAdapter {
        public int getCount(){return shown.size();}public Object getItem(int p){return shown.get(p);}public long getItemId(int p){return shown.get(p).id;}
        public View getView(int p,View old,android.view.ViewGroup parent){ImageView im=old instanceof ImageView?(ImageView)old:new ImageView(MainActivity.this);int h=(getResources().getDisplayMetrics().widthPixels-38)/3;im.setLayoutParams(new GridView.LayoutParams(-1,h));im.setScaleType(ImageView.ScaleType.CENTER_CROP);im.setBackgroundColor(CARD);Item item=shown.get(p);im.setTag(item.uri.toString());im.setImageDrawable(null);pool.execute(()->{Bitmap b=null;try{if(Build.VERSION.SDK_INT>=29)b=getContentResolver().loadThumbnail(item.uri,new android.util.Size(360,360),null);else{java.io.InputStream in=getContentResolver().openInputStream(item.uri);if(in!=null){BitmapFactory.Options o=new BitmapFactory.Options();o.inSampleSize=4;b=BitmapFactory.decodeStream(in,null,o);in.close();}}}catch(Exception e){}final Bitmap bm=b;runOnUiThread(()->{if(item.uri.toString().equals(im.getTag())&&bm!=null)im.setImageBitmap(bm);});});return im;}
    }
}
