package com.studio.ai;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends Activity {
    private LinearLayout root, content;
    private SharedPreferences prefs;
    private String activeProjectName = "";
    private String activeProjectStyle = "";
    private Runnable backAction;

    private static final int PICK_VIDEO = 2001;
    private final int BG = Color.rgb(9,11,15);
    private final int CARD = Color.rgb(19,23,30);
    private final int CARD_2 = Color.rgb(25,30,39);
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
        activeProjectName = "";
        activeProjectStyle = "";
        backAction = null;
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
        backAction = this::showHome;
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
        addBack("رجوع للرئيسية", this::showHome);
    }

    private void showStyles(String name) {
        backAction = this::showHome;
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
        addBack("رجوع للرئيسية", this::showHome);
    }

    private void saveProject(String name, String style) {
        Set<String> old = new LinkedHashSet<>(prefs.getStringSet("projects", new LinkedHashSet<>()));
        old.add(name + "|" + style);
        prefs.edit().putStringSet("projects", old).apply();
        showWorkspace(name, style);
    }

    private void showWorkspace(String name, String style) {
        activeProjectName = name;
        activeProjectStyle = style;
        backAction = this::showHome;
        base(name, style + "  •  مساحة مشروع مستقلة");

        addWorkspaceCard("🎬", "الفيديوهات", "استيراد مواد العميل والمراجع وتشغيلها", true, () -> showVideos(name, style));
        addWorkspaceCard("💬", "شات المشروع", "سيتم تفعيله في دفعة الشات", false, null);
        addWorkspaceCard("🧠", "الذاكرة", "سيتم تفعيلها في دفعات الذاكرة", false, null);
        addWorkspaceCard("🗂", "الأرشيف", "المواد المحفوظة داخل هذا المشروع", true, () -> showArchive(name, style));
        addWorkspaceCard("✨", "المؤثرات والانتقالات", "سيتم تفعيل المكتبة في دفعتها", false, null);
        addBack("رجوع للرئيسية", this::showHome);
    }

    private void addWorkspaceCard(String icon, String title, String subtitle, boolean enabled, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackground(bg(enabled ? CARD : Color.rgb(15,18,23), 18));
        card.setPadding(dp(16), dp(13), dp(16), dp(13));
        card.setMinimumHeight(dp(74));
        card.setAlpha(enabled ? 1f : .60f);

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

        if (enabled && action != null) card.setOnClickListener(v -> action.run());

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        content.addView(card, lp);
    }

    private String videoKey(String name, String style) {
        return "videos_" + Integer.toHexString((name + "|" + style).hashCode());
    }

    private JSONArray getVideos(String name, String style) {
        try {
            return new JSONArray(prefs.getString(videoKey(name, style), "[]"));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void saveVideos(String name, String style, JSONArray videos) {
        prefs.edit().putString(videoKey(name, style), videos.toString()).apply();
    }

    private void showVideos(String name, String style) {
        activeProjectName = name;
        activeProjectStyle = style;
        backAction = () -> showWorkspace(name, style);
        base("فيديوهات المشروع", name + "  •  " + style);

        Button add = button("＋  استيراد فيديو من الجوال");
        add.setOnClickListener(v -> pickVideo());
        content.addView(add, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView note = text("الاستيراد هنا حقيقي: التطبيق يحتفظ بصلاحية الوصول للفيديو حتى بعد إغلاقه.", 13, MUTED, false);
        LinearLayout.LayoutParams noteLp = new LinearLayout.LayoutParams(-1, -2);
        noteLp.setMargins(0, dp(12), 0, dp(18));
        content.addView(note, noteLp);

        JSONArray videos = getVideos(name, style);
        if (videos.length() == 0) {
            content.addView(text("لا توجد فيديوهات في هذا المشروع حتى الآن.", 14, MUTED, false));
        } else {
            for (int i = videos.length() - 1; i >= 0; i--) {
                JSONObject item = videos.optJSONObject(i);
                if (item != null) addVideoCard(item, name, style, false);
            }
        }
        addBack("رجوع للمشروع", () -> showWorkspace(name, style));
    }

    private void showArchive(String name, String style) {
        activeProjectName = name;
        activeProjectStyle = style;
        backAction = () -> showWorkspace(name, style);
        base("أرشيف المشروع", "المواد التي حفظتها داخل " + name);

        JSONArray videos = getVideos(name, style);
        if (videos.length() == 0) {
            content.addView(text("الأرشيف فارغ. أضف فيديو من قسم الفيديوهات أولاً.", 14, MUTED, false));
        } else {
            TextView h = text("الفيديوهات المحفوظة  •  " + videos.length(), 17, Color.WHITE, true);
            LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(-1, -2);
            hLp.setMargins(0, 0, 0, dp(12));
            content.addView(h, hLp);
            for (int i = videos.length() - 1; i >= 0; i--) {
                JSONObject item = videos.optJSONObject(i);
                if (item != null) addVideoCard(item, name, style, true);
            }
        }

        TextView future = text("ملاحظة: تحليلات الفيديو والأساليب المتعلّمة لن تظهر هنا قبل بناء محرك التحليل في الدفعات القادمة.", 13, MUTED, false);
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(-1, -2);
        fLp.setMargins(0, dp(14), 0, 0);
        content.addView(future, fLp);
        addBack("رجوع للمشروع", () -> showWorkspace(name, style));
    }

    private void addVideoCard(JSONObject item, String name, String style, boolean archiveMode) {
        String fileName = item.optString("name", "فيديو");
        String uri = item.optString("uri", "");
        long added = item.optLong("added", 0L);
        String duration = getDuration(Uri.parse(uri));
        String date = added > 0 ? new SimpleDateFormat("yyyy/MM/dd  HH:mm", Locale.getDefault()).format(new Date(added)) : "";

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        card.setBackground(bg(CARD, 18));
        card.setPadding(dp(16), dp(14), dp(16), dp(14));

        TextView title = text("🎞  " + fileName, 16, Color.WHITE, true);
        card.addView(title, new LinearLayout.LayoutParams(-1, -2));
        String meta = (duration.isEmpty() ? "" : "المدة " + duration) + (date.isEmpty() ? "" : (duration.isEmpty() ? "" : "  •  ") + "أضيف " + date);
        TextView sub = text(meta.isEmpty() ? "جاهز للتشغيل" : meta, 12.5f, MUTED, false);
        LinearLayout.LayoutParams subLp = new LinearLayout.LayoutParams(-1, -2);
        subLp.setMargins(0, dp(5), 0, 0);
        card.addView(sub, subLp);

        TextView status = text(archiveMode ? "محفوظ في أرشيف المشروع" : "اضغط للتشغيل", 12.5f, GREEN, true);
        LinearLayout.LayoutParams stLp = new LinearLayout.LayoutParams(-1, -2);
        stLp.setMargins(0, dp(7), 0, 0);
        card.addView(status, stLp);

        card.setOnClickListener(v -> showPlayer(fileName, uri, name, style));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2);
        lp.setMargins(0, 0, 0, dp(10));
        content.addView(card, lp);
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_VIDEO);
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != PICK_VIDEO || resultCode != RESULT_OK || data == null || data.getData() == null) return;

        Uri uri = data.getData();
        try {
            final int flags = data.getFlags() & (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            getContentResolver().takePersistableUriPermission(uri, flags & Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (Exception ignored) {}

        String fileName = getDisplayName(uri);
        JSONArray videos = getVideos(activeProjectName, activeProjectStyle);
        boolean exists = false;
        for (int i = 0; i < videos.length(); i++) {
            JSONObject old = videos.optJSONObject(i);
            if (old != null && uri.toString().equals(old.optString("uri"))) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            JSONObject obj = new JSONObject();
            try {
                obj.put("name", fileName);
                obj.put("uri", uri.toString());
                obj.put("added", System.currentTimeMillis());
                videos.put(obj);
                saveVideos(activeProjectName, activeProjectStyle, videos);
                Toast.makeText(this, "تمت إضافة الفيديو للمشروع", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "تعذر حفظ الفيديو", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "الفيديو موجود بالفعل في المشروع", Toast.LENGTH_SHORT).show();
        }
        showVideos(activeProjectName, activeProjectStyle);
    }

    private String getDisplayName(Uri uri) {
        String result = "فيديو";
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) result = cursor.getString(index);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return result;
    }

    private String getDuration(Uri uri) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, uri);
            String raw = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
            if (raw == null) return "";
            long total = Long.parseLong(raw) / 1000L;
            long h = total / 3600;
            long m = (total % 3600) / 60;
            long s = total % 60;
            return h > 0 ? String.format(Locale.US, "%d:%02d:%02d", h, m, s) : String.format(Locale.US, "%02d:%02d", m, s);
        } catch (Exception e) {
            return "";
        } finally {
            try { retriever.release(); } catch (Exception ignored) {}
        }
    }

    private void showPlayer(String fileName, String uriString, String name, String style) {
        activeProjectName = name;
        activeProjectStyle = style;
        backAction = () -> showVideos(name, style);
        base("مشغل الفيديو", fileName);

        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(bg(Color.BLACK, 18));
        frame.setPadding(dp(4), dp(4), dp(4), dp(4));

        VideoView videoView = new VideoView(this);
        frame.addView(videoView, new FrameLayout.LayoutParams(-1, dp(230)));
        content.addView(frame, new LinearLayout.LayoutParams(-1, dp(238)));

        MediaController controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(Uri.parse(uriString));
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.start();
        });
        videoView.setOnErrorListener((mp, what, extra) -> {
            Toast.makeText(this, "تعذر تشغيل هذا الملف على الجهاز", Toast.LENGTH_LONG).show();
            return true;
        });

        TextView info = text("تشغيل فعلي من ملف الفيديو الأصلي على جهازك. التحليل الذكي لم يبدأ بعد؛ سيتم بناؤه في الدفعات القادمة.", 13, MUTED, false);
        LinearLayout.LayoutParams infoLp = new LinearLayout.LayoutParams(-1, -2);
        infoLp.setMargins(0, dp(16), 0, 0);
        content.addView(info, infoLp);
        addBack("رجوع للفيديوهات", () -> showVideos(name, style));
    }

    private void addBack(String label, Runnable action) {
        TextView b = text("‹  " + label, 15, GREEN, true);
        b.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        b.setPadding(0, dp(22), 0, dp(18));
        root.addView(b, new LinearLayout.LayoutParams(-1, -2));
        b.setOnClickListener(v -> action.run());
    }

    @Override public void onBackPressed() {
        if (backAction != null) backAction.run(); else showHome();
    }
}
