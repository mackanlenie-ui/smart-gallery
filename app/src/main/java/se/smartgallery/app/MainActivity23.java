package se.smartgallery.app;

import android.app.*;
import android.content.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.*;
import java.util.*;

public class MainActivity23 extends MainActivity22 {
    static final int REQ_GOOGLE_PHOTOS = 2301;

    @Override void smartHub() {
        String[] a={
                "☁️ Google Photos (test)",
                "👥 Personer 8.0 TEST",
                "🖼 Bildvisare 2.0",
                "✨ Liknande bilder 2.0",
                "⧉ Riktiga dubbletter (SHA-256)",
                "★ Favoriter",
                "📱 S23 Ultra",
                "🗑 Papperskorg",
                "ℹ Bildinformation"
        };
        new AlertDialog.Builder(this).setTitle("✨ Smart Gallery 2.3 TEST")
                .setItems(a,(d,w)->{
                    if(w==0) pickGooglePhotos();
                    else if(w==1) peoplePage();
                    else if(w==2) Toast.makeText(this,"Tryck på en bild för Smart Gallery-visaren",Toast.LENGTH_LONG).show();
                    else if(w==3) findSimilar22();
                    else if(w==4) findTrueDuplicates17();
                    else if(w==5){filter="Favoriter";showMain();applyFilter();}
                    else if(w==6) s23Page17();
                    else if(w==7) trashSelected17();
                    else imageInfoHelp();
                }).setNegativeButton("Stäng",null).show();
    }

    @Override void peoplePage() {
        String[] a={
                "☁️ Välj bilder från Google Photos",
                "🖼 Personalbum",
                "➕ Skapa personalbum från markerade",
                "👤 Hitta samma person",
                "🧠 Bygg om personindex",
                "🧪 Diagnostik",
                "🔒 Om Google Photos-testet"
        };
        new AlertDialog.Builder(this).setTitle("👥 Personer 8.0 TEST")
                .setItems(a,(d,w)->{
                    if(w==0) pickGooglePhotos();
                    else if(w==1) showPersonAlbums21();
                    else if(w==2) createPersonAlbum21();
                    else if(w==3) search20();
                    else if(w==4) index20(null,true);
                    else if(w==5) diagnose20();
                    else new AlertDialog.Builder(this)
                            .setTitle("☁️ Google Photos – test")
                            .setMessage("Du väljer själv vilka bilder Smart Gallery får läsa via Androids bildväljare. Ansiktsanalysen sker lokalt på telefonen. Testversionen försöker behålla läsåtkomst till valda bilder, men Google Photos kan kräva att vissa molnbilder väljs igen senare.")
                            .setPositiveButton("OK",null).show();
                }).setNegativeButton("Stäng",null).show();
    }

    void pickGooglePhotos() {
        try {
            Intent i;
            if(Build.VERSION.SDK_INT>=33){
                i=new Intent(MediaStore.ACTION_PICK_IMAGES);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                try{i.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX,Math.min(100,MediaStore.getPickImagesMaxLimit()));}catch(Exception ignored){}
            }else{
                i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
                i.setType("image/*");
                i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
                i.addCategory(Intent.CATEGORY_OPENABLE);
            }
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i,REQ_GOOGLE_PHOTOS);
        }catch(Exception e){
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.setType("image/*");
            i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            startActivityForResult(i,REQ_GOOGLE_PHOTOS);
        }
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=REQ_GOOGLE_PHOTOS || resultCode!=RESULT_OK || data==null)return;
        LinkedHashSet<Uri> uris=new LinkedHashSet<>();
        if(data.getData()!=null)uris.add(data.getData());
        if(data.getClipData()!=null){
            for(int x=0;x<data.getClipData().getItemCount();x++)uris.add(data.getClipData().getItemAt(x).getUri());
        }
        int added=0;
        HashSet<String> existing=new HashSet<>();
        for(Item item:all)existing.add(item.uri.toString());
        for(Uri u:uris){
            if(existing.contains(u.toString()))continue;
            try{
                try{getContentResolver().takePersistableUriPermission(u,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
                String name="Google Photos";
                long size=0;
                try(Cursor c=getContentResolver().query(u,new String[]{OpenableColumns.DISPLAY_NAME,OpenableColumns.SIZE},null,null,null)){
                    if(c!=null&&c.moveToFirst()){
                        int ni=c.getColumnIndex(OpenableColumns.DISPLAY_NAME),si=c.getColumnIndex(OpenableColumns.SIZE);
                        if(ni>=0&&c.getString(ni)!=null)name=c.getString(ni);
                        if(si>=0&&!c.isNull(si))size=c.getLong(si);
                    }
                }
                String mime=getContentResolver().getType(u);
                if(mime==null)mime="image/*";
                all.add(0,new Item(-Math.abs((long)u.toString().hashCode()),System.currentTimeMillis()/1000,size,name,"Google Photos/TEST/",mime,u));
                existing.add(u.toString());
                added++;
            }catch(Exception ignored){}
        }
        if(screen==0){updateHero();applyFilter();}
        final int count=added;
        new AlertDialog.Builder(this)
                .setTitle("☁️ Google Photos")
                .setMessage(count+" bilder lades till i testbiblioteket.\n\nVill du bygga personindex nu så Smart Gallery kan leta efter samma person bland bilderna?")
                .setPositiveButton("Bygg personindex",(d,w)->index20(null,true))
                .setNegativeButton("Senare",null)
                .show();
    }
}
