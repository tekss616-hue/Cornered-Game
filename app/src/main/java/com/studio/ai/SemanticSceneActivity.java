package com.studio.ai;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.util.Locale;

public class SemanticSceneActivity extends Activity {
    private SharedPreferences prefs;
    private String project, style;
    private JSONObject video;
    private LinearLayout content;
    private ProgressBar progress;
    private TextView status;
    private final int BG=Color.rgb(9,11,15), CARD=Color.rgb(19,23,30), MUTED=Color.rgb(151,160,174), GREEN=Color.rgb(124,255,178);

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(BG);getWindow().setNavigationBarColor(BG);
        prefs=getSharedPreferences("studio",MODE_PRIVATE);
        project=getIntent().getStringExtra("project");style=getIntent().getStringExtra("style");
        try{video=new JSONObject(getIntent().getStringExtra("video"));}catch(Exception e){video=new JSONObject();}
        render();
    }

    private int dp(float x){return Math.round(x*getResources().getDisplayMetrics().density);} private GradientDrawable bg(int c,float r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private TextView tx(String s,float z,int c,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setTypeface(Typeface.create("sans",bold?Typeface.BOLD:Typeface.NORMAL));v.setGravity(Gravity.END);v.setTextDirection(View.TEXT_DIRECTION_RTL);v.setPadding(0,dp(4),0,dp(4));return v;}
    private Button btn(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(15);b.setTextColor(Color.rgb(5,20,13));b.setTypeface(null,Typeface.BOLD);b.setBackground(bg(GREEN,16));return b;}
    private void card(String title,String body){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(13),dp(16),dp(13));c.setBackground(bg(CARD,16));c.addView(tx(title,16,Color.WHITE,true));if(body!=null&&!body.isEmpty())c.addView(tx(body,13,MUTED,false));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(10));content.addView(c,p);}

    private void render(){
        ScrollView sc=new ScrollView(this);sc.setBackgroundColor(BG);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(dp(20),dp(18),dp(20),dp(24));root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);sc.addView(root,new ScrollView.LayoutParams(-1,-2));setContentView(sc);
        TextView brand=tx("STUDIO  /  AI",12,GREEN,true);brand.setTextDirection(View.TEXT_DIRECTION_LTR);brand.setGravity(Gravity.START);root.addView(brand);
        root.addView(tx("فهم محتوى المشاهد",28,Color.WHITE,true));root.addView(tx(video.optString("name","فيديو")+" • "+style,14,MUTED,false));
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);root.addView(content,new LinearLayout.LayoutParams(-1,-2));

        JSONObject inspection=video.optJSONObject("inspection");
        if(inspection==null){card("غير جاهز","حلل الفيديو محليًا أولًا حتى نستخرج المشاهد.");return;}
        JSONObject semantic=inspection.optJSONObject("semanticUnderstanding");
        card("الدفعة 17","هذه الصفحة تستخدم فريمات حقيقية من كل مشهد وترسلها إلى نموذج رؤية سحابي. لا يتم اختراع وصف من الحركة أو السطوع فقط.");
        if(AiEndpoints.SEMANTIC_SCENE_ENDPOINT==null||AiEndpoints.SEMANTIC_SCENE_ENDPOINT.isEmpty()) card("الربط السحابي غير مفعل بعد","الكود جاهز، لكن نحتاج رابط Netlify بعد ربط المستودع ووضع المفتاح. لن يتم إرسال أي طلب مدفوع الآن.");
        if(semantic!=null) showSemantic(semantic); else card("لا يوجد فهم دلالي محفوظ","اضغط التحليل السحابي بعد إكمال الربط. النتائج تُحفظ محليًا ولا يُعاد الدفع عند فتح الصفحة.");

        progress=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);progress.setMax(100);progress.setVisibility(View.GONE);content.addView(progress,new LinearLayout.LayoutParams(-1,dp(10)));
        status=tx("",13,MUTED,true);content.addView(status);
        Button analyze=btn(semantic==null?"👁 تحليل محتوى المشاهد الحقيقي":"↻ إعادة التحليل السحابي");content.addView(analyze,new LinearLayout.LayoutParams(-1,dp(54)));
        analyze.setOnClickListener(v->{
            if(AiEndpoints.SEMANTIC_SCENE_ENDPOINT==null||AiEndpoints.SEMANTIC_SCENE_ENDPOINT.isEmpty()){Toast.makeText(this,"نحتاج ربط Netlify أولًا",Toast.LENGTH_LONG).show();return;}
            new AlertDialog.Builder(this).setTitle("تحليل سحابي مدفوع").setMessage("سيتم إرسال 1–2 إطار من كل مشهد إلى OpenAI، بحد أقصى 12 مشهدًا في هذه الدفعة. النتائج المحفوظة لا تُعاد تلقائيًا.").setNegativeButton("إلغاء",null).setPositiveButton("ابدأ",(d,w)->runAnalysis(inspection)).show();
        });
    }

    private void showSemantic(JSONObject semantic){
        JSONArray scenes=semantic.optJSONArray("scenes");
        card("النتيجة المحفوظة","المشاهد المفهومة: "+semantic.optInt("analyzedSceneCount")+" من "+semantic.optInt("sourceSceneCount")+(semantic.optBoolean("capped")?" • تم تطبيق حد التكلفة":""));
        for(int i=0;scenes!=null&&i<scenes.length();i++){
            JSONObject s=scenes.optJSONObject(i);if(s==null)continue;
            StringBuilder body=new StringBuilder();
            body.append(time(s.optLong("startMs"))).append(" – ").append(time(s.optLong("endMs"))).append("\n");
            body.append(s.optString("summary","بدون وصف"));
            if(!s.optString("shotType","").isEmpty())body.append("\nنوع اللقطة: ").append(s.optString("shotType"));
            JSONArray actions=s.optJSONArray("actions");if(actions!=null&&actions.length()>0)body.append("\nالحركة: ").append(join(actions));
            JSONArray text=s.optJSONArray("visibleText");if(text!=null&&text.length()>0)body.append("\nنص ظاهر: ").append(join(text));
            JSONArray notes=s.optJSONArray("editingNotes");if(notes!=null&&notes.length()>0)body.append("\nملاحظات مونتاج: ").append(join(notes));
            body.append("\nأهمية: ").append(s.optInt("importance")).append("/100 • بداية: ").append(s.optInt("openingPotential")).append("/100");
            card("مشهد "+(i+1),body.toString());
        }
    }

    private void runAnalysis(JSONObject inspection){
        progress.setVisibility(View.VISIBLE);progress.setProgress(0);status.setText("بدء فهم المشاهد…");
        new Thread(()->{
            try{
                Uri uri=Uri.parse(video.optString("uri"));
                JSONObject result=SemanticSceneAnalyzer.analyze(this,uri,inspection,project,style,(percent,stage)->runOnUiThread(()->{progress.setProgress(percent);status.setText(stage+" • "+percent+"%");}));
                inspection.put("semanticUnderstanding",result);video.put("inspection",inspection);saveBack();
                runOnUiThread(()->{Toast.makeText(this,"اكتمل فهم المشاهد الحقيقي",Toast.LENGTH_LONG).show();render();});
            }catch(Exception e){runOnUiThread(()->{progress.setVisibility(View.GONE);status.setText("تعذر التحليل: "+(e.getMessage()==null?"خطأ":e.getMessage()));});}
        }).start();
    }

    private void saveBack() throws Exception{
        String key="videos_"+Integer.toHexString((project+"|"+style).hashCode());JSONArray a;
        try{a=new JSONArray(prefs.getString(key,"[]"));}catch(Exception e){a=new JSONArray();}
        String uri=video.optString("uri");
        for(int i=0;i<a.length();i++){JSONObject v=a.optJSONObject(i);if(v!=null&&uri.equals(v.optString("uri"))){v.put("inspection",video.optJSONObject("inspection"));break;}}
        prefs.edit().putString(key,a.toString()).apply();
    }
    private String join(JSONArray a){StringBuilder b=new StringBuilder();for(int i=0;i<a.length();i++){if(i>0)b.append("، ");b.append(a.optString(i));}return b.toString();}
    private String time(long ms){long sec=Math.max(0,ms)/1000;return String.format(Locale.US,"%d:%02d",sec/60,sec%60);}
}
