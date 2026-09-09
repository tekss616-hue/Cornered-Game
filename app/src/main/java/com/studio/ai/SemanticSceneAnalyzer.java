package com.studio.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class SemanticSceneAnalyzer {
    public interface Progress { void onProgress(int percent, String stage); }
    private static final int MAX_SCENES = 12;
    private static final int MAX_EDGE = 720;
    private SemanticSceneAnalyzer() {}

    public static JSONObject analyze(Context context, Uri uri, JSONObject inspection, String project, String style, Progress progress) throws Exception {
        if (AiEndpoints.SEMANTIC_SCENE_ENDPOINT == null || AiEndpoints.SEMANTIC_SCENE_ENDPOINT.trim().isEmpty()) {
            throw new IllegalStateException("لم يتم ربط خادم الدفعة 17 بعد");
        }
        JSONArray segments = inspection == null ? null : inspection.optJSONArray("segments");
        if (segments == null || segments.length() == 0) throw new IllegalStateException("لا توجد مشاهد محللة محليًا");

        JSONObject out = new JSONObject();
        out.put("version", 17);
        out.put("mode", "cloud-vision");
        out.put("generatedAt", System.currentTimeMillis());
        out.put("sourceSceneCount", segments.length());
        out.put("maxScenesPerRun", MAX_SCENES);
        out.put("capped", segments.length() > MAX_SCENES);
        JSONArray scenes = new JSONArray();
        out.put("scenes", scenes);

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(context, uri);
            int count = Math.min(MAX_SCENES, segments.length());
            for (int i = 0; i < count; i++) {
                JSONObject seg = segments.optJSONObject(i);
                if (seg == null) continue;
                long start = seg.optLong("startMs");
                long end = seg.optLong("endMs");
                if (end <= start) continue;
                notify(progress, (int) Math.round((i * 100.0) / Math.max(1, count)), "تجهيز المشهد " + (i + 1) + " من " + count);

                JSONArray images = new JSONArray();
                long duration = end - start;
                long[] times = duration < 1400
                        ? new long[]{start + duration / 2}
                        : new long[]{start + duration / 3, start + (duration * 2) / 3};
                for (long t : times) {
                    Bitmap frame = retriever.getFrameAtTime(t * 1000L, MediaMetadataRetriever.OPTION_CLOSEST);
                    if (frame == null) continue;
                    Bitmap scaled = scale(frame);
                    images.put(toDataUrl(scaled));
                    if (scaled != frame && !scaled.isRecycled()) scaled.recycle();
                    if (!frame.isRecycled()) frame.recycle();
                }
                if (images.length() == 0) continue;

                notify(progress, (int) Math.round(((i + 0.35) * 100.0) / Math.max(1, count)), "إرسال المشهد للرؤية السحابية");
                JSONObject scene = requestScene(project, style, start, end, images);
                scene.put("startMs", start);
                scene.put("endMs", end);
                scene.put("durationMs", end - start);
                scenes.put(scene);
                notify(progress, (int) Math.round(((i + 1.0) * 100.0) / Math.max(1, count)), "اكتمل فهم المشهد " + (i + 1));
            }
        } finally {
            try { retriever.release(); } catch (Exception ignored) {}
        }
        out.put("analyzedSceneCount", scenes.length());
        return out;
    }

    private static JSONObject requestScene(String project, String style, long start, long end, JSONArray images) throws Exception {
        JSONObject body = new JSONObject();
        body.put("action", "analyze_scene");
        body.put("project", project);
        body.put("style", style);
        body.put("startMs", start);
        body.put("endMs", end);
        body.put("images", images);

        HttpURLConnection c = (HttpURLConnection) new URL(AiEndpoints.SEMANTIC_SCENE_ENDPOINT).openConnection();
        c.setRequestMethod("POST");
        c.setConnectTimeout(20000);
        c.setReadTimeout(90000);
        c.setDoOutput(true);
        c.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream os = c.getOutputStream()) { os.write(payload); }

        int code = c.getResponseCode();
        InputStream input = code >= 200 && code < 300 ? c.getInputStream() : c.getErrorStream();
        StringBuilder raw = new StringBuilder();
        if (input != null) {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line; while ((line = r.readLine()) != null) raw.append(line);
            }
        }
        c.disconnect();
        JSONObject response = raw.length() == 0 ? new JSONObject() : new JSONObject(raw.toString());
        if (code < 200 || code >= 300 || !response.optBoolean("ok")) {
            throw new Exception(response.optString("error", "فشل تحليل المشهد سحابيًا"));
        }
        JSONObject scene = response.optJSONObject("scene");
        if (scene == null) throw new Exception("استجابة المشهد غير صالحة");
        return scene;
    }

    private static Bitmap scale(Bitmap source) {
        int w = source.getWidth(), h = source.getHeight();
        int max = Math.max(w, h);
        if (max <= MAX_EDGE) return source;
        float ratio = MAX_EDGE / (float) max;
        return Bitmap.createScaledBitmap(source, Math.max(1, Math.round(w * ratio)), Math.max(1, Math.round(h * ratio)), true);
    }

    private static String toDataUrl(Bitmap b) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (!b.compress(Bitmap.CompressFormat.JPEG, 68, out)) throw new Exception("تعذر ضغط إطار المشهد");
        return "data:image/jpeg;base64," + Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP);
    }

    private static void notify(Progress p, int percent, String stage) {
        if (p != null) p.onProgress(Math.max(0, Math.min(100, percent)), stage);
    }
}
