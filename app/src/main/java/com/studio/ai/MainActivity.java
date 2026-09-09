package com.studio.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private SharedPreferences prefs;

    private final int BG = Color.rgb(9,11,15);
    private final int CARD = Color.rgb(19,23,30);
    private final int MUTED = Color.rgb(151,160,174);
    private final int GREEN = Color.rgb(124,255,178);

    private final String[] styles = {
            "إعلانات الشركات", "تجمعيات أنمي", "TikTok", "YouTube", "رعب", "أكشن", "عام"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        prefs = getSharedPreferences("studio", MODE_PRIVATE);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        showHome();
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, float sp, int color, boolean bold) {
        TextView v = new TextView(this);
        v.setText(value);
        v.setTextSize(sp);
        v.setTextColor(color);
        v.setTypeface(Typeface.create("sans", bold ? Typeface.BOLD : Typeface.NORMAL));
        v.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        v.setTextDirection(View.TEXT_DIRECTION_RTL);
        v.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        v.setLineSpacing(0f, 1.12f);
        v.setIncludeFontPadding(true);
        return v;
    }

    private GradientDrawable bg(int color, float radiusDp) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(radiusDp));
        return d;
    }

    private Button button(String label) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextSize(16);
        b.setAllCaps(false);
        b.setTextColor(Color.rgb(5,20,13));
        b.setTypeface(Typeface.create("sans", Typeface.BOLD));
        b.setGravity(Gravity.CENTER);
        b.setBackground(bg(GREEN, 18));
        b.setPadding(dp(18), dp(12), dp(18), dp(12));
        return b;
    }

    private void base(String title, String sub) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(BG);
        scroll.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);

        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.setPadding(dp(20), dp(18), dp(20), dp(28));
        root.setBackgroundColor(BG);
        scroll.addView(root, new ScrollView.LayoutParams(-1, -2));
        setContentView(scroll);

        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            v.setPadding(dp(20), top + dp(14), dp(20), bottom + dp(24));
            return insets;
        });
        root.requestApplyInsets();

        TextView brand = text("STUDIO  /  AI", 12, GREEN, true);
        brand.setTextDirection(View.TEXT_DIRECTION_LTR);
        brand.setGravity(Gravity.END);
        brand.setLetterSpacing(.16f);
        LinearLayout.LayoutParams brandLp = new LinearLayout.LayoutParams(-1, -2);
        brandLp.setMargins(0, 0, 0, dp(8));
        root.addView(brand, brandLp);

        TextView titleView = text(title, 29, Color.WHITE, true);
        titleView.setGravity(Gravity.END);
        LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(-1, -2);
        titleLp.setMargins(0, 0, 0, dp(4));
        root.addView(titleView, titleLp);

        TextView subtitle = text(sub, 14, MUTED, false);
        subtitle.setGravity(Gravity.END);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, 0, 0, dp(22));
        root.addView(subtitle, subLp);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        root.addView(content, new LinearLayout.LayoutParams(-1, -2));
    }

    private void showHome() {
        base("الاستوديو", "مساحة عملك الخاصة للمونتاج والتعلّم من الأساليب.");

        Button add = button("＋  إنشاء مشروع");
        add.setOnClickListener(v -> showName());
        content.addView(add, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView h = text("المشاريع الأخيرة", 18, Color.WHITE, true);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(-1, -2);
        hLp.setMargins(0, dp(28), 0, dp(12));
        content.addView(h, hLp);

        Set<String> projects = prefs.getStringSet("projects", new LinkedHashSet<>());
        if (projects.isEmpty()) {
            TextView empty = text("لا توجد مشاريع بعد. ابدأ أول مشروع من الزر بالأعلى.", 14, MUTED, false);
            empty.setPadding(0, dp(4), 0, dp(4));
            content.addView(empty);
        } else {
            for (String p : projects) {
                String[] x = p.split("\\|", 2);
                addProjectCard(x[0], x.length > 1 ? x[1] : "عام");
            }
        }
    }

    private void addProjectCard(String name, String style) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackground(bg(CARD, 18));
        card.setPadding(dp(18), dp(14), dp(18), dp(14));
        card.setMinimumHeight(dp(78));

        TextView n = text(name, 17, Color.WHITE, true);
        TextView s = text(style, 13, MUTED, false);
        card.addView(n, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(-1, -2);
        sLp.setMargins(0, dp(3), 0, 0);
        card.addView(s, sLp);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        content.addView(card, lp);
        card.setOnClickListener(v -> showWorkspace(name, style));
    }

    private void showName() {
        base("مشروع جديد", "أعط المشروع اسمًا واضحًا. سنفصل ذاكرته وملفاته عن بقية المشاريع.");

        EditText name = new EditText(this);
        name.setHint("مثال: إعلان مطعم العميل");
        name.setHintTextColor(MUTED);
        name.setTextColor(Color.WHITE);
        name.setTextSize(17);
        name.setSingleLine(true);
        name.setTextDirection(View.TEXT_DIRECTION_RTL);
        name.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        name.setBackground(bg(CARD, 18));
        name.setPadding(dp(18), dp(12), dp(18), dp(12));
        content.addView(name, new LinearLayout.LayoutParams(-1, dp(58)));

        Button next = button("التالي  ←");
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(56));
        lp.setMargins(0, dp(18), 0, 0);
        content.addView(next, lp);
        next.setOnClickListener(v -> {
            String n = name.getText().toString().trim();
            if (n.isEmpty()) {
                name.setError("اكتب اسم المشروع");
                return;
            }
            showStyles(n);
        });
        back();
    }

    private void showStyles(String name) {
        base("اختر أسلوب المشروع", "لكل قسم ذاكرته وأرشيفه وشاته ومؤثراته وانتقالاته بشكل مستقل.");
        for (String style : styles) {
            TextView card = text(style, 17, Color.WHITE, true);
            card.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
            card.setBackground(bg(CARD, 18));
            card.setPadding(dp(18), dp(12), dp(18), dp(12));
            card.setMinimumHeight(dp(58));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
            lp.setMargins(0, 0, 0, dp(10));
            content.addView(card, lp);
            card.setOnClickListener(v -> saveProject(name, style));
        }
        back();
    }

    private void saveProject(String name, String style) {
        Set<String> old = new LinkedHashSet<>(prefs.getStringSet("projects", new LinkedHashSet<>()));
        old.add(name + "|" + style);
        prefs.edit().putStringSet("projects", old).apply();
        showWorkspace(name, style);
    }

    private void showWorkspace(String name, String style) {
        base(name, style + "  •  مساحة مشروع مستقلة");

        addWorkspaceCard("🎬", "الفيديوهات", "مواد العميل والمراجع");
        addWorkspaceCard("💬", "شات المشروع", "التواصل مع مساعد هذا القسم");
        addWorkspaceCard("🧠", "الذاكرة", "ذاكرة قصيرة + طويلة للقسم");
        addWorkspaceCard("🗂", "الأرشيف", "التحليلات والأساليب المحفوظة");
        addWorkspaceCard("✨", "المؤثرات والانتقالات", "مكتبة " + style);
        back();
    }

    private void addWorkspaceCard(String icon, String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackground(bg(CARD, 18));
        card.setPadding(dp(16), dp(13), dp(16), dp(13));
        card.setMinimumHeight(dp(74));

        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setTextSize(22);
        iconView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dp(40), -1);
        iconLp.setMarginStart(dp(8));
        card.addView(iconView, iconLp);

        LinearLayout labels = new LinearLayout(this);
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        labels.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);

        TextView t = text(title, 17, Color.WHITE, true);
        TextView s = text(subtitle, 13, MUTED, false);
        labels.addView(t, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(-1, -2);
        sLp.setMargins(0, dp(2), 0, 0);
        labels.addView(s, sLp);
        card.addView(labels, new LinearLayout.LayoutParams(0, -2, 1f));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        content.addView(card, lp);
    }

    private void back() {
        TextView b = text("‹  رجوع للرئيسية", 15, GREEN, true);
        b.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        b.setPadding(0, dp(22), 0, dp(18));
        root.addView(b, new LinearLayout.LayoutParams(-1, -2));
        b.setOnClickListener(v -> showHome());
    }

    @Override public void onBackPressed() {
        showHome();
    }
}
