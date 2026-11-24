package cl.evaluaapp;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.app.PendingIntent;
import android.app.AlarmManager;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    private static final String PREFS = "evalua_data";
    private static final String KEY = "exams";
    private static final String CHANNEL = "evalua_reminders";
    private final ArrayList<Exam> exams = new ArrayList<>();
    private LinearLayout listContainer;
    private TextView countText;
    private String filter = "Todas";

    static class Exam {
        String id, subject, details;
        long date;
        boolean done;
        Exam(String id, String subject, String details, long date, boolean done) {
            this.id=id; this.subject=subject; this.details=details; this.date=date; this.done=done;
        }
        JSONObject toJson() throws Exception {
            JSONObject o=new JSONObject(); o.put("id",id); o.put("subject",subject); o.put("details",details); o.put("date",date); o.put("done",done); return o;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(23,37,84));
        createChannel(); load(); buildUi(); requestNotificationPermission();
        for (Exam e: exams) if (!e.done) scheduleReminder(e);
    }

    private int dp(float v) { return (int)(v*getResources().getDisplayMetrics().density+0.5f); }
    private TextView text(String value, int size, int color, boolean bold) {
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(color);
        if (bold) t.setTypeface(null, Typeface.BOLD); return t;
    }
    private GradientDrawableBg bg(int color, int radius) { return new GradientDrawableBg(color, dp(radius)); }
    private void buildUi() {
        LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(Color.rgb(245,248,255));
        LinearLayout header=new LinearLayout(this); header.setOrientation(LinearLayout.VERTICAL); header.setPadding(dp(20),dp(22),dp(20),dp(20)); header.setBackground(bg(Color.rgb(23,37,84),0));
        TextView brand=text("📘  EvalúaApp",25,Color.WHITE,true); header.addView(brand);
        TextView subtitle=text("Tu calendario escolar inteligente",14,Color.rgb(219,234,254),false); LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2); sp.topMargin=dp(5); header.addView(subtitle,sp);
        countText=text("Organiza tus próximas evaluaciones",14,Color.WHITE,false); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.topMargin=dp(18); header.addView(countText,cp);
        root.addView(header);

        LinearLayout actions=new LinearLayout(this); actions.setPadding(dp(16),dp(14),dp(16),dp(8)); actions.setGravity(Gravity.CENTER_VERTICAL);
        Button add=button("+ Agregar evaluación",Color.rgb(37,99,235),Color.WHITE); actions.addView(add,new LinearLayout.LayoutParams(0,dp(48),1)); add.setOnClickListener(v->showAddDialog());
        root.addView(actions);
        LinearLayout filters=new LinearLayout(this); filters.setPadding(dp(16),dp(2),dp(16),dp(10));
        String[] choices={"Todas","Pendientes","Realizadas"};
        for (String c:choices) { Button b=button(c, c.equals(filter)?Color.rgb(219,234,254):Color.WHITE, Color.rgb(30,64,175)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(40),1); p.setMargins(dp(3),0,dp(3),0); filters.addView(b,p); b.setOnClickListener(v->{filter=c; refreshList(); buildFilterColors(filters,choices);}); }
        root.addView(filters);
        ScrollView scroll=new ScrollView(this); listContainer=new LinearLayout(this); listContainer.setOrientation(LinearLayout.VERTICAL); listContainer.setPadding(dp(16),dp(4),dp(16),dp(24)); scroll.addView(listContainer); root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root); refreshList();
    }
    private void buildFilterColors(LinearLayout row,String[] choices) {
        for(int i=0;i<row.getChildCount();i++){ Button b=(Button)row.getChildAt(i); boolean selected=choices[i].equals(filter); b.setBackground(bg(selected?Color.rgb(219,234,254):Color.WHITE,12)); }
    }
    private Button button(String label,int color,int fg) { Button b=new Button(this); b.setText(label); b.setTextColor(fg); b.setTextSize(13); b.setAllCaps(false); b.setTypeface(null,Typeface.BOLD); b.setBackground(bg(color,14)); if(Build.VERSION.SDK_INT>=21)b.setStateListAnimator(null); return b; }

    private void refreshList() {
        if(listContainer==null)return; listContainer.removeAllViews();
        int pending=0; for(Exam e:exams)if(!e.done)pending++;
        countText.setText(pending+" evaluación"+(pending==1?" pendiente":"es pendientes")+" por preparar");
        ArrayList<Exam> sorted=new ArrayList<>(exams); Collections.sort(sorted,(a,b)->Long.compare(a.date,b.date));
        int shown=0;
        for(Exam e:sorted){ if(filter.equals("Pendientes")&&e.done)continue; if(filter.equals("Realizadas")&&!e.done)continue; listContainer.addView(examCard(e)); shown++; }
        if(shown==0){ TextView empty=text("Todavía no hay evaluaciones aquí.\nPulsa ‘Agregar evaluación’ para comenzar.",16,Color.rgb(100,116,139),false); empty.setGravity(Gravity.CENTER); empty.setPadding(dp(18),dp(55),dp(18),dp(55)); listContainer.addView(empty); }
    }
    private View examCard(Exam e) {
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL); card.setPadding(dp(16),dp(14),dp(16),dp(12)); card.setBackground(bg(Color.WHITE,16)); LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,-2); cp.bottomMargin=dp(10); card.setLayoutParams(cp);
        LinearLayout top=new LinearLayout(this); top.setGravity(Gravity.CENTER_VERTICAL);
        TextView subject=text(e.subject,18,Color.rgb(30,41,59),true); top.addView(subject,new LinearLayout.LayoutParams(0,-2,1));
        TextView status=text(e.done?"REALIZADA":"PENDIENTE",11,e.done?Color.rgb(21,128,61):Color.rgb(29,78,216),true); top.addView(status); card.addView(top);
        String date=new SimpleDateFormat("EEEE d 'de' MMMM yyyy",new Locale("es","CL")).format(new Date(e.date));
        TextView d=text("📅  "+date,14,Color.rgb(51,65,85),false); LinearLayout.LayoutParams dp1=new LinearLayout.LayoutParams(-1,-2); dp1.topMargin=dp(8); card.addView(d,dp1);
        if(!e.details.trim().isEmpty()){ TextView details=text(e.details,14,Color.rgb(100,116,139),false); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(5);card.addView(details,p); }
        LinearLayout buttons=new LinearLayout(this); buttons.setGravity(Gravity.END); LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.topMargin=dp(10);card.addView(buttons,bp);
        Button done=button(e.done?"Deshacer":"Marcar realizada",Color.rgb(219,234,254),Color.rgb(30,64,175)); buttons.addView(done); done.setOnClickListener(v->{e.done=!e.done;save();refreshList();});
        Button delete=button("Eliminar",Color.rgb(254,226,226),Color.rgb(185,28,28)); LinearLayout.LayoutParams delp=new LinearLayout.LayoutParams(-2,dp(46));delp.leftMargin=dp(8);buttons.addView(delete,delp);delete.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Eliminar evaluación").setMessage("¿Quieres eliminar esta evaluación?").setNegativeButton("Cancelar",null).setPositiveButton("Eliminar",(a,b)->{cancelReminder(e);exams.remove(e);save();refreshList();}).show());
        return card;
    }

    private void showAddDialog() {
        LinearLayout form=new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL); form.setPadding(dp(20),dp(8),dp(20),0);
        EditText subject=new EditText(this);subject.setSingleLine(true);subject.setHint("Asignatura (ej. Matemática)");form.addView(subject);
        EditText details=new EditText(this);details.setHint("Contenidos o descripción");details.setMinLines(2);details.setGravity(Gravity.TOP);form.addView(details);
        Calendar selected=Calendar.getInstance(); selected.set(Calendar.HOUR_OF_DAY,9);selected.set(Calendar.MINUTE,0);selected.set(Calendar.SECOND,0);
        TextView dateLabel=text("Fecha: "+new SimpleDateFormat("dd/MM/yyyy",new Locale("es","CL")).format(selected.getTime()),15,Color.rgb(30,64,175),true);
        Button pick=button("Elegir fecha",Color.rgb(219,234,254),Color.rgb(30,64,175)); form.addView(pick);form.addView(dateLabel);
        pick.setOnClickListener(v->{Calendar now=Calendar.getInstance();new DatePickerDialog(this,(view,y,m,d)->{selected.set(y,m,d,9,0,0);dateLabel.setText("Fecha: "+new SimpleDateFormat("dd/MM/yyyy",new Locale("es","CL")).format(selected.getTime()));},selected.get(Calendar.YEAR),selected.get(Calendar.MONTH),selected.get(Calendar.DAY_OF_MONTH)).show();});
        new AlertDialog.Builder(this).setTitle("Nueva evaluación").setView(form).setNegativeButton("Cancelar",null).setPositiveButton("Guardar",(dialog,which)->{
            String s=subject.getText().toString().trim(); if(s.isEmpty()){Toast.makeText(this,"Escribe una asignatura",Toast.LENGTH_SHORT).show();return;}
            Exam e=new Exam(UUID.randomUUID().toString(),s,details.getText().toString().trim(),selected.getTimeInMillis(),false);exams.add(e);save();scheduleReminder(e);refreshList();Toast.makeText(this,"Evaluación guardada",Toast.LENGTH_SHORT).show();
        }).show();
    }

    private void load(){try{String raw=getSharedPreferences(PREFS,MODE_PRIVATE).getString(KEY,"[]");JSONArray a=new JSONArray(raw);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);exams.add(new Exam(o.getString("id"),o.getString("subject"),o.optString("details",""),o.getLong("date"),o.optBoolean("done",false)));}}catch(Exception ignored){}}
    private void save(){try{JSONArray a=new JSONArray();for(Exam e:exams)a.put(e.toJson());getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(KEY,a.toString()).apply();}catch(Exception ignored){}}
    private void createChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel(CHANNEL,"Recordatorios de evaluaciones",NotificationManager.IMPORTANCE_DEFAULT);c.setDescription("Avisos antes de las evaluaciones");getSystemService(NotificationManager.class).createNotificationChannel(c);}}
    private void requestNotificationPermission(){if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},20);}
    private void scheduleReminder(Exam e){long when=e.date-24L*60L*60L*1000L; if(when<System.currentTimeMillis())return;Intent i=new Intent(this,ReminderReceiver.class);i.putExtra("subject",e.subject);i.putExtra("details",e.details);i.putExtra("id",e.id);PendingIntent pi=PendingIntent.getBroadcast(this,e.id.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);if(am!=null)am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);}
    private void cancelReminder(Exam e){Intent i=new Intent(this,ReminderReceiver.class);PendingIntent pi=PendingIntent.getBroadcast(this,e.id.hashCode(),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);AlarmManager am=(AlarmManager)getSystemService(ALARM_SERVICE);if(am!=null)am.cancel(pi);}
}

class GradientDrawableBg extends android.graphics.drawable.GradientDrawable {
    GradientDrawableBg(int color,int radius){super();setColor(color);setCornerRadius(radius);}
}
