package cl.evaluaapp;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context, Intent intent) {
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Las fechas se conservan en el teléfono. Para volver a programar avisos,
            // abre EvalúaApp después de reiniciar el dispositivo.
        }
    }
}
