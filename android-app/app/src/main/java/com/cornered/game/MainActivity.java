package com.cornered.game;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
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

    private static final String PREFS = "cornered_prefs";
    private static final String KEY_NICKNAME = "nickname";

    private final int bg = Color.rgb(12, 13, 16);
    private final int panel = Color.rgb(22, 24, 29);
    private final int panelSoft = Color.rgb(29, 32, 38);
    private final int text = Color.rgb(244, 244, 246);
    private final int muted = Color.rgb(159, 163, 174);
    private final int accent = Color.rgb(196, 255, 95);
    private final int danger = Color.rgb(255, 120, 120);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.setStatusBarColor(bg);
        window.setNavigationBarColor(bg);
        getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        String nickname = prefs.getString(KEY_NICKNAME, "");
        if (nickname != null && !nickname.trim().isEmpty()) {
            showHome(nickname.trim());
        } else {
            showLogin();
        }
    }

    private void showLogin() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = column();
        root.setPadding(dp(24), dp(52), dp(24), dp(32));
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        scroll.addView(root, matchWrap());

        TextView mark = text("C", 28, Color.BLACK, Typeface.BOLD);
        mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(accent, 22));
        root.addView(mark, size(dp(64), dp(64)));

        root.addView(space(22));

        TextView title = text("ادخل القصة", 30, text, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        root.addView(space(10));

        TextView subtitle = text("كل جلسة شخصية مختلفة، وكل كلمة منك تغيّر النهاية.", 16, muted, Typeface.NORMAL);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setLineSpacing(0, 1.15f);
        root.addView(subtitle, matchWrap());

        root.addView(space(36));

        LinearLayout card = column();
        card.setPadding(dp(18), dp(20), dp(18), dp(18));
        card.setBackground(round(panel, 24));
        root.addView(card, matchWrap());

        TextView label = text("اختر لقبك داخل الجلسات", 14, muted, Typeface.BOLD);
        card.addView(label, matchWrap());
        card.addView(space(10));

        EditText nickname = new EditText(this);
        nickname.setTextColor(text);
        nickname.setHintTextColor(Color.rgb(105, 109, 119));
        nickname.setHint("مثال: الغراب");
        nickname.setTextSize(17);
        nickname.setSingleLine(true);
        nickname.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        nickname.setPadding(dp(16), 0, dp(16), 0);
        nickname.setInputType(InputType.TYPE_CLASS_TEXT);
        nickname.setBackground(round(panelSoft, 16));
        card.addView(nickname, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        card.addView(space(14));

        Button enter = new Button(this);
        enter.setText("دخول");
        enter.setTextSize(17);
        enter.setTextColor(Color.BLACK);
        enter.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        enter.setAllCaps(false);
        enter.setBackground(round(accent, 16));
        card.addView(enter, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(56)));

        card.addView(space(12));
        TextView note = text("سيظهر هذا اللقب للاعبين بدل اسمك الحقيقي.", 13, muted, Typeface.NORMAL);
        note.setGravity(Gravity.CENTER);
        card.addView(note, matchWrap());

        root.addView(space(24));
        TextView footer = text("نسخة تجريبية 0.1", 12, Color.rgb(90, 94, 103), Typeface.NORMAL);
        footer.setGravity(Gravity.CENTER);
        root.addView(footer, matchWrap());

        enter.setOnClickListener(v -> {
            String value = nickname.getText().toString().trim();
            if (value.length() < 2) {
                Toast.makeText(this, "اكتب لقبًا من حرفين على الأقل", Toast.LENGTH_SHORT).show();
                return;
            }
            if (value.length() > 18) {
                Toast.makeText(this, "خل اللقب أقصر من 18 حرفًا", Toast.LENGTH_SHORT).show();
                return;
            }
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().putString(KEY_NICKNAME, value).apply();
            showHome(value);
        });

        setContentView(scroll);
    }

    private void showHome(String nickname) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(bg);

        LinearLayout root = column();
        root.setPadding(dp(22), dp(42), dp(22), dp(30));
        scroll.addView(root, matchWrap());

        LinearLayout top = new LinearLayout(this);
        top.setOrientation(LinearLayout.HORIZONTAL);
        top.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(top, matchWrap());

        TextView avatar = text(firstLetter(nickname), 18, Color.BLACK, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(round(accent, 16));
        top.addView(avatar, size(dp(48), dp(48)));

        LinearLayout nameBlock = column();
        LinearLayout.LayoutParams nameParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        nameParams.setMarginStart(dp(12));
        top.addView(nameBlock, nameParams);

        TextView welcome = text("هلا، " + nickname, 20, text, Typeface.BOLD);
        nameBlock.addView(welcome, matchWrap());
        TextView status = text("جاهز لقصة جديدة؟", 13, muted, Typeface.NORMAL);
        nameBlock.addView(status, matchWrap());

        TextView logout = text("تغيير اللقب", 13, muted, Typeface.BOLD);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(dp(12), dp(10), dp(12), dp(10));
        logout.setBackground(round(panel, 14));
        top.addView(logout, wrapWrap());
        logout.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE).edit().remove(KEY_NICKNAME).apply();
            showLogin();
        });

        root.addView(space(34));

        LinearLayout hero = column();
        hero.setPadding(dp(22), dp(24), dp(22), dp(24));
        hero.setBackground(round(panel, 28));
        root.addView(hero, matchWrap());

        TextView live = text("●  جلسة عشوائية", 13, accent, Typeface.BOLD);
        hero.addView(live, matchWrap());
        hero.addView(space(12));

        TextView heroTitle = text("7 لاعبين.\nشخصية واحدة.\n15 دقيقة تغيّر القصة.", 29, text, Typeface.BOLD);
        heroTitle.setLineSpacing(dp(2), 1.05f);
        hero.addView(heroTitle, matchWrap());

        hero.addView(space(14));
        TextView heroText = text("لن تعرف من ستقابل. افهم الشخصية، أثّر عليها، واكتشف النهاية التي صنعتموها معًا.", 15, muted, Typeface.NORMAL);
        heroText.setLineSpacing(dp(2), 1.15f);
        hero.addView(heroText, matchWrap());

        hero.addView(space(24));

        Button search = new Button(this);
        search.setText("ابحث عن جلسة");
        search.setTextSize(17);
        search.setTextColor(Color.BLACK);
        search.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        search.setAllCaps(false);
        search.setBackground(round(accent, 18));
        hero.addView(search, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(58)));

        search.setOnClickListener(v -> Toast.makeText(this, "البحث عن الجلسات نربطه في الخطوة القادمة", Toast.LENGTH_SHORT).show());

        root.addView(space(28));
        TextView section = text("كيف تبدأ؟", 17, text, Typeface.BOLD);
        root.addView(section, matchWrap());
        root.addView(space(12));

        LinearLayout steps = column();
        steps.setPadding(dp(18), dp(6), dp(18), dp(6));
        steps.setBackground(round(panel, 22));
        root.addView(steps, matchWrap());

        addStep(steps, "1", "ادخل البحث", "ننتظر حتى يكتمل 7 لاعبين.");
        addDivider(steps);
        addStep(steps, "2", "اكتشف الشخصية", "الخلفية تظهر داخل الشات، والباقي تكتشفه بنفسك.");
        addDivider(steps);
        addStep(steps, "3", "اصنع النهاية", "بعد 15 دقيقة تظهر النهاية، سببها، وأكثر لاعب أثّر في الشخصية.");

        root.addView(space(22));
        TextView noHistory = text("سجل جلساتك سيظهر هنا بعد أول قصة.", 13, Color.rgb(104, 108, 117), Typeface.NORMAL);
        noHistory.setGravity(Gravity.CENTER);
        root.addView(noHistory, matchWrap());

        setContentView(scroll);
    }

    private void addStep(LinearLayout parent, String number, String titleValue, String bodyValue) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(14), 0, dp(14));
        parent.addView(row, matchWrap());

        TextView badge = text(number, 14, Color.BLACK, Typeface.BOLD);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(round(accent, 13));
        row.addView(badge, size(dp(36), dp(36)));

        LinearLayout copy = column();
        LinearLayout.LayoutParams copyParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        copyParams.setMarginStart(dp(12));
        row.addView(copy, copyParams);

        TextView title = text(titleValue, 15, text, Typeface.BOLD);
        copy.addView(title, matchWrap());
        TextView body = text(bodyValue, 13, muted, Typeface.NORMAL);
        body.setLineSpacing(dp(1), 1.1f);
        copy.addView(body, matchWrap());
    }

    private void addDivider(LinearLayout parent) {
        View divider = new View(this);
        divider.setBackgroundColor(Color.rgb(42, 45, 52));
        parent.addView(divider, size(ViewGroup.LayoutParams.MATCH_PARENT, dp(1)));
    }

    private LinearLayout column() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        return layout;
    }

    private TextView text(String value, int sp, int color, int style) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setTypeface(Typeface.DEFAULT, style);
        view.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        view.setTextDirection(View.TEXT_DIRECTION_RTL);
        return view;
    }

    private GradientDrawable round(int color, int radiusDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        return drawable;
    }

    private Space space(int heightDp) {
        Space space = new Space(this);
        space.setLayoutParams(size(1, dp(heightDp)));
        return space;
    }

    private String firstLetter(String value) {
        if (value == null || value.trim().isEmpty()) return "؟";
        return value.trim().substring(0, 1);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams wrapWrap() {
        return new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams size(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }
}
