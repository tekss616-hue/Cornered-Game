package com.cornered.game;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.util.Patterns;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Space;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private final int bg=Color.rgb(10,11,14), panel=Color.rgb(21,23,28), field=Color.rgb(29,32,39);
    private final int text=Color.rgb(246,246,248), muted=Color.rgb(154,159,170), accent=Color.rgb(196,255,95);

    @Override public void onCreate(Bundle b){super.onCreate(b); Window w=getWindow(); w.setStatusBarColor(bg);w.setNavigationBarColor(bg);showLogin();}

    private void showLogin(){
        LinearLayout card=screen("مرحبًا بعودتك","ادخل حسابك وارجع للقصة.");
        EditText email=input("البريد الإلكتروني",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pass=input("كلمة المرور",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        card.addView(email,full(56)); gap(card,12); card.addView(pass,full(56)); gap(card,16);
        Button login=primary("تسجيل الدخول");card.addView(login,full(56)); gap(card,14);
        TextView forgot=label("نسيت كلمة المرور؟",14,accent,Typeface.BOLD);forgot.setGravity(Gravity.CENTER);card.addView(forgot,wrap());
        divider(card,"أو");
        Button google=secondary("G   المتابعة باستخدام Google");card.addView(google,full(56));gap(card,22);
        TextView create=label("ما عندك حساب؟  إنشاء حساب",15,text,Typeface.BOLD);create.setGravity(Gravity.CENTER);card.addView(create,wrap());
        create.setOnClickListener(v->showRegister());
        login.setOnClickListener(v->{if(!validEmail(email)||pass.getText().length()<6)toast("تأكد من البريد وكلمة المرور");else toast("واجهة الدخول جاهزة — الربط بالحسابات في خطوة الخادم");});
        google.setOnClickListener(v->showGoogleProfile());
        forgot.setOnClickListener(v->toast("استعادة كلمة المرور سنربطها مع نظام الحسابات"));
    }

    private void showRegister(){
        LinearLayout card=screen("أنشئ حسابك","أربع خانات فقط، وبعدها تكون جاهز.");
        EditText player=input("اسم اللاعب",InputType.TYPE_CLASS_TEXT);
        EditText user=input("اسم المستخدم  @username",InputType.TYPE_CLASS_TEXT);
        EditText email=input("البريد الإلكتروني",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pass=input("كلمة المرور",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        addFields(card,player,user,email,pass); gap(card,16);
        Button create=primary("إنشاء الحساب");card.addView(create,full(56));gap(card,22);
        TextView login=label("عندك حساب؟  تسجيل الدخول",15,text,Typeface.BOLD);login.setGravity(Gravity.CENTER);card.addView(login,wrap());
        login.setOnClickListener(v->showLogin());
        create.setOnClickListener(v->{
            if(player.getText().toString().trim().length()<2){toast("اكتب اسم اللاعب");return;}
            if(!validUsername(user.getText().toString())){toast("اسم المستخدم: 3–18 حرفًا أو رقمًا أو _");return;}
            if(!validEmail(email)){toast("اكتب بريدًا إلكترونيًا صحيحًا");return;}
            if(pass.getText().length()<8){toast("كلمة المرور لازم تكون 8 أحرف على الأقل");return;}
            toast("واجهة إنشاء الحساب جاهزة — التحقق الفعلي سنربطه بالخادم");
        });
    }

    private void showGoogleProfile(){
        LinearLayout card=screen("أكمل حساب Google","بعد اختيار حساب Google نحتاج منك شيئين فقط.");
        TextView note=label("Google يزوّدنا بالبريد والهوية بأمان. أنت تختار الاسم الذي يراه اللاعبون واسم المستخدم الفريد.",14,muted,Typeface.NORMAL);note.setLineSpacing(0,1.2f);card.addView(note,wrap());gap(card,18);
        EditText player=input("اسم اللاعب",InputType.TYPE_CLASS_TEXT);
        EditText user=input("اسم المستخدم  @username",InputType.TYPE_CLASS_TEXT);
        addFields(card,player,user);gap(card,16);
        Button done=primary("إكمال إنشاء الحساب");card.addView(done,full(56));gap(card,16);
        TextView back=label("رجوع لتسجيل الدخول",14,muted,Typeface.BOLD);back.setGravity(Gravity.CENTER);card.addView(back,wrap());
        back.setOnClickListener(v->showLogin());
        done.setOnClickListener(v->{if(player.getText().toString().trim().length()<2||!validUsername(user.getText().toString()))toast("تأكد من اسم اللاعب واسم المستخدم");else toast("واجهة Google جاهزة — اختيار حساب Google الحقيقي يحتاج إعداد OAuth");});
    }

    private LinearLayout screen(String title,String subtitle){
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout root=column();root.setPadding(dp(24),dp(42),dp(24),dp(32));root.setGravity(Gravity.CENTER_HORIZONTAL);scroll.addView(root,wrap());
        TextView mark=label("C",27,Color.BLACK,Typeface.BOLD);mark.setGravity(Gravity.CENTER);mark.setBackground(round(accent,20));root.addView(mark,new LinearLayout.LayoutParams(dp(60),dp(60)));gap(root,20);
        TextView t=label(title,29,text,Typeface.BOLD);t.setGravity(Gravity.CENTER);root.addView(t,wrap());gap(root,8);
        TextView s=label(subtitle,15,muted,Typeface.NORMAL);s.setGravity(Gravity.CENTER);root.addView(s,wrap());gap(root,30);
        LinearLayout card=column();card.setPadding(dp(18),dp(22),dp(18),dp(22));card.setBackground(round(panel,24));root.addView(card,wrap());
        gap(root,22);TextView footer=label("Cornered  •  الدفعة 1 من 20",12,Color.rgb(88,92,101),Typeface.NORMAL);footer.setGravity(Gravity.CENTER);root.addView(footer,wrap());
        setContentView(scroll);return card;
    }

    private EditText input(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(Color.rgb(112,117,128));e.setTextColor(text);e.setTextSize(16);e.setSingleLine(true);e.setInputType(type);e.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);e.setPadding(dp(16),0,dp(16),0);e.setBackground(round(field,15));return e;}
    private Button primary(String s){Button b=button(s);b.setTextColor(Color.BLACK);b.setBackground(round(accent,16));return b;}
    private Button secondary(String s){Button b=button(s);b.setTextColor(text);GradientDrawable d=round(field,16);d.setStroke(dp(1),Color.rgb(55,59,68));b.setBackground(d);return b;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);return b;}
    private void addFields(LinearLayout p,EditText... es){for(int i=0;i<es.length;i++){p.addView(es[i],full(56));if(i<es.length-1)gap(p,12);}}
    private void divider(LinearLayout p,String word){gap(p,20);LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER);View a=new View(this),b=new View(this);a.setBackgroundColor(Color.rgb(54,57,65));b.setBackgroundColor(Color.rgb(54,57,65));r.addView(a,new LinearLayout.LayoutParams(0,dp(1),1));TextView t=label(word,13,muted,Typeface.NORMAL);t.setGravity(Gravity.CENTER);LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(dp(50),dp(30));r.addView(t,tp);r.addView(b,new LinearLayout.LayoutParams(0,dp(1),1));p.addView(r,wrap());gap(p,12);}
    private boolean validEmail(EditText e){return Patterns.EMAIL_ADDRESS.matcher(e.getText().toString().trim()).matches();}
    private boolean validUsername(String s){return s.trim().matches("[A-Za-z0-9_]{3,18}");}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private TextView label(String s,int z,int c,int style){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,style);t.setTextDirection(View.TEXT_DIRECTION_RTL);return t;}
    private GradientDrawable round(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private void gap(LinearLayout p,int h){Space s=new Space(this);p.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    private LinearLayout.LayoutParams wrap(){return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);}
    private LinearLayout.LayoutParams full(int h){return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(h));}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}
    @Override public void onBackPressed(){showLogin();}
}
