package se.smartgallery.app;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class MainActivity13 extends MainActivity {
    static final int REQ_WRITE = 913;
    final LinkedHashSet<String> selected = new LinkedHashSet<>();
    ArrayList<Item> pendingMove = new ArrayList<>();
    String pendingPath = null;
    LinearLayout selectionBar;
    boolean selectionMode=false;
    int returnScreen=0;

    @Override public void onBackPressed(){
        if(screen==3){selected.clear();selectionMode=false;if(returnScreen==2&&currentAlbum!=null)openAlbum(currentAlbum);else showMain();return;}
        if(selectionMode){clearSelection();return;}
        super.onBackPressed();
    }

    @Override void showMain(){super.showMain();screen=0;installSelectionUi(true);}
    @Override void openAlbum(Album a){super.openAlbum(a);screen=2;installSelectionUi(false);}
    @Override void showAlbumsPage(){super.showAlbumsPage();screen=1;selected.clear();selectionMode=false;addCreateFolderButton();}

    void installSelectionUi(boolean hasBottomBar){
        if(grid==null)return;
        adapter=new SelectAdapter();grid.setAdapter(adapter);
        grid.setOnItemClickListener((p,v,pos,id)->{Item i=shown.get(pos);if(selectionMode){toggle(i);}else open(i);});
        grid.setOnItemLongClickListener((p,v,pos,id)->{selectionMode=true;toggle(shown.get(pos));return true;});
        selectionBar=new LinearLayout(this);selectionBar.setGravity(Gravity.CENTER);selectionBar.setVisibility(View.GONE);selectionBar.setBackground(bg(Color.rgb(18,24,31),14));
        Button move=button("📁 Flytta");move.setOnClickListener(v->chooseMoveTarget());selectionBar.addView(move,new LinearLayout.LayoutParams(0,-2,1));
        Button person=button("👤 Samma person");person.setOnClickListener(v->findSamePerson());selectionBar.addView(person,new LinearLayout.LayoutParams(0,-2,1));
        Button share=button("↗ Dela");share.setOnClickListener(v->shareSelected());selectionBar.addView(share,new LinearLayout.LayoutParams(0,-2,1));
        Button cancel=button("✕");cancel.setOnClickListener(v->clearSelection());selectionBar.addView(cancel,new LinearLayout.LayoutParams(dp(52),-2));
        int idx=hasBottomBar?Math.max(0,root.getChildCount()-1):root.getChildCount();
        root.addView(selectionBar,idx,new LinearLayout.LayoutParams(-1,-2));
    }

    void updateSelection(){
        if(selectionBar==null)return;
        selectionBar.setVisibility(selectionMode?View.VISIBLE:View.GONE);
        if(selectionMode && selectionBar.getChildCount()>1){Button person=(Button)selectionBar.getChildAt(1);person.setEnabled(selected.size()==1);person.setAlpha(selected.size()==1?1f:.4f);}
        if(info!=null&&selectionMode)info.setText("✓ "+selected.size()+" markerade");
        if(adapter!=null)adapter.notifyDataSetChanged();
    }
    void toggle(Item i){String k=i.uri.toString();if(selected.contains(k))selected.remove(k);else selected.add(k);if(selected.isEmpty())selectionMode=false;updateSelection();}
    void clearSelection(){selected.clear();selectionMode=false;updateSelection();if(screen==0)applyFilter();}
    ArrayList<Item> selectedItems(){ArrayList<Item>r=new ArrayList<>();for(Item i:all)if(selected.contains(i.uri.toString()))r.add(i);return r;}

    class SelectAdapter extends GalleryAdapter{
        @Override public View getView(int p,View old,ViewGroup parent){View v=super.getView(p,old,parent);if(v instanceof FrameLayout){FrameLayout f=(FrameLayout)v;Item i=shown.get(p);if(selected.contains(i.uri.toString())){TextView c=tv("✓",18,true);c.setGravity(Gravity.CENTER);c.setTextColor(Color.WHITE);c.setBackground(bg(Color.argb(220,20,145,190),30));FrameLayout.LayoutParams lp=new FrameLayout.LayoutParams(dp(34),dp(34),Gravity.TOP|Gravity.LEFT);lp.setMargins(dp(6),dp(6),0,0);f.addView(c,lp);}}return v;}
    }

    void addCreateFolderButton(){
        if(root==null||root.getChildCount()==0)return;View h=root.getChildAt(0);if(!(h instanceof LinearLayout))return;Button plus=button("＋");plus.setTextSize(24);plus.setOnClickListener(v->createLogicalFolder());((LinearLayout)h).addView(plus,Math.max(1,((LinearLayout)h).getChildCount()-1),new LinearLayout.LayoutParams(dp(52),-2));
    }
    void createLogicalFolder(){
        EditText e=new EditText(this);e.setHint("Namn på ny mapp");e.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("📁 Skapa ny mapp").setMessage("Mappen skapas i Pictures/Smart Gallery. Den blir en fysisk mediamapp när första bilden flyttas dit.").setView(e).setPositiveButton("Skapa",(d,w)->{String n=safeFolder(e.getText().toString());if(n.isEmpty())return;Set<String>s=new LinkedHashSet<>(prefs.getStringSet("custom_folders",Collections.emptySet()));s.add("Pictures/Smart Gallery/"+n+"/");prefs.edit().putStringSet("custom_folders",s).apply();Toast.makeText(this,"Mappen ”"+n+"” skapad",Toast.LENGTH_SHORT).show();}).setNegativeButton("Avbryt",null).show();
    }
    String safeFolder(String s){s=s.trim().replaceAll("[\\\\/:*?\"<>|]"," ").replaceAll("\\s+"," ");return s.length()>60?s.substring(0,60):s;}

    void chooseMoveTarget(){
        ArrayList<Item>items=selectedItems();if(items.isEmpty())return;
        LinkedHashMap<String,String>paths=new LinkedHashMap<>();for(Item i:all)if(i.path!=null)paths.put(albumName(i.path),i.path);
        for(String p:prefs.getStringSet("custom_folders",Collections.emptySet()))paths.put(albumName(p),p);
        ArrayList<String>names=new ArrayList<>();names.add("＋ Ny mapp");names.addAll(paths.keySet());
        new AlertDialog.Builder(this).setTitle("Flytta "+items.size()+" objekt till…").setItems(names.toArray(new String[0]),(d,w)->{if(w==0){askNewMoveFolder(items);}else{String name=names.get(w);beginMove(items,paths.get(name));}}).setNegativeButton("Avbryt",null).show();
    }
    void askNewMoveFolder(ArrayList<Item>items){EditText e=new EditText(this);e.setHint("Nytt mappnamn");new AlertDialog.Builder(this).setTitle("Ny mapp").setView(e).setPositiveButton("Skapa och flytta",(d,w)->{String n=safeFolder(e.getText().toString());if(!n.isEmpty())beginMove(items,"Pictures/Smart Gallery/"+n+"/");}).setNegativeButton("Avbryt",null).show();}

    void beginMove(ArrayList<Item>items,String path){
        if(path==null||items.isEmpty())return;pendingMove=new ArrayList<>(items);pendingPath=path.endsWith("/")?path:path+"/";
        if(Build.VERSION.SDK_INT>=30){try{ArrayList<Uri>uris=new ArrayList<>();for(Item i:items)uris.add(i.uri);PendingIntent pi=MediaStore.createWriteRequest(getContentResolver(),uris);startIntentSenderForResult(pi.getIntentSender(),REQ_WRITE,null,0,0,0);return;}catch(Exception e){}}
        performMove();
    }
    @Override protected void onActivityResult(int r,int c,Intent data){super.onActivityResult(r,c,data);if(r==REQ_WRITE){if(c==RESULT_OK)performMove();else Toast.makeText(this,"Flytten avbröts",Toast.LENGTH_SHORT).show();}}
    void performMove(){
        final ArrayList<Item>items=new ArrayList<>(pendingMove);final String path=pendingPath;if(items.isEmpty()||path==null)return;
        pool.execute(()->{int ok=0;for(Item i:items){try{ContentValues cv=new ContentValues();cv.put(MediaStore.MediaColumns.RELATIVE_PATH,path);int n=getContentResolver().update(i.uri,cv,null,null);if(n>0)ok++;}catch(Exception ignored){}}final int done=ok;runOnUiThread(()->{Set<String>s=new LinkedHashSet<>(prefs.getStringSet("custom_folders",Collections.emptySet()));s.add(path);prefs.edit().putStringSet("custom_folders",s).apply();Toast.makeText(this,"Flyttade "+done+" av "+items.size()+" objekt",Toast.LENGTH_LONG).show();selected.clear();selectionMode=false;load();});});
    }

    void shareSelected(){ArrayList<Item>items=selectedItems();if(items.isEmpty())return;Intent x=new Intent(Intent.ACTION_SEND_MULTIPLE);x.setType("*/*");ArrayList<Uri>u=new ArrayList<>();for(Item i:items)u.add(i.uri);x.putParcelableArrayListExtra(Intent.EXTRA_STREAM,u);x.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(Intent.createChooser(x,"Dela "+items.size()+" objekt"));}

    static class FaceSig{float[] pix;Rect box;FaceSig(float[]p,Rect b){pix=p;box=b;}}
    FaceSig signature(Bitmap src,FaceDetector detector)throws Exception{
        if(src==null)return null;InputImage im=InputImage.fromBitmap(src,0);List<Face>faces=Tasks.await(detector.process(im),25,TimeUnit.SECONDS);if(faces==null||faces.isEmpty())return null;Face best=faces.get(0);for(Face f:faces)if(f.getBoundingBox().width()*f.getBoundingBox().height()>best.getBoundingBox().width()*best.getBoundingBox().height())best=f;Rect b=new Rect(best.getBoundingBox());int pad=(int)(Math.max(b.width(),b.height())*.12f);b.left=Math.max(0,b.left-pad);b.top=Math.max(0,b.top-pad);b.right=Math.min(src.getWidth(),b.right+pad);b.bottom=Math.min(src.getHeight(),b.bottom+pad);if(b.width()<20||b.height()<20)return null;Bitmap crop=Bitmap.createBitmap(src,b.left,b.top,b.width(),b.height());Bitmap sm=Bitmap.createScaledBitmap(crop,16,16,true);float[]v=new float[256];float mean=0;for(int y=0;y<16;y++)for(int x=0;x<16;x++){int c=sm.getPixel(x,y);float g=(Color.red(c)*.299f+Color.green(c)*.587f+Color.blue(c)*.114f)/255f;v[y*16+x]=g;mean+=g;}mean/=256f;float norm=0;for(int i=0;i<v.length;i++){v[i]-=mean;norm+=v[i]*v[i];}norm=(float)Math.sqrt(norm)+1e-6f;for(int i=0;i<v.length;i++)v[i]/=norm;return new FaceSig(v,b);
    }
    float sim(FaceSig a,FaceSig b){if(a==null||b==null)return-1;float s=0;for(int i=0;i<a.pix.length;i++)s+=a.pix[i]*b.pix[i];return s;}
    Bitmap thumb(Item i)throws Exception{if(Build.VERSION.SDK_INT>=29)return getContentResolver().loadThumbnail(i.uri,new android.util.Size(360,360),null);java.io.InputStream in=getContentResolver().openInputStream(i.uri);Bitmap b=BitmapFactory.decodeStream(in);if(in!=null)in.close();return b;}

    void findSamePerson(){
        ArrayList<Item>s=selectedItems();if(s.size()!=1){Toast.makeText(this,"Markera exakt en bild först",Toast.LENGTH_SHORT).show();return;}Item ref=s.get(0);if(ref.video()){Toast.makeText(this,"Välj en bild med personen",Toast.LENGTH_SHORT).show();return;}
        new AlertDialog.Builder(this).setTitle("👤 Hitta samma person").setMessage("Smart Gallery analyserar ansikten lokalt på telefonen. Det här är en beta-funktion: träffarna visas för granskning innan något flyttas. Analysen kan ta några minuter med ett stort bibliotek.").setPositiveButton("Starta",(d,w)->scanPerson(ref)).setNegativeButton("Avbryt",null).show();
    }
    void scanPerson(Item ref){
        final ProgressDialog pd=new ProgressDialog(this);pd.setTitle("Analyserar ansikten lokalt");pd.setMessage("Förbereder…");pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);pd.setMax(all.size());pd.setCancelable(true);pd.show();
        pool.execute(()->{FaceDetectorOptions opt=new FaceDetectorOptions.Builder().setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST).setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE).build();FaceDetector detector=FaceDetection.getClient(opt);ArrayList<Item>matches=new ArrayList<>();try{FaceSig rs=signature(thumb(ref),detector);if(rs==null){runOnUiThread(()->{pd.dismiss();Toast.makeText(this,"Kunde inte hitta ett tydligt ansikte i bilden",Toast.LENGTH_LONG).show();});detector.close();return;}int n=0;for(Item i:all){if(pd.isCancelled())break;n++;if(i.video())continue;try{FaceSig x=signature(thumb(i),detector);float score=sim(rs,x);if(score>=0.66f)matches.add(i);}catch(Exception ignored){}if(n%20==0){final int z=n;runOnUiThread(()->{pd.setProgress(z);pd.setMessage("Analyserat "+z+" av "+all.size());});}}}catch(Exception ignored){}detector.close();runOnUiThread(()->{pd.dismiss();showPersonResults(matches);});});
    }
    void showPersonResults(ArrayList<Item>matches){
        returnScreen=screen;screen=3;selected.clear();selectionMode=true;for(Item i:matches)selected.add(i.uri.toString());baseRoot();LinearLayout h=new LinearLayout(this);h.setGravity(Gravity.CENTER_VERTICAL);Button back=button("‹");back.setTextSize(28);back.setOnClickListener(v->onBackPressed());h.addView(back,new LinearLayout.LayoutParams(dp(54),-2));LinearLayout t=new LinearLayout(this);t.setOrientation(LinearLayout.VERTICAL);t.addView(tv("Möjliga personträffar",25,true));TextView sub=tv(matches.size()+" bilder • granska innan flytt",13,false);sub.setTextColor(ACC);t.addView(sub);h.addView(t,new LinearLayout.LayoutParams(0,-2,1));root.addView(h);shown.clear();shown.addAll(matches);grid=new GridView(this);grid.setNumColumns(3);grid.setHorizontalSpacing(dp(3));grid.setVerticalSpacing(dp(3));adapter=new SelectAdapter();grid.setAdapter(adapter);grid.setOnItemClickListener((p,v,pos,id)->toggle(shown.get(pos)));grid.setOnItemLongClickListener((p,v,pos,id)->{toggle(shown.get(pos));return true;});root.addView(grid,new LinearLayout.LayoutParams(-1,0,1));selectionBar=new LinearLayout(this);selectionBar.setGravity(Gravity.CENTER);Button move=button("📁 Flytta markerade");move.setOnClickListener(v->chooseMoveTarget());selectionBar.addView(move,new LinearLayout.LayoutParams(0,-2,1));Button allBtn=button("✓ Alla");allBtn.setOnClickListener(v->{selected.clear();for(Item i:shown)selected.add(i.uri.toString());selectionMode=true;updateSelection();});selectionBar.addView(allBtn,new LinearLayout.LayoutParams(0,-2,1));Button cancel=button("✕ Avmarkera");cancel.setOnClickListener(v->{selected.clear();selectionMode=false;updateSelection();});selectionBar.addView(cancel,new LinearLayout.LayoutParams(0,-2,1));root.addView(selectionBar);setContentView(root);updateSelection();
    }
}
