package cl.evaluaapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.os.Build;

public class ReminderReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        String subject=intent.getStringExtra("subject"); if(subject==null)subject="una evaluación";
        String details=intent.getStringExtra("details");
        String message="Mañana tienes evaluación de "+subject+".";
        if(details!=null&&!details.trim().isEmpty())message+=" Contenidos: "+details;
        Notification.Builder b;
        if(Build.VERSION.SDK_INT>=26)b=new Notification.Builder(context,"evalua_reminders");else b=new Notification.Builder(context);
        b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("Recordatorio de EvalúaApp").setContentText(message).setStyle(new Notification.BigTextStyle().bigText(message)).setAutoCancel(true);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);if(nm!=null)nm.notify(intent.getStringExtra("id")==null?1:intent.getStringExtra("id").hashCode(),b.build());
    }
}
