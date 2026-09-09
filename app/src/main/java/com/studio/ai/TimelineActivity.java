package com.studio.ai;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.*;

public class TimelineActivity extends Activity {
    SharedPreferences prefs; LinearLayout root,content; JSONObject video,timeline; String project,style;
    final int BG=Color.rgb(9,11,15),CARD=Color.rgb(19,23,30),MUTED=Color.rgb(151,160,174),GREEN=Color.rgb(124,255,178),DARK=Color.rgb(5,20,13);
    int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);}
    GradientDrawable bg(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    TextView tx(String s,float z,int c,boolean b){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",b?Typeface.BOLD:Typeface.NORMAL));v.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);v.setTextDirection(View.TEXT_DIRECTION_RTL);v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);v.setLineSpacing(0,1.12f);return v;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(14);b.setTextColor(DARK);b.setTypeface(null,Typeface.BOLD);b.setSingleLine(true);b.setMinWidth(0);b.setMinimumWidth(0);b.setPadding(dp(6),0,dp(6),0);b.setBackground(bg(GREEN,14));return b;}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);prefs=getSharedPreferences("studio",MODE_PRIVATE);project=getIntent().getStringExtra("project");style=getIntent().getStringExtra("style");
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        try{video=new JSONObject(getIntent().getStringExtra("video"));}catch(Exception e){finish();return;}
        timeline=EditTimelineStore.load(prefs,project,style,video);render();
    }

    void render(){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.setBackgroundColor(BG);
        root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);sc.addView(root,new ScrollView.LayoutParams(-1,-2));setContentView(sc);
        root.setOnApplyWindowInsetsListener((v,i)->{v.setPadding(dp(18),i.getSystemWindowInsetTop()+dp(14),dp(18),i.getSystemWindowInsetBottom()+dp(24));return i;});root.requestApplyInsets();
        TextView brand=tx("STUDIO / AI",12,GREEN,true);brand.setTextDirection(View.TEXT_DIRECTION_LTR);root.addView(brand);
        TextView title=tx("التايملاين القابل للتحرير",27,Color.WHITE,true);title.setPadding(0,dp(8),0,0);root.addView(title);
        TextView sub=tx(video.optString("name","فيديو")+" • "+format(EditTimelineStore.duration(timeline)),14,MUTED,false);sub.setPadding(0,dp(6),0,dp(12));root.addView(sub);
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);root.addView(content);
        card("تحرير حقيقي للخطة","القص والتقسيم والترتيب والسرعة والاستبعاد تُحفظ كخطة تحرير فعلية. ملف الفيديو الأصلي لا يتغير في هذه الدفعة؛ الرندر يأتي في الدفعة 14.");
        JSONArray a=timeline.optJSONArray("clips");for(int i=0;a!=null&&i<a.length();i++){JSONObject c=a.optJSONObject(i);if(c!=null)clipCard(i,c);}
        Button reset=button("إعادة بناء التايملاين من التحليل");LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,dp(54));rp.setMargins(0,dp(14),0,0);content.addView(reset,rp);reset.setOnClickListener(v->{EditTimelineStore.reset(prefs,project,style,video);timeline=EditTimelineStore.load(prefs,project,style,video);render();});
        Button close=button("رجوع للمشروع");LinearLayout.LayoutParams cp=new LinearLayout.LayoutParams(-1,dp(54));cp.setMargins(0,dp(10),0,0);content.addView(close,cp);close.setOnClickListener(v->finish());
    }

    void card(String a,String b){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackground(bg(CARD,18));c.addView(tx(a,17,Color.WHITE,true));TextView d=tx(b,13,MUTED,false);d.setPadding(0,dp(6),0,0);c.addView(d);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(10),0,0);content.addView(c,p);}

    void clipCard(int index,JSONObject c){
        LinearLayout box=new LinearLayout(this);box.setOrientation(LinearLayout.VERTICAL);box.setPadding(dp(16),dp(14),dp(16),dp(14));box.setBackground(bg(CARD,18));
        boolean enabled=c.optBoolean("enabled",true);long start=c.optLong("sourceStartMs"),end=c.optLong("sourceEndMs");double speed=c.optDouble("speed",1);
        box.addView(tx("لقطة "+(index+1)+(enabled?"":" • مستبعدة"),18,enabled?Color.WHITE:MUTED,true));
        TextView meta=tx(format(start)+" → "+format(end)+"   •   "+format(end-start)+"   •   "+speedText(speed),14,MUTED,false);meta.setPadding(0,dp(6),0,dp(4));box.addView(meta);
        String note=c.optString("note","");if(!note.isEmpty()){TextView nv=tx("ملاحظة: "+note,13,MUTED,false);nv.setPadding(0,dp(4),0,dp(4));box.addView(nv);}

        LinearLayout row1=new LinearLayout(this);row1.setOrientation(LinearLayout.HORIZONTAL);row1.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row1.setGravity(Gravity.CENTER_VERTICAL);
        Button toggle=button(enabled?"استبعاد":"استعادة"), split=button("تقسيم"), trim=button("قص الحدود"), speedB=button("السرعة");
        addAction(row1,toggle);addAction(row1,split);addAction(row1,trim);addAction(row1,speedB);box.addView(row1,new LinearLayout.LayoutParams(-1,dp(50)));

        LinearLayout row2=new LinearLayout(this);row2.setOrientation(LinearLayout.HORIZONTAL);row2.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);row2.setGravity(Gravity.CENTER_VERTICAL);
        Button noteB=button("ملاحظة"), up=button("↑ للأعلى"), down=button("↓ للأسفل");addAction(row2,noteB);addAction(row2,up);addAction(row2,down);
        LinearLayout.LayoutParams row2p=new LinearLayout.LayoutParams(-1,dp(50));row2p.setMargins(0,dp(8),0,0);box.addView(row2,row2p);

        toggle.setOnClickListener(v->{EditTimelineStore.setEnabled(timeline,index,!enabled);save();});
        split.setOnClickListener(v->splitDialog(index,start,end));trim.setOnClickListener(v->trimDialog(index,start,end));speedB.setOnClickListener(v->speedDialog(index));noteB.setOnClickListener(v->noteDialog(index,note));
        up.setOnClickListener(v->{if(EditTimelineStore.move(timeline,index,Math.max(0,index-1)))save();});
        down.setOnClickListener(v->{JSONArray clips=timeline.optJSONArray("clips");if(clips!=null&&EditTimelineStore.move(timeline,index,Math.min(clips.length()-1,index+1)))save();});
        if(index==0)up.setEnabled(false);JSONArray clips=timeline.optJSONArray("clips");if(clips!=null&&index==clips.length()-1)down.setEnabled(false);

        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(12),0,0);content.addView(box,p);
    }

    void addAction(LinearLayout row,Button b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(46),1);p.setMargins(dp(3),dp(2),dp(3),dp(2));row.addView(b,p);}
    String speedText(double s){return s==1.0?"سرعة 1×":"سرعة "+String.format(Locale.US,"%.1f×",s);}

    void splitDialog(int i,long s,long e){final EditText x=input(String.valueOf((s+e)/2000.0));new AlertDialog.Builder(this).setTitle("قسّم عند الثانية").setView(x).setPositiveButton("تقسيم",(d,w)->{try{long at=Math.round(Double.parseDouble(x.getText().toString())*1000);if(EditTimelineStore.split(timeline,i,at))save();else toast("نقطة التقسيم خارج اللقطة");}catch(Exception z){toast("قيمة غير صحيحة");}}).setNegativeButton("إلغاء",null).show();}
    void trimDialog(int i,long s,long e){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setPadding(dp(18),0,dp(18),0);EditText a=input(String.valueOf(s/1000.0)),b=input(String.valueOf(e/1000.0));a.setHint("البداية بالثواني");b.setHint("النهاية بالثواني");l.addView(a);l.addView(b);new AlertDialog.Builder(this).setTitle("قص حدود اللقطة").setView(l).setPositiveButton("حفظ",(d,w)->{try{if(EditTimelineStore.trim(timeline,i,Math.round(Double.parseDouble(a.getText().toString())*1000),Math.round(Double.parseDouble(b.getText().toString())*1000)))save();else toast("حدود القص غير صحيحة");}catch(Exception z){toast("قيمة غير صحيحة");}}).setNegativeButton("إلغاء",null).show();}
    void speedDialog(int i){String[] x={"0.5×","1×","1.5×","2×"};double[] v={.5,1,1.5,2};new AlertDialog.Builder(this).setTitle("سرعة اللقطة").setItems(x,(d,w)->{EditTimelineStore.setSpeed(timeline,i,v[w]);save();}).show();}
    void noteDialog(int i,String old){EditText x=input(old);new AlertDialog.Builder(this).setTitle("ملاحظة اللقطة").setView(x).setPositiveButton("حفظ",(d,w)->{EditTimelineStore.setNote(timeline,i,x.getText().toString());save();}).setNegativeButton("إلغاء",null).show();}
    EditText input(String s){EditText e=new EditText(this);e.setText(s);e.setTextColor(Color.BLACK);e.setSelectAllOnFocus(true);e.setSingleLine(true);return e;}
    void save(){EditTimelineStore.save(prefs,project,style,video,timeline);render();}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    String format(long ms){long sec=Math.max(0,ms)/1000;return String.format(Locale.US,"%d:%02d",sec/60,sec%60);}
}
