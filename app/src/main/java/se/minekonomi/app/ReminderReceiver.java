package se.minekonomi.app;
import android.app.*;import android.content.*;import android.os.Build;
public class ReminderReceiver extends BroadcastReceiver{
 @Override public void onReceive(Context c,Intent i){
  if(Build.VERSION.SDK_INT>=26){NotificationManager nm=(NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE);nm.createNotificationChannel(new NotificationChannel("economy","Ekonomipåminnelser",NotificationManager.IMPORTANCE_DEFAULT));}
  Intent open=new Intent(c,MainActivity.class);PendingIntent pi=PendingIntent.getActivity(c,0,open,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
  Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(c,"economy"):new Notification.Builder(c);b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(i.getStringExtra("title")).setContentText(i.getStringExtra("text")).setAutoCancel(true).setContentIntent(pi);
  ((NotificationManager)c.getSystemService(Context.NOTIFICATION_SERVICE)).notify((int)(System.currentTimeMillis()%100000),b.build());
 }
}
