package com.studio.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private SharedPreferences prefs;
    private String project = "", style = "";
    private Runnable backAction;

    private static final int PICK_VIDEO = 2001;
    private final int BG = Color.rgb(9,11,15);
    private final int CARD = Color.rgb(19,23,30);
    private final int MUTED = Color.rgb(151,160,174);
    private final int GREEN = Color.rgb(124,255,178);
    private final String[] styles = {"إعلانات الشركات","تجمعيات أنمي","TikTok","YouTube","رعب","أكشن","عام"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("studio", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        home();
    }

    private int dp(float x) { return Math.round(x * getResources().getDisplayMetrics().density); }

    private GradientDrawable bg(int c, float r) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(c);
        d.setCornerRadius(dp(r));
        return d;
    }

    private TextView tx(String s, float z, int c, boolean bold) {
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(z);
        v.setTextColor(c);
        v.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        v.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        v.setLineSpacing(0, 1.12f);
        return v;
    }

    private Button btn(String s) {
        Button b = new Button(this);
        b.setText(s);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTextColor(Color.rgb(5,20,13));
        b.setTypeface(null, Typeface.BOLD);
        b.setBackground(bg(GREEN,18));
        return b;
    }

    private void base(String title, String sub) {
        ScrollView sc = new ScrollView(this);
        sc.setFillViewport(true);
        sc.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setBackgroundColor(BG);
        sc.addView(root, new ScrollView.LayoutParams(-1,-2));
        setContentView(sc);
        root.setOnApplyWindowInsetsListener((v,i) -> {
            v.setPadding(dp(20), i.getSystemWindowInsetTop()+dp(14), dp(20), i.getSystemWindowInsetBottom()+dp(24));
            return i;
        });
        root.requestApplyInsets();

        TextView brand = tx("STUDIO  /  AI",12,GREEN,true);
        brand.setTextDirection(View.TEXT_DIRECTION_LTR);
        brand.setLetterSpacing(.16f);
        root.addView(brand);
        TextView t = tx(title,29,Color.WHITE,true);
        LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(-1,-2);
        tl.setMargins(0,dp(8),0,dp(4));
        root.addView(t,tl);
        TextView s = tx(sub,14,MUTED,false);
        LinearLayout.LayoutParams sl = new LinearLayout.LayoutParams(-1,-2);
        sl.setMargins(0,0,0,dp(22));
        root.addView(s,sl);
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.addView(content);
    }

    private void back(String label, Runnable r) {
        TextView b = tx("‹  " + label,15,GREEN,true);
        b.setPadding(0,dp(22),0,dp(18));
        root.addView(b);
        b.setOnClickListener(v -> r.run());
        backAction = r;
    }

    private void home() {
        project=""; style=""; backAction=null;
        base("الاستوديو","مساحة عملك الخاصة للمونتاج والتعلّم من الأساليب.");
        Button add=btn("＋  إنشاء مشروع");
        content.addView(add,new LinearLayout.LayoutParams(-1,dp(58)));
        add.setOnClickListener(v->newProject());
        TextView h=tx("المشاريع الأخيرة",18,Color.WHITE,true);
        LinearLayout.LayoutParams hl=new LinearLayout.LayoutParams(-1,-2);
        hl.setMargins(0,dp(28),0,dp(12));
        content.addView(h,hl);
        Set<String> ps=prefs.getStringSet("projects",new LinkedHashSet<>());
        if(ps.isEmpty()) content.addView(tx("لا توجد مشاريع بعد.",14,MUTED,false));
        else for(String p:ps){
            String[] x=p.split("\\|",2);
            String n=x[0], s=x.length>1?x[1]:"عام";
            card(n,s,()->workspace(n,s));
        }
    }

    private void newProject() {
        base("مشروع جديد","اكتب اسم المشروع ثم اختر قسمه.");
        EditText e=new EditText(this);
        e.setHint("مثال: إعلان مطعم العميل");
        e.setHintTextColor(MUTED); e.setTextColor(Color.WHITE);
        e.setTextDirection(View.TEXT_DIRECTION_RTL);
        e.setGravity(Gravity.END|Gravity.CENTER_VERTICAL);
        e.setBackground(bg(CARD,18)); e.setPadding(dp(18),0,dp(18),0);
        content.addView(e,new LinearLayout.LayoutParams(-1,dp(58)));
        Button n=btn("التالي  ←");
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(56));
        lp.setMargins(0,dp(18),0,0); content.addView(n,lp);
        n.setOnClickListener(v->{String name=e.getText().toString().trim();if(name.isEmpty()){e.setError("اكتب اسم المشروع");return;}chooseStyle(name);});
        back("رجوع للرئيسية",this::home);
    }

    private void chooseStyle(String name) {
        base("اختر أسلوب المشروع","كل قسم مستقل بمواده وتحليلاته.");
        for(String s:styles) card(s,"",()->saveProject(name,s));
        back("رجوع للرئيسية",this::home);
    }

    private void saveProject(String name,String s) {
        Set<String> p=new LinkedHashSet<>(prefs.getStringSet("projects",new LinkedHashSet<>()));
        p.add(name+"|"+s); prefs.edit().putStringSet("projects",p).apply(); workspace(name,s);
    }

    private void card(String title,String sub,Runnable r) {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(bg(CARD,18)); c.setPadding(dp(18),dp(13),dp(18),dp(13)); c.setMinimumHeight(dp(66));
        c.addView(tx(title,17,Color.WHITE,true)); if(!sub.isEmpty())c.addView(tx(sub,13,MUTED,false));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(10)); content.addView(c,lp);
        if(r!=null)c.setOnClickListener(v->r.run());
    }

    private void workspace(String n,String s) {
        project=n; style=s;
        base(n,s+"  •  مساحة مشروع مستقلة");
        card("🎬  الفيديوهات","استيراد وتشغيل الفيديو",()->videos(n,s));
        card("🧪  التحليل البصري والزمني","الحركة • تغير الإطارات • مرشحات القص • Timeline",()->analysisList(n,s));
        card("🗂  الأرشيف","مواد المشروع ونتائج التحليل",()->archive(n,s));
        disabled("💬  شات المشروع","موعده في دفعة الشات");
        disabled("🧠  الذاكرة","موعدها في دفعات الذاكرة");
        disabled("✨  المؤثرات والانتقالات","موعد المكتبة في دفعتها");
        back("رجوع للرئيسية",this::home);
    }

    private void disabled(String a,String b) {
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL);
        c.setBackground(bg(Color.rgb(15,18,23),18)); c.setPadding(dp(18),dp(13),dp(18),dp(13)); c.setAlpha(.6f);
        c.addView(tx(a,17,Color.WHITE,true)); c.addView(tx(b,13,MUTED,false));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(10)); content.addView(c,lp);
    }

    private String key(String n,String s){return "videos_"+Integer.toHexString((n+"|"+s).hashCode());}
    private JSONArray list(String n,String s){try{return new JSONArray(prefs.getString(key(n,s),"[]"));}catch(Exception e){return new JSONArray();}}
    private void save(String n,String s,JSONArray a){prefs.edit().putString(key(n,s),a.toString()).apply();}

    private void videos(String n,String s) {
        project=n; style=s; base("فيديوهات المشروع",n+"  •  "+s);
        Button add=btn("＋  استيراد فيديو من الجوال"); content.addView(add,new LinearLayout.LayoutParams(-1,dp(58))); add.setOnClickListener(v->pick());
        TextView note=tx("بعد الاستيراد، التحليل يعمل محليًا على الفيديو ويقيس التغير والحركة ويستخرج مرشحات القص.",13,MUTED,false);
        LinearLayout.LayoutParams nl=new LinearLayout.LayoutParams(-1,-2); nl.setMargins(0,dp(12),0,dp(18)); content.addView(note,nl);
        JSONArray a=list(n,s);
        if(a.length()==0) content.addView(tx("لا توجد فيديوهات حتى الآن.",14,MUTED,false));
        else for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);if(o!=null)videoCard(o,n,s);}
        back("رجوع للمشروع",()->workspace(n,s));
    }

    private void videoCard(JSONObject o,String n,String s) {
        String name=o.optString("name","فيديو"), uri=o.optString("uri",""); JSONObject in=o.optJSONObject("inspection");
        String status="لم يتم التحليل";
        if(in!=null){JSONArray cuts=in.optJSONArray("cutCandidates");status="تحليل v"+in.optInt("analysisVersion",3)+"  •  "+in.optInt("width")+"×"+in.optInt("height")+"  •  "+(cuts==null?0:cuts.length())+" مرشح قص";}
        LinearLayout c=new LinearLayout(this); c.setOrientation(LinearLayout.VERTICAL); c.setBackground(bg(CARD,18)); c.setPadding(dp(16),dp(13),dp(16),dp(13));
        c.addView(tx("🎞  "+name,16,Color.WHITE,true)); c.addView(tx(status,12.5f,in==null?MUTED:GREEN,true));
        c.setOnClickListener(v->player(name,uri,n,s)); LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2); lp.setMargins(0,0,0,dp(10)); content.addView(c,lp);
    }

    private void analysisList(String n,String s) {
        base("التحليل البصري والزمني",n+"  •  قياسات فعلية من إطارات الفيديو");
        JSONArray a=list(n,s);
        if(a.length()==0) content.addView(tx("أضف فيديو أولًا من قسم الفيديوهات.",14,MUTED,false));
        for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);if(o==null)continue;JSONObject in=o.optJSONObject("inspection");card(o.optString("name","فيديو"),in==null?"لم يُحلل بعد":"افتح Timeline ومرشحات القص",in==null?null:()->analysisDetail(o,n,s));}
        back("رجوع للمشروع",()->workspace(n,s));
    }

    private void analysisDetail(JSONObject o,String n,String s) {
        JSONObject x=o.optJSONObject("inspection"); base("نتيجة التحليل",o.optString("name","فيديو"));
        if(x==null){content.addView(tx("لا توجد نتيجة.",14,MUTED,false));back("رجوع",()->analysisList(n,s));return;}

        JSONArray cuts=x.optJSONArray("cutCandidates"), seg=x.optJSONArray("segments"), timeline=x.optJSONArray("visualTimeline");
        String info="المدة: "+format(x.optLong("durationMs"))+"\nالأبعاد: "+x.optInt("width")+" × "+x.optInt("height")+"\nالدوران: "+x.optInt("rotation")+"°\nمتوسط الحركة: "+x.optDouble("averageMotion",0)+"/100\nمتوسط التغير: "+x.optDouble("averageChange",0)+"/100\nحد ترشيح القص: "+x.optDouble("cutThreshold",0)+"\nفاصل القياس: "+x.optLong("analysisStepMs",0)+"ms\nمرشحات القص: "+(cuts==null?0:cuts.length())+"\nالمقاطع الناتجة حسابيًا: "+(seg==null?0:seg.length());
        TextView box=tx(info,14.5f,Color.WHITE,false); box.setBackground(bg(CARD,18)); box.setPadding(dp(18),dp(15),dp(18),dp(15)); content.addView(box);

        Button again=btn("↻  إعادة التحليل بهذا المحرك"); LinearLayout.LayoutParams al=new LinearLayout.LayoutParams(-1,dp(54)); al.setMargins(0,dp(14),0,dp(18)); content.addView(again,al);
        again.setOnClickListener(v->reanalyze(o,n,s));

        section("مرشحات القص", (cuts==null?0:cuts.length())+" نقطة تجاوزت حد التغير");
        if(cuts==null||cuts.length()==0) content.addView(tx("لم يرصد المحرك تغيّرًا قويًا بما يكفي لاعتباره مرشح قص.",13,MUTED,false));
        else for(int i=0;i<cuts.length();i++){
            JSONObject q=cuts.optJSONObject(i); if(q==null)continue;
            String conf=q.optString("confidence","low"); String ar="high".equals(conf)?"عالية":"medium".equals(conf)?"متوسطة":"منخفضة";
            card(format(q.optLong("timeMs")),"درجة التغير "+q.optDouble("score")+"/100  •  ثقة "+ar,null);
        }

        section("المقاطع الحسابية", (seg==null?0:seg.length())+" مقطع بين مرشحات القص");
        if(seg!=null) for(int i=0;i<seg.length();i++){
            JSONObject q=seg.optJSONObject(i); if(q==null)continue;
            card("مقطع "+(i+1),format(q.optLong("startMs"))+" ← "+format(q.optLong("endMs"))+"  •  "+format(q.optLong("durationMs")),null);
        }

        section("Timeline القياسات", (timeline==null?0:timeline.length())+" نقطة زمنية");
        if(timeline!=null){int stride=Math.max(1,timeline.length()/35);for(int i=0;i<timeline.length();i+=stride){JSONObject q=timeline.optJSONObject(i);if(q!=null)card(format(q.optLong("timeMs")),"حركة "+q.optDouble("motion")+"  •  تغير "+q.optDouble("change")+"  •  سطوع "+q.optInt("brightness"),null);}}

        TextView note=tx("مرشح القص ليس ادعاءً بأن النقطة انتقال مؤكد؛ هو اكتشاف حسابي مبني على اختلاف الإطارات والسطوع والتوزيع اللوني. فهم نوع الانتقال وأسلوب المونتاج يأتي في الدفعة الخامسة.",13,MUTED,false);
        LinearLayout.LayoutParams np=new LinearLayout.LayoutParams(-1,-2);np.setMargins(0,dp(14),0,0);content.addView(note,np);
        back("رجوع للتحليل",()->analysisList(n,s));
    }

    private void section(String title,String sub){TextView h=tx(title,18,Color.WHITE,true);LinearLayout.LayoutParams hp=new LinearLayout.LayoutParams(-1,-2);hp.setMargins(0,dp(22),0,dp(2));content.addView(h,hp);TextView s=tx(sub,12.5f,MUTED,false);LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(-1,-2);sp.setMargins(0,0,0,dp(10));content.addView(s,sp);}

    private void archive(String n,String s){base("أرشيف المشروع","الفيديوهات ونتائج التحليل المحفوظة داخل "+n);JSONArray a=list(n,s);if(a.length()==0)content.addView(tx("الأرشيف فارغ.",14,MUTED,false));else for(int i=a.length()-1;i>=0;i--){JSONObject o=a.optJSONObject(i);if(o!=null)videoCard(o,n,s);}back("رجوع للمشروع",()->workspace(n,s));}

    private void pick(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("video/*");i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);startActivityForResult(i,PICK_VIDEO);}

    @Override protected void onActivityResult(int rc,int result,Intent data){
        super.onActivityResult(rc,result,data);
        if(rc!=PICK_VIDEO||result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
        JSONArray a=list(project,style);
        for(int i=0;i<a.length();i++){JSONObject old=a.optJSONObject(i);if(old!=null&&uri.toString().equals(old.optString("uri"))){Toast.makeText(this,"الفيديو موجود بالفعل",Toast.LENGTH_SHORT).show();videos(project,style);return;}}
        JSONObject o=new JSONObject();
        try{o.put("name",displayName(uri));o.put("uri",uri.toString());o.put("added",System.currentTimeMillis());a.put(o);save(project,style,a);}catch(Exception e){Toast.makeText(this,"تعذر حفظ الفيديو",Toast.LENGTH_LONG).show();return;}
        analyzeAsync(uri,o,project,style);
    }

    private void analyzeAsync(Uri uri,JSONObject target,String n,String s){
        Toast.makeText(this,"جارٍ التحليل البصري والزمني…",Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try{
                JSONObject inspection=VideoInspector.inspect(this,uri);
                target.put("inspection",inspection);
                JSONArray latest=list(n,s);
                String u=target.optString("uri");
                for(int i=0;i<latest.length();i++){JSONObject item=latest.optJSONObject(i);if(item!=null&&u.equals(item.optString("uri"))){item.put("inspection",inspection);break;}}
                save(n,s,latest);
                runOnUiThread(()->{Toast.makeText(this,"اكتمل التحليل",Toast.LENGTH_SHORT).show();videos(n,s);});
            }catch(Exception e){runOnUiThread(()->{Toast.makeText(this,"تم حفظ الفيديو لكن تعذر تحليله",Toast.LENGTH_LONG).show();videos(n,s);});}
        }).start();
    }

    private void reanalyze(JSONObject o,String n,String s){String uri=o.optString("uri","");if(uri.isEmpty())return;analyzeAsync(Uri.parse(uri),o,n,s);}

    private String displayName(Uri u){Cursor c=null;try{c=getContentResolver().query(u,new String[]{OpenableColumns.DISPLAY_NAME},null,null,null);if(c!=null&&c.moveToFirst()){int x=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(x>=0)return c.getString(x);}}catch(Exception ignored){}finally{if(c!=null)c.close();}return "فيديو";}

    private void player(String file,String uri,String n,String s){
        base("مشغل الفيديو",file);
        FrameLayout f=new FrameLayout(this);f.setBackground(bg(Color.BLACK,18));VideoView vv=new VideoView(this);f.addView(vv,new FrameLayout.LayoutParams(-1,dp(230)));content.addView(f,new LinearLayout.LayoutParams(-1,dp(238)));
        MediaController mc=new MediaController(this);mc.setAnchorView(vv);vv.setMediaController(mc);vv.setVideoURI(Uri.parse(uri));vv.setOnPreparedListener(mp->{mp.setLooping(false);vv.start();});vv.setOnErrorListener((mp,w,e)->{Toast.makeText(this,"تعذر تشغيل الملف",Toast.LENGTH_LONG).show();return true;});
        back("رجوع للفيديوهات",()->videos(n,s));
    }

    private String format(long ms){long total=Math.max(0,ms)/1000;long h=total/3600,m=(total%3600)/60,sec=total%60;return h>0?String.format(Locale.US,"%d:%02d:%02d",h,m,sec):String.format(Locale.US,"%02d:%02d",m,sec);}

    @Override public void onBackPressed(){if(backAction!=null)backAction.run();else home();}
}
