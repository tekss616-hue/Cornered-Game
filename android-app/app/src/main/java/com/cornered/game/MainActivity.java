package com.cornered.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final int RC_GOOGLE=3117;
    private final int bg=Color.rgb(10,11,14), panel=Color.rgb(21,23,28), field=Color.rgb(29,32,39);
    private final int text=Color.rgb(246,246,248), muted=Color.rgb(154,159,170), accent=Color.rgb(196,255,95);
    private FirebaseAuth auth;
    private GoogleSignInClient googleClient;
    private final ExecutorService io=Executors.newCachedThreadPool();
    private SharedPreferences prefs;
    private String pendingGoogleName="";
    private String currentSection="home";

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        Window w=getWindow();w.setStatusBarColor(bg);w.setNavigationBarColor(bg);
        prefs=getSharedPreferences("cornered_fast_cache",MODE_PRIVATE);
        initializeFirebase();
        if(FirebaseApp.getApps(this).isEmpty()){showLogin();return;}
        auth=FirebaseAuth.getInstance();
        setupGoogle();
        FirebaseUser current=auth.getCurrentUser();
        if(current!=null){showHome();refreshProfileInBackground(current);}else showLogin();
    }

    private void initializeFirebase(){
        if(!FirebaseApp.getApps(this).isEmpty())return;
        if(BuildConfig.FIREBASE_API_KEY.trim().isEmpty())return;
        FirebaseOptions options=new FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .setDatabaseUrl(BuildConfig.FIREBASE_DATABASE_URL)
                .setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                .build();
        FirebaseApp.initializeApp(this,options);
    }

    private void setupGoogle(){
        GoogleSignInOptions options=new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.FIREBASE_WEB_CLIENT_ID)
                .requestEmail()
                .build();
        googleClient=GoogleSignIn.getClient(this,options);
    }

    private boolean firebaseReady(){
        if(auth!=null)return true;
        toast("إعداد Firebase غير موجود في نسخة البناء الحالية");
        return false;
    }

    private void showLogin(){
        currentSection="login";
        LinearLayout card=authScreen("مرحبًا بعودتك","ادخل حسابك وارجع للقصة.");
        EditText email=input("البريد الإلكتروني",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pass=input("كلمة المرور",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        card.addView(email,full(56));gap(card,12);card.addView(pass,full(56));gap(card,16);
        Button login=primary("تسجيل الدخول");card.addView(login,full(56));gap(card,14);
        TextView forgot=label("نسيت كلمة المرور؟",14,accent,Typeface.BOLD);forgot.setGravity(Gravity.CENTER);card.addView(forgot,wrap());
        divider(card,"أو");
        Button google=secondary("G   المتابعة باستخدام Google");card.addView(google,full(56));gap(card,22);
        TextView create=label("ما عندك حساب؟  إنشاء حساب",15,text,Typeface.BOLD);create.setGravity(Gravity.CENTER);card.addView(create,wrap());

        create.setOnClickListener(v->showRegister());
        login.setOnClickListener(v->{
            if(!validEmail(email)||pass.getText().length()<6){toast("تأكد من البريد وكلمة المرور");return;}
            if(!firebaseReady())return;
            setBusy(login,true,"جاري الدخول...");
            auth.signInWithEmailAndPassword(email.getText().toString().trim(),pass.getText().toString())
                    .addOnCompleteListener(this,t->{
                        setBusy(login,false,"تسجيل الدخول");
                        if(t.isSuccessful()){showHome();refreshProfileInBackground(auth.getCurrentUser());}
                        else toast(authMessage(t.getException()));
                    });
        });
        google.setOnClickListener(v->startGoogleSignIn(google));
        forgot.setOnClickListener(v->{
            if(!validEmail(email)){toast("اكتب بريدك أولًا");return;}
            if(!firebaseReady())return;
            auth.sendPasswordResetEmail(email.getText().toString().trim()).addOnCompleteListener(t->toast(t.isSuccessful()?"أرسلنا رابط استعادة كلمة المرور":authMessage(t.getException())));
        });
    }

    private void showRegister(){
        currentSection="register";
        LinearLayout card=authScreen("أنشئ حسابك","أربع خانات فقط، وبعدها تكون جاهز.");
        EditText player=input("اسم اللاعب",InputType.TYPE_CLASS_TEXT);
        EditText user=input("اسم المستخدم  @username",InputType.TYPE_CLASS_TEXT);
        EditText email=input("البريد الإلكتروني",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        EditText pass=input("كلمة المرور",InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);
        addFields(card,player,user,email,pass);gap(card,16);
        Button create=primary("إنشاء الحساب");card.addView(create,full(56));gap(card,22);
        TextView login=label("عندك حساب؟  تسجيل الدخول",15,text,Typeface.BOLD);login.setGravity(Gravity.CENTER);card.addView(login,wrap());
        login.setOnClickListener(v->showLogin());
        create.setOnClickListener(v->{
            String playerName=player.getText().toString().trim(), username=user.getText().toString().trim();
            if(playerName.length()<2){toast("اكتب اسم اللاعب");return;}
            if(!validUsername(username)){toast("اسم المستخدم: 3–18 حرفًا أو رقمًا أو _");return;}
            if(!validEmail(email)){toast("اكتب بريدًا إلكترونيًا صحيحًا");return;}
            if(pass.getText().length()<8){toast("كلمة المرور لازم تكون 8 أحرف على الأقل");return;}
            if(!firebaseReady())return;
            setBusy(create,true,"جاري إنشاء الحساب...");
            auth.createUserWithEmailAndPassword(email.getText().toString().trim(),pass.getText().toString()).addOnCompleteListener(this,t->{
                if(!t.isSuccessful()){setBusy(create,false,"إنشاء الحساب");toast(authMessage(t.getException()));return;}
                cacheProfile(playerName,username,0,0,0,0);
                bootstrapProfile(playerName,username,ok->{setBusy(create,false,"إنشاء الحساب");if(ok)showHome();});
            });
        });
    }

    private void startGoogleSignIn(Button button){
        if(!firebaseReady())return;
        if(googleClient==null)setupGoogle();
        setBusy(button,true,"جاري فتح Google...");
        // نستخدم منتقي Google المباشر بدل Credential Manager حتى لا يظهر خطأ NoCredential.
        googleClient.signOut().addOnCompleteListener(this,t->{
            setBusy(button,false,"G   المتابعة باستخدام Google");
            try{startActivityForResult(googleClient.getSignInIntent(),RC_GOOGLE);}
            catch(Exception e){toast("تعذر فتح قائمة حسابات Google على الجهاز");}
        });
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data){
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode!=RC_GOOGLE)return;
        Task<GoogleSignInAccount> task=GoogleSignIn.getSignedInAccountFromIntent(data);
        try{
            GoogleSignInAccount account=task.getResult(ApiException.class);
            if(account==null||account.getIdToken()==null){toast("تعذر قراءة حساب Google");return;}
            pendingGoogleName=account.getDisplayName()==null?"":account.getDisplayName();
            AuthCredential credential=GoogleAuthProvider.getCredential(account.getIdToken(),null);
            auth.signInWithCredential(credential).addOnCompleteListener(this,t->{
                if(t.isSuccessful())resolveGoogleProfile();
                else toast(authMessage(t.getException()));
            });
        }catch(ApiException e){toast(googleStatusMessage(e.getStatusCode()));}
    }

    private String googleStatusMessage(int code){
        if(code==12501)return "تم إلغاء تسجيل الدخول باستخدام Google";
        if(code==7)return "تعذر الاتصال بخدمات Google، تحقق من الإنترنت وحاول مرة أخرى";
        if(code==10)return "إعداد Google لهذا الإصدار غير متطابق مع توقيع التطبيق";
        return "تعذر تسجيل الدخول باستخدام Google، حاول مرة أخرى";
    }

    private void resolveGoogleProfile(){
        FirebaseUser current=auth==null?null:auth.getCurrentUser();
        if(current==null){toast("انتهت جلسة الدخول");showLogin();return;}
        if(hasCachedProfile()){
            showHome();refreshProfileInBackground(current);return;
        }
        current.getIdToken(false).addOnCompleteListener(t->{
            if(!t.isSuccessful()||t.getResult()==null){showGoogleProfile();return;}
            String token=t.getResult().getToken();
            io.execute(()->{
                try{
                    JSONObject result=getJson("/api/profile/me",token);
                    runOnUiThread(()->{
                        if(result.optBoolean("ok")){
                            cacheProfileObject(result.optJSONObject("profile"));
                            showHome();
                        }else if("profile_not_found".equals(result.optString("error")))showGoogleProfile();
                        else toast(serverMessage(result.optString("error")));
                    });
                }catch(Exception e){runOnUiThread(this::showGoogleProfile);}
            });
        });
    }

    private void showGoogleProfile(){
        currentSection="google_profile";
        LinearLayout card=authScreen("أكمل حساب Google","هذه الخطوة تظهر مرة واحدة فقط للحساب الجديد.");
        EditText player=input("اسم اللاعب",InputType.TYPE_CLASS_TEXT);player.setText(pendingGoogleName);
        EditText user=input("اسم المستخدم  @username",InputType.TYPE_CLASS_TEXT);
        addFields(card,player,user);gap(card,16);
        Button done=primary("إكمال إنشاء الحساب");card.addView(done,full(56));gap(card,16);
        TextView back=label("إلغاء والرجوع",14,muted,Typeface.BOLD);back.setGravity(Gravity.CENTER);card.addView(back,wrap());
        back.setOnClickListener(v->{if(auth!=null)auth.signOut();showLogin();});
        done.setOnClickListener(v->{
            String name=player.getText().toString().trim(), username=user.getText().toString().trim();
            if(name.length()<2||!validUsername(username)){toast("تأكد من اسم اللاعب واسم المستخدم");return;}
            setBusy(done,true,"جاري الحفظ...");
            cacheProfile(name,username,0,0,0,0);
            bootstrapProfile(name,username,ok->{setBusy(done,false,"إكمال إنشاء الحساب");if(ok)showHome();});
        });
    }

    private void showHome(){
        currentSection="home";
        LinearLayout page=basePage();
        LinearLayout top=row();
        TextView brand=label("Cornered",23,text,Typeface.BOLD);top.addView(brand,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView hello=label("جاهز؟",14,muted,Typeface.BOLD);hello.setGravity(Gravity.CENTER_VERTICAL|Gravity.LEFT);top.addView(hello,new LinearLayout.LayoutParams(dp(80),dp(48)));
        page.addView(top,wrap());
        Space flex=new Space(this);page.addView(flex,new LinearLayout.LayoutParams(1,0,1));
        TextView hint=label("القصة تنتظر قرارك",16,muted,Typeface.NORMAL);hint.setGravity(Gravity.CENTER);page.addView(hint,wrap());gap(page,18);
        Button play=primary("ابدأ القصة");play.setTextSize(21);page.addView(play,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(76)));gap(page,14);
        TextView sub=label("ابحث عن جلسة جديدة",13,muted,Typeface.NORMAL);sub.setGravity(Gravity.CENTER);page.addView(sub,wrap());
        Space flex2=new Space(this);page.addView(flex2,new LinearLayout.LayoutParams(1,0,1));
        page.addView(mainBottomBar("home"),full(70));
        setContentView(page);
        play.setOnClickListener(v->toast("البحث عن الجلسة نربطه مع نظام اللعب"));
    }

    private void showProfile(){
        currentSection="profile";
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        LinearLayout root=column();root.setPadding(dp(18),dp(18),dp(18),0);scroll.addView(root,wrap());
        LinearLayout header=row();
        TextView back=label("‹",34,text,Typeface.NORMAL);back.setGravity(Gravity.CENTER);header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));
        TextView title=label("الملف الشخصي",20,text,Typeface.BOLD);title.setGravity(Gravity.CENTER);header.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        TextView gear=label("⚙",22,text,Typeface.NORMAL);gear.setGravity(Gravity.CENTER);header.addView(gear,new LinearLayout.LayoutParams(dp(48),dp(48)));
        root.addView(header,wrap());gap(root,18);

        LinearLayout profileCard=column();profileCard.setPadding(dp(18),dp(22),dp(18),dp(20));profileCard.setBackground(round(panel,24));
        TextView avatar=label(profileInitial(),34,Color.BLACK,Typeface.BOLD);avatar.setGravity(Gravity.CENTER);GradientDrawable av=round(accent,42);av.setStroke(dp(3),Color.rgb(82,130,51));avatar.setBackground(av);LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(88),dp(88));ap.gravity=Gravity.CENTER_HORIZONTAL;profileCard.addView(avatar,ap);gap(profileCard,12);
        TextView name=label(profileName(),24,text,Typeface.BOLD);name.setGravity(Gravity.CENTER);profileCard.addView(name,wrap());gap(profileCard,4);
        TextView username=label("@"+profileUsername(),14,Color.rgb(123,188,255),Typeface.NORMAL);username.setGravity(Gravity.CENTER);profileCard.addView(username,wrap());gap(profileCard,10);
        TextView quote=label("\"Better alone.\"",14,muted,Typeface.NORMAL);quote.setGravity(Gravity.CENTER);profileCard.addView(quote,wrap());gap(profileCard,18);

        LinearLayout stats=row();stats.setBackground(round(Color.rgb(15,18,21),18));stats.setPadding(dp(8),dp(10),dp(8),dp(10));
        stats.addView(stat(String.valueOf(profileLevel()),"المستوى"),new LinearLayout.LayoutParams(0,dp(58),1));
        stats.addView(stat(String.valueOf(profileFriends()),"الأصدقاء"),new LinearLayout.LayoutParams(0,dp(58),1));
        stats.addView(stat(String.valueOf(profileMatches()),"المباريات"),new LinearLayout.LayoutParams(0,dp(58),1));
        profileCard.addView(stats,wrap());gap(profileCard,8);
        TextView xp=label("XP  "+profileXp(),12,muted,Typeface.BOLD);xp.setGravity(Gravity.CENTER);profileCard.addView(xp,wrap());gap(profileCard,16);
        Button edit=secondary("✎  تعديل الملف");profileCard.addView(edit,full(52));gap(profileCard,16);

        LinearLayout tabs=row();
        TextView overview=tab("نظرة عامة",true);TextView achievements=tab("الإنجازات",false);TextView gallery=tab("المعرض",false);
        tabs.addView(overview,new LinearLayout.LayoutParams(0,dp(46),1));tabs.addView(achievements,new LinearLayout.LayoutParams(0,dp(46),1));tabs.addView(gallery,new LinearLayout.LayoutParams(0,dp(46),1));
        profileCard.addView(tabs,wrap());gap(profileCard,14);
        profileCard.addView(infoBlock("نبذة عني","لا أبحث عن أحد.. فقط أستمتع باللعب"),wrap());gap(profileCard,12);
        profileCard.addView(infoBlock("ألعابي المفضلة","Cornered   •   رعب   •   غموض   •   قصص"),wrap());
        root.addView(profileCard,wrap());gap(root,18);
        root.addView(socialBottomBar("profile"),full(70));
        setContentView(scroll);
        back.setOnClickListener(v->showHome());
        gear.setOnClickListener(v->showSettings());
        edit.setOnClickListener(v->toast("تخصيص مظهر الملف هو الخطوة القادمة على هذا التصميم"));
        refreshProfileInBackground(auth==null?null:auth.getCurrentUser());
    }

    private void showShop(){
        currentSection="shop";
        LinearLayout page=basePage();
        page.addView(pageHeader("المتجر",this::showHome),wrap());gap(page,18);
        page.addView(infoBlock("مظاهر الملف","هنا ستظهر المظاهر والعناصر القابلة للشراء. الشراء غير مفعّل قبل ربط اقتصاد اللعبة الحقيقي."),wrap());gap(page,12);
        page.addView(infoBlock("الحالة","الصفحة نفسها مفعّلة الآن، ولن نعرض أسعارًا أو مشتريات وهمية."),wrap());
        Space flex=new Space(this);page.addView(flex,new LinearLayout.LayoutParams(1,0,1));
        page.addView(mainBottomBar("shop"),full(70));setContentView(page);
    }

    private void showRanking(){
        currentSection="ranking";
        LinearLayout page=basePage();
        page.addView(pageHeader("التصنيف",this::showHome),wrap());gap(page,12);
        TextView status=label("جاري تحميل التصنيف الحقيقي...",14,muted,Typeface.NORMAL);status.setGravity(Gravity.CENTER);page.addView(status,wrap());gap(page,12);
        LinearLayout list=column();page.addView(list,new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,0,1));
        page.addView(mainBottomBar("ranking"),full(70));setContentView(page);
        loadRanking(status,list);
    }

    private void loadRanking(TextView status,LinearLayout list){
        FirebaseUser current=auth==null?null:auth.getCurrentUser();
        if(current==null){status.setText("سجل الدخول لعرض التصنيف");return;}
        current.getIdToken(false).addOnCompleteListener(t->{
            if(!t.isSuccessful()||t.getResult()==null){status.setText("تعذر تحميل التصنيف");return;}
            String token=t.getResult().getToken();
            io.execute(()->{
                try{
                    JSONObject result=getJson("/api/rankings",token);
                    JSONArray players=result.optJSONArray("players");
                    runOnUiThread(()->{
                        list.removeAllViews();
                        if(!result.optBoolean("ok")||players==null){status.setText("تعذر تحميل التصنيف");return;}
                        if(players.length()==0){status.setText("لا يوجد لاعبين في التصنيف بعد");return;}
                        status.setText("أفضل اللاعبين حسب XP");
                        for(int i=0;i<players.length();i++){
                            JSONObject p=players.optJSONObject(i);if(p==null)continue;
                            String row=(i+1)+"   "+p.optString("playerName","لاعب")+"   •   المستوى "+p.optInt("level",0)+"   •   XP "+p.optInt("xp",0);
                            TextView item=label(row,15,text,Typeface.BOLD);item.setPadding(dp(14),dp(14),dp(14),dp(14));item.setBackground(round(panel,15));list.addView(item,wrap());gap(list,8);
                        }
                    });
                }catch(Exception e){runOnUiThread(()->status.setText("تعذر تحميل التصنيف الآن"));}
            });
        });
    }

    private void showSettings(){
        currentSection="settings";
        LinearLayout page=basePage();page.addView(pageHeader("الإعدادات",this::showProfile),wrap());gap(page,22);
        Button logout=secondary("تسجيل الخروج");page.addView(logout,full(56));gap(page,12);
        Button delete=secondary("حذف الحساب نهائيًا");delete.setTextColor(Color.rgb(255,105,105));page.addView(delete,full(56));gap(page,12);
        TextView note=label("حذف الحساب نهائي: يحذف حساب الدخول وبيانات الملف واسم المستخدم المحجوز.",13,muted,Typeface.NORMAL);note.setGravity(Gravity.CENTER);page.addView(note,wrap());
        setContentView(page);
        logout.setOnClickListener(v->logoutAccount());
        delete.setOnClickListener(v->confirmDeleteAccount(delete));
    }

    private void logoutAccount(){
        if(auth!=null)auth.signOut();
        if(googleClient!=null)googleClient.signOut();
        prefs.edit().clear().apply();
        showLogin();
    }

    private void confirmDeleteAccount(Button button){
        new AlertDialog.Builder(this)
                .setTitle("حذف الحساب نهائيًا؟")
                .setMessage("لن تستطيع استرجاع الحساب بعد الحذف. هل أنت متأكد؟")
                .setNegativeButton("إلغاء",null)
                .setPositiveButton("حذف نهائي",(d,w)->deleteAccount(button))
                .show();
    }

    private void deleteAccount(Button button){
        FirebaseUser current=auth==null?null:auth.getCurrentUser();
        if(current==null){showLogin();return;}
        setBusy(button,true,"جاري الحذف...");
        current.getIdToken(true).addOnCompleteListener(t->{
            if(!t.isSuccessful()||t.getResult()==null){setBusy(button,false,"حذف الحساب نهائيًا");toast("تعذر تأكيد الحساب");return;}
            String token=t.getResult().getToken();
            io.execute(()->{
                try{
                    JSONObject result=postJson("/api/account/delete",token,new JSONObject());
                    runOnUiThread(()->{
                        if(result.optBoolean("ok")){
                            if(auth!=null)auth.signOut();if(googleClient!=null)googleClient.signOut();prefs.edit().clear().apply();toast("تم حذف الحساب نهائيًا");showLogin();
                        }else{setBusy(button,false,"حذف الحساب نهائيًا");toast(serverMessage(result.optString("error")));}
                    });
                }catch(Exception e){runOnUiThread(()->{setBusy(button,false,"حذف الحساب نهائيًا");toast("تعذر حذف الحساب الآن، حاول مرة أخرى");});}
            });
        });
    }

    private LinearLayout pageHeader(String title,Runnable backAction){
        LinearLayout header=row();TextView back=label("‹",34,text,Typeface.NORMAL);back.setGravity(Gravity.CENTER);header.addView(back,new LinearLayout.LayoutParams(dp(48),dp(48)));
        TextView t=label(title,22,text,Typeface.BOLD);t.setGravity(Gravity.CENTER);header.addView(t,new LinearLayout.LayoutParams(0,dp(48),1));Space x=new Space(this);header.addView(x,new LinearLayout.LayoutParams(dp(48),dp(48)));back.setOnClickListener(v->backAction.run());return header;
    }

    private void showSocialPlaceholder(String section){
        currentSection=section;LinearLayout page=basePage();page.addView(pageHeader("friends".equals(section)?"الأصدقاء":"messages".equals(section)?"المحادثات":"طلبات الصداقة",this::showHome),wrap());
        Space flex=new Space(this);page.addView(flex,new LinearLayout.LayoutParams(1,0,1));TextView empty=label("لا يوجد شيء هنا حاليًا",17,muted,Typeface.BOLD);empty.setGravity(Gravity.CENTER);page.addView(empty,wrap());Space flex2=new Space(this);page.addView(flex2,new LinearLayout.LayoutParams(1,0,1));page.addView(socialBottomBar(section),full(70));setContentView(page);
    }

    private LinearLayout mainBottomBar(String active){
        LinearLayout bar=row();bar.setPadding(dp(4),dp(4),dp(4),dp(4));bar.setBackground(round(panel,22));
        bar.addView(nav("⌂\nالرئيسية","home".equals(active),this::showHome),new LinearLayout.LayoutParams(0,dp(62),1));
        bar.addView(nav("◉\nالملف","profile".equals(active),this::showProfile),new LinearLayout.LayoutParams(0,dp(62),1));
        bar.addView(nav("◇\nالمتجر","shop".equals(active),this::showShop),new LinearLayout.LayoutParams(0,dp(62),1));
        bar.addView(nav("♛\nالتصنيف","ranking".equals(active),this::showRanking),new LinearLayout.LayoutParams(0,dp(62),1));
        return bar;
    }

    private LinearLayout socialBottomBar(String active){
        LinearLayout bar=row();bar.setPadding(dp(4),dp(4),dp(4),dp(4));bar.setBackground(round(panel,22));
        bar.addView(nav("♡\nطلبات الصداقة","requests".equals(active),()->showSocialPlaceholder("requests")),new LinearLayout.LayoutParams(0,dp(62),1));
        bar.addView(nav("♙\nالأصدقاء","friends".equals(active),()->showSocialPlaceholder("friends")),new LinearLayout.LayoutParams(0,dp(62),1));
        bar.addView(nav("▣\nالمحادثات","messages".equals(active),()->showSocialPlaceholder("messages")),new LinearLayout.LayoutParams(0,dp(62),1));return bar;
    }

    private TextView nav(String value,boolean active,Runnable action){TextView v=label(value,12,active?accent:muted,active?Typeface.BOLD:Typeface.NORMAL);v.setGravity(Gravity.CENTER);v.setBackground(active?round(Color.rgb(27,34,26),16):round(Color.TRANSPARENT,16));v.setOnClickListener(x->action.run());return v;}
    private TextView stat(String number,String caption){TextView v=label(number+"\n"+caption,15,text,Typeface.BOLD);v.setGravity(Gravity.CENTER);v.setLineSpacing(0,1.15f);return v;}
    private TextView tab(String title,boolean active){TextView v=label(title,13,active?accent:muted,active?Typeface.BOLD:Typeface.NORMAL);v.setGravity(Gravity.CENTER);if(active)v.setBackground(round(Color.rgb(25,34,23),14));return v;}
    private LinearLayout infoBlock(String title,String value){LinearLayout box=column();box.setPadding(dp(14),dp(13),dp(14),dp(13));box.setBackground(round(Color.rgb(17,19,23),16));box.addView(label(title,15,text,Typeface.BOLD),wrap());gap(box,7);box.addView(label(value,13,muted,Typeface.NORMAL),wrap());return box;}
    private LinearLayout basePage(){LinearLayout page=column();page.setPadding(dp(20),dp(22),dp(20),dp(14));page.setBackgroundColor(bg);page.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return page;}

    private void refreshProfileInBackground(FirebaseUser current){
        if(current==null||BuildConfig.SERVER_BASE_URL.trim().isEmpty())return;
        current.getIdToken(false).addOnCompleteListener(t->{
            if(!t.isSuccessful()||t.getResult()==null)return;String token=t.getResult().getToken();
            io.execute(()->{try{JSONObject result=getJson("/api/profile/me",token);if(result.optBoolean("ok"))cacheProfileObject(result.optJSONObject("profile"));}catch(Exception ignored){}});
        });
    }

    private void cacheProfileObject(JSONObject p){if(p==null)return;cacheProfile(p.optString("playerName"),p.optString("username"),p.optInt("level",0),p.optInt("xp",0),p.optInt("matches",0),p.optInt("friendsCount",0));}
    private interface BoolCallback{void done(boolean ok);}

    private void bootstrapProfile(String playerName,String username,BoolCallback callback){
        FirebaseUser current=auth==null?null:auth.getCurrentUser();
        if(current==null){toast("انتهت جلسة الدخول");callback.done(false);return;}
        current.getIdToken(false).addOnCompleteListener(t->{
            if(!t.isSuccessful()||t.getResult()==null){toast("تعذر تأكيد جلسة الحساب");callback.done(false);return;}String token=t.getResult().getToken();
            io.execute(()->{try{JSONObject body=new JSONObject();body.put("playerName",playerName);body.put("username",username);JSONObject result=postJson("/api/profile/bootstrap",token,body);runOnUiThread(()->{if(result.optBoolean("ok")){cacheProfileObject(result.optJSONObject("profile"));callback.done(true);}else{toast(serverMessage(result.optString("error")));callback.done(false);}});}catch(Exception e){runOnUiThread(()->{toast("تعذر الاتصال بالخادم، تحقق من الإنترنت وحاول مرة أخرى");callback.done(false);});}});
        });
    }

    private void cacheProfile(String playerName,String username,int level,int xp,int matches,int friends){
        if(playerName==null||username==null)return;if(playerName.trim().isEmpty()||username.trim().isEmpty())return;
        prefs.edit().putString("playerName",playerName.trim()).putString("username",username.trim().replace("@","")).putInt("level",Math.max(0,level)).putInt("xp",Math.max(0,xp)).putInt("matches",Math.max(0,matches)).putInt("friendsCount",Math.max(0,friends)).apply();
    }
    private boolean hasCachedProfile(){return !profileName().isEmpty()&&!profileUsername().isEmpty();}
    private String profileName(){String s=prefs.getString("playerName","");return s==null?"":s.trim();}
    private String profileUsername(){String s=prefs.getString("username","");if(s==null||s.trim().isEmpty())return "player";return s.trim().replace("@","");}
    private String profileInitial(){String n=profileName();if(n.isEmpty())return "C";return String.valueOf(n.charAt(0)).toUpperCase();}
    private int profileLevel(){return prefs.getInt("level",0);}
    private int profileXp(){return prefs.getInt("xp",0);}
    private int profileMatches(){return prefs.getInt("matches",0);}
    private int profileFriends(){return prefs.getInt("friendsCount",0);}

    private JSONObject postJson(String path,String token,JSONObject body)throws Exception{URL url=new URL(BuildConfig.SERVER_BASE_URL.replaceAll("/$","")+path);HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setRequestMethod("POST");c.setConnectTimeout(4500);c.setReadTimeout(6500);c.setDoOutput(true);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Connection","keep-alive");try(OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(StandardCharsets.UTF_8));}return readJsonResponse(c);}
    private JSONObject getJson(String path,String token)throws Exception{URL url=new URL(BuildConfig.SERVER_BASE_URL.replaceAll("/$","")+path);HttpURLConnection c=(HttpURLConnection)url.openConnection();c.setRequestMethod("GET");c.setConnectTimeout(4500);c.setReadTimeout(6500);c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Connection","keep-alive");return readJsonResponse(c);}
    private JSONObject readJsonResponse(HttpURLConnection c)throws Exception{int code=c.getResponseCode();java.io.InputStream stream=code>=400?c.getErrorStream():c.getInputStream();if(stream==null){c.disconnect();return new JSONObject().put("error","network_error");}BufferedReader br=new BufferedReader(new InputStreamReader(stream,StandardCharsets.UTF_8));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);br.close();c.disconnect();return new JSONObject(sb.length()==0?"{}":sb.toString());}

    private String authMessage(Exception e){
        if(e==null)return "حدث خطأ في تسجيل الدخول، حاول مرة أخرى";Throwable current=e;
        while(current!=null){if(current instanceof FirebaseAuthException){String code=((FirebaseAuthException)current).getErrorCode();if("ERROR_EMAIL_ALREADY_IN_USE".equals(code)||"ERROR_ACCOUNT_EXISTS_WITH_DIFFERENT_CREDENTIAL".equals(code)||"ERROR_CREDENTIAL_ALREADY_IN_USE".equals(code))return "هذا البريد الإلكتروني مرتبط بحساب موجود مسبقًا. سجل الدخول بدل إنشاء حساب جديد.";if("ERROR_INVALID_EMAIL".equals(code))return "البريد الإلكتروني غير صحيح";if("ERROR_WEAK_PASSWORD".equals(code))return "كلمة المرور ضعيفة، استخدم كلمة مرور أقوى";if("ERROR_WRONG_PASSWORD".equals(code)||"ERROR_USER_NOT_FOUND".equals(code)||"ERROR_INVALID_CREDENTIAL".equals(code))return "البريد الإلكتروني أو كلمة المرور غير صحيحة";if("ERROR_USER_DISABLED".equals(code))return "هذا الحساب موقوف حاليًا";if("ERROR_TOO_MANY_REQUESTS".equals(code))return "محاولات كثيرة، انتظر قليلًا ثم حاول مرة أخرى";if("ERROR_NETWORK_REQUEST_FAILED".equals(code))return "تعذر الاتصال بالإنترنت، تحقق من الشبكة وحاول مرة أخرى";return "تعذر إكمال العملية، حاول مرة أخرى";}current=current.getCause();}return "تعذر إكمال العملية، حاول مرة أخرى";
    }
    private String serverMessage(String code){if("username_taken".equals(code))return "اسم المستخدم مستخدم، اختر اسمًا آخر";if("invalid_username".equals(code))return "اسم المستخدم غير صالح";if("invalid_player_name".equals(code))return "اسم اللاعب غير صالح";if("profile_not_found".equals(code))return "لم يتم العثور على بيانات الحساب";if("auth_required".equals(code))return "انتهت جلسة الدخول، سجل الدخول مرة أخرى";if("network_error".equals(code))return "تعذر الاتصال بالخادم، حاول مرة أخرى";return "تعذر إكمال العملية، حاول مرة أخرى";}

    private LinearLayout authScreen(String title,String subtitle){ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(bg);scroll.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);LinearLayout root=column();root.setPadding(dp(24),dp(42),dp(24),dp(32));root.setGravity(Gravity.CENTER_HORIZONTAL);scroll.addView(root,wrap());TextView mark=label("C",27,Color.BLACK,Typeface.BOLD);mark.setGravity(Gravity.CENTER);mark.setBackground(round(accent,20));root.addView(mark,new LinearLayout.LayoutParams(dp(60),dp(60)));gap(root,20);TextView t=label(title,29,text,Typeface.BOLD);t.setGravity(Gravity.CENTER);root.addView(t,wrap());gap(root,8);TextView s=label(subtitle,15,muted,Typeface.NORMAL);s.setGravity(Gravity.CENTER);root.addView(s,wrap());gap(root,30);LinearLayout card=column();card.setPadding(dp(18),dp(22),dp(18),dp(22));card.setBackground(round(panel,24));root.addView(card,wrap());gap(root,22);TextView footer=label("Cornered  •  0.1.11",12,Color.rgb(88,92,101),Typeface.NORMAL);footer.setGravity(Gravity.CENTER);root.addView(footer,wrap());setContentView(scroll);return card;}
    private void setBusy(Button b,boolean busy,String value){b.setEnabled(!busy);b.setAlpha(busy?.65f:1f);b.setText(value);}
    private EditText input(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setHintTextColor(Color.rgb(112,117,128));e.setTextColor(text);e.setTextSize(16);e.setSingleLine(true);e.setInputType(type);e.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);e.setPadding(dp(16),0,dp(16),0);e.setBackground(round(field,15));return e;}
    private Button primary(String s){Button b=button(s);b.setTextColor(Color.BLACK);b.setBackground(round(accent,16));return b;}
    private Button secondary(String s){Button b=button(s);b.setTextColor(text);GradientDrawable d=round(field,16);d.setStroke(dp(1),Color.rgb(55,59,68));b.setBackground(d);return b;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setTextSize(16);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setAllCaps(false);b.setStateListAnimator(null);return b;}
    private void addFields(LinearLayout p,EditText... es){for(int i=0;i<es.length;i++){p.addView(es[i],full(56));if(i<es.length-1)gap(p,12);}}
    private void divider(LinearLayout p,String word){gap(p,20);LinearLayout r=new LinearLayout(this);r.setGravity(Gravity.CENTER);View a=new View(this),b=new View(this);a.setBackgroundColor(Color.rgb(54,57,65));b.setBackgroundColor(Color.rgb(54,57,65));r.addView(a,new LinearLayout.LayoutParams(0,dp(1),1));TextView t=label(word,13,muted,Typeface.NORMAL);t.setGravity(Gravity.CENTER);r.addView(t,new LinearLayout.LayoutParams(dp(50),dp(30)));r.addView(b,new LinearLayout.LayoutParams(0,dp(1),1));p.addView(r,wrap());gap(p,12);}
    private boolean validEmail(EditText e){return Patterns.EMAIL_ADDRESS.matcher(e.getText().toString().trim()).matches();}
    private boolean validUsername(String s){return s.trim().matches("[A-Za-z0-9_]{3,18}");}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private LinearLayout column(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);return l;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);l.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);l.setGravity(Gravity.CENTER_VERTICAL);return l;}
    private TextView label(String s,int z,int c,int style){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(c);t.setTypeface(Typeface.DEFAULT,style);t.setTextDirection(View.TEXT_DIRECTION_RTL);return t;}
    private GradientDrawable round(int c,int r){GradientDrawable d=new GradientDrawable();d.setColor(c);d.setCornerRadius(dp(r));return d;}
    private void gap(LinearLayout p,int h){Space s=new Space(this);p.addView(s,new LinearLayout.LayoutParams(1,dp(h)));}
    private LinearLayout.LayoutParams wrap(){return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);}
    private LinearLayout.LayoutParams full(int h){return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dp(h));}
    private int dp(int n){return(int)(n*getResources().getDisplayMetrics().density+.5f);}

    @Override public void onBackPressed(){
        if("profile".equals(currentSection)||"shop".equals(currentSection)||"ranking".equals(currentSection)||"requests".equals(currentSection)||"friends".equals(currentSection)||"messages".equals(currentSection)){showHome();return;}
        if("settings".equals(currentSection)){showProfile();return;}
        if("home".equals(currentSection)){moveTaskToBack(true);return;}
        if(auth!=null&&auth.getCurrentUser()!=null){showHome();return;}
        if(!"login".equals(currentSection)){showLogin();return;}
        super.onBackPressed();
    }
}
