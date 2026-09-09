package com.studio.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private SharedPreferences prefs;
    private final int BG = Color.rgb(9,11,15), CARD = Color.rgb(19,23,30), MUTED = Color.rgb(151,160,174), GREEN = Color.rgb(124,255,178);
    private final String[] styles = {"إعلانات الشركات","تجمعيات أنمي","TikTok","YouTube","رعب","أكشن","عام"};

    @Override public void onCreate(Bundle b) {
        super.onCreate(b); prefs=getSharedPreferences("studio",MODE_PRIVATE); showHome();
    }
    TextView text(String s,int sp,int color,boolean bold){ TextView v=new TextView(this);v.setText(s);v.setTextSize(sp);v.setTextColor(color);v.setGravity(Gravity.RIGHT);v.setTypeface(null,bold?Typeface.BOLD:Typeface.NORMAL);v.setPadding(4,8,4,8);return v; }
    GradientDrawable bg(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(radius);return d;}
    Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);b.setTextColor(Color.rgb(5,20,13));b.setTypeface(null,Typeface.BOLD);b.setBackground(bg(GREEN,28));return b;}
    void base(String title,String sub){
        ScrollView sc=new ScrollView(this);sc.setFillViewport(true);root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(28,35,28,35);root.setBackgroundColor(BG);sc.addView(root);setContentView(sc);
        TextView brand=text("STUDIO  /  AI",12,GREEN,true);brand.setLetterSpacing(.18f);root.addView(brand);
        root.addView(text(title,30,Color.WHITE,true));root.addView(text(sub,14,MUTED,false));
        Space gap=new Space(this);root.addView(gap,new LinearLayout.LayoutParams(1,24));
        content=new LinearLayout(this);content.setOrientation(LinearLayout.VERTICAL);root.addView(content);
    }
    void showHome(){
        base("الاستوديو","مساحة عملك الخاصة للمونتاج والتعلّم من الأساليب.");
        Button add=button("＋  إنشاء مشروع");add.setOnClickListener(v->showName());content.addView(add,new LinearLayout.LayoutParams(-1,120));
        TextView h=text("المشاريع الأخيرة",19,Color.WHITE,true);h.setPadding(0,35,0,12);content.addView(h);
        Set<String> projects=prefs.getStringSet("projects",new LinkedHashSet<>());
        if(projects.isEmpty()) content.addView(text("لا توجد مشاريع بعد. ابدأ أول مشروع من الزر بالأعلى.",14,MUTED,false));
        else for(String p:projects){String[] x=p.split("\\|",2); TextView c=text(x[0]+"\n"+(x.length>1?x[1]:"عام"),17,Color.WHITE,true);c.setBackground(bg(CARD,24));c.setPadding(22,20,22,20);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,0,0,14);content.addView(c,lp);c.setOnClickListener(v->showWorkspace(x[0],x.length>1?x[1]:"عام"));}
    }
    void showName(){
        base("مشروع جديد","أعط المشروع اسمًا واضحًا. سنفصل ذاكرته وملفاته عن بقية المشاريع.");
        EditText name=new EditText(this);name.setHint("مثال: إعلان مطعم العميل");name.setHintTextColor(MUTED);name.setTextColor(Color.WHITE);name.setTextSize(18);name.setSingleLine();name.setBackground(bg(CARD,24));name.setPadding(22,8,22,8);content.addView(name,new LinearLayout.LayoutParams(-1,110));
        Button next=button("التالي  ←");LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,110);lp.setMargins(0,24,0,0);content.addView(next,lp);next.setOnClickListener(v->{String n=name.getText().toString().trim();if(n.isEmpty()){name.setError("اكتب اسم المشروع");return;}showStyles(n);});
        back();
    }
    void showStyles(String name){
        base("اختر أسلوب المشروع","كل قسم سيكون له لاحقًا ذاكرته، أرشيفه، شاته، مؤثراته وانتقالاته الخاصة.");
        for(String s:styles){TextView c=text(s,18,Color.WHITE,true);c.setBackground(bg(CARD,24));c.setPadding(22,18,22,18);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,85);lp.setMargins(0,0,0,13);content.addView(c,lp);c.setOnClickListener(v->saveProject(name,s));} back();
    }
    void saveProject(String name,String style){Set<String> old=new LinkedHashSet<>(prefs.getStringSet("projects",new LinkedHashSet<>()));old.add(name+"|"+style);prefs.edit().putStringSet("projects",old).apply();showWorkspace(name,style);}
    void showWorkspace(String name,String style){
        base(name,style+"  •  مساحة مشروع مستقلة");
        String[] cards={"🎬  الفيديوهات\nمواد العميل والمراجع","💬  شات المشروع\nتواصل مع مساعد هذا القسم","🧠  الذاكرة\nقصيرة + طويلة للقسم","🗂  الأرشيف\nالتحليلات والأساليب المحفوظة","✨  المؤثرات والانتقالات\nمكتبة "+style};
        for(String s:cards){TextView c=text(s,17,Color.WHITE,true);c.setBackground(bg(CARD,24));c.setPadding(22,16,22,16);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,100);lp.setMargins(0,0,0,13);content.addView(c,lp);} back();
    }
    void back(){TextView b=text("‹  رجوع للرئيسية",15,GREEN,true);b.setPadding(0,30,0,20);root.addView(b);b.setOnClickListener(v->showHome());}
    @Override public void onBackPressed(){showHome();}
}
