package com.studio.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public final class VideoInspector {
    private VideoInspector() {}

    public static JSONObject inspect(Context context, Uri uri) throws Exception {
        MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(context, uri);
            JSONObject out = new JSONObject();
            long durationMs = number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int width = (int) number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = (int) number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            int rotation = (int) number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            long bitrate = number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            String mime = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            String fps = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            String hasAudio = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            String hasVideo = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);

            out.put("durationMs", durationMs);
            out.put("width", width);
            out.put("height", height);
            out.put("rotation", rotation);
            out.put("bitrate", bitrate);
            out.put("mime", mime == null ? "" : mime);
            out.put("fps", fps == null ? "" : fps);
            out.put("hasAudio", "yes".equalsIgnoreCase(hasAudio));
            out.put("hasVideo", "yes".equalsIgnoreCase(hasVideo));
            out.put("inspectedAt", System.currentTimeMillis());

            VisualResult visual = analyzeVisualTimeline(r, durationMs);
            out.put("samples", visual.samples);
            out.put("visualTimeline", visual.timeline);
            out.put("cutCandidates", visual.cuts);
            out.put("segments", visual.segments);
            out.put("averageChange", visual.averageChange);
            out.put("averageMotion", visual.averageMotion);
            out.put("cutThreshold", visual.cutThreshold);
            out.put("analysisStepMs", visual.stepMs);
            out.put("analysisVersion", 4);
            return out;
        } finally {
            try { r.release(); } catch (Exception ignored) {}
        }
    }

    private static VisualResult analyzeVisualTimeline(MediaMetadataRetriever r, long durationMs) throws Exception {
        VisualResult result = new VisualResult();
        if (durationMs <= 0) return result;

        // Keep analysis practical on-phone: 0.35s–1.5s spacing, capped around 140 frames.
        long step = Math.max(350L, durationMs / 140L);
        step = Math.min(1500L, step);
        result.stepMs = step;

        List<FrameVector> frames = new ArrayList<>();
        JSONArray samples = new JSONArray();
        JSONArray timeline = new JSONArray();

        for (long t = 0; t < durationMs; t += step) {
            Bitmap source = null;
            try {
                source = r.getFrameAtTime(t * 1000L, MediaMetadataRetriever.OPTION_CLOSEST);
                if (source == null) continue;
                FrameVector fv = vector(source, t);
                frames.add(fv);

                JSONObject sample = new JSONObject();
                sample.put("timeMs", t);
                sample.put("brightness", fv.brightness);
                sample.put("colorRange", fv.colorRange);
                samples.put(sample);
            } finally {
                if (source != null && !source.isRecycled()) source.recycle();
            }
        }

        if (frames.isEmpty()) {
            result.samples = samples;
            return result;
        }

        double[] scores = new double[Math.max(0, frames.size() - 1)];
        double sumChange = 0;
        double sumMotion = 0;
        for (int i = 0; i < frames.size(); i++) {
            FrameVector current = frames.get(i);
            double change = 0;
            double motion = 0;
            if (i > 0) {
                FrameVector previous = frames.get(i - 1);
                motion = meanAbsDiff(previous.luma, current.luma);
                double hist = histogramDistance(previous.hist, current.hist);
                double brightJump = Math.abs(current.brightness - previous.brightness);
                change = clamp(motion * 0.58 + hist * 0.32 + brightJump * 0.10, 0, 100);
                scores[i - 1] = change;
                sumChange += change;
                sumMotion += motion;
            }

            JSONObject point = new JSONObject();
            point.put("timeMs", current.timeMs);
            point.put("brightness", current.brightness);
            point.put("colorRange", current.colorRange);
            point.put("motion", round1(motion));
            point.put("change", round1(change));
            timeline.put(point);
        }

        double mean = scores.length == 0 ? 0 : sumChange / scores.length;
        double variance = 0;
        for (double v : scores) variance += (v - mean) * (v - mean);
        double std = scores.length == 0 ? 0 : Math.sqrt(variance / scores.length);
        double threshold = Math.max(26.0, Math.min(62.0, mean + 1.55 * std));

        JSONArray cuts = new JSONArray();
        long lastCut = -5000;
        for (int i = 0; i < scores.length; i++) {
            double score = scores[i];
            long time = frames.get(i + 1).timeMs;
            if (score >= threshold && time - lastCut >= Math.max(500, step)) {
                JSONObject cut = new JSONObject();
                cut.put("timeMs", time);
                cut.put("score", round1(score));
                cut.put("confidence", confidence(score, threshold));
                cuts.put(cut);
                lastCut = time;
            }
        }

        JSONArray segments = new JSONArray();
        long start = 0;
        for (int i = 0; i < cuts.length(); i++) {
            long end = cuts.getJSONObject(i).getLong("timeMs");
            if (end > start) segments.put(segment(start, end));
            start = end;
        }
        if (durationMs > start) segments.put(segment(start, durationMs));

        result.samples = samples;
        result.timeline = timeline;
        result.cuts = cuts;
        result.segments = segments;
        result.averageChange = round1(scores.length == 0 ? 0 : sumChange / scores.length);
        result.averageMotion = round1(scores.length == 0 ? 0 : sumMotion / scores.length);
        result.cutThreshold = round1(threshold);
        return result;
    }

    private static JSONObject segment(long start, long end) throws Exception {
        JSONObject o = new JSONObject();
        o.put("startMs", start);
        o.put("endMs", end);
        o.put("durationMs", Math.max(0, end - start));
        return o;
    }

    private static FrameVector vector(Bitmap source, long timeMs) {
        Bitmap b = Bitmap.createScaledBitmap(source, 32, 18, true);
        int n = b.getWidth() * b.getHeight();
        int[] px = new int[n];
        b.getPixels(px, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        b.recycle();

        FrameVector f = new FrameVector();
        f.timeMs = timeMs;
        f.luma = new int[n];
        f.hist = new int[16];
        long lumTotal = 0;
        long rangeTotal = 0;
        for (int i = 0; i < n; i++) {
            int c = px[i];
            int rr = (c >> 16) & 255;
            int g = (c >> 8) & 255;
            int bl = c & 255;
            int y = (299 * rr + 587 * g + 114 * bl) / 1000;
            f.luma[i] = y;
            f.hist[Math.min(15, y / 16)]++;
            lumTotal += y;
            rangeTotal += Math.max(rr, Math.max(g, bl)) - Math.min(rr, Math.min(g, bl));
        }
        f.brightness = n == 0 ? 0 : (int)(lumTotal / n);
        f.colorRange = n == 0 ? 0 : (int)(rangeTotal / n);
        return f;
    }

    private static double meanAbsDiff(int[] a, int[] b) {
        if (a == null || b == null || a.length == 0 || a.length != b.length) return 0;
        long sum = 0;
        for (int i = 0; i < a.length; i++) sum += Math.abs(a[i] - b[i]);
        return (sum / (double)a.length) * 100.0 / 255.0;
    }

    private static double histogramDistance(int[] a, int[] b) {
        if (a == null || b == null || a.length != b.length) return 0;
        int totalA = 0, totalB = 0;
        for (int v : a) totalA += v;
        for (int v : b) totalB += v;
        if (totalA == 0 || totalB == 0) return 0;
        double diff = 0;
        for (int i = 0; i < a.length; i++) {
            diff += Math.abs(a[i] / (double)totalA - b[i] / (double)totalB);
        }
        return Math.min(100, diff * 50.0);
    }

    private static String confidence(double score, double threshold) {
        if (score >= threshold + 16) return "high";
        if (score >= threshold + 7) return "medium";
        return "low";
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private static long number(String s) {
        try { return s == null ? 0L : Long.parseLong(s); } catch (Exception e) { return 0L; }
    }

    private static final class FrameVector {
        long timeMs;
        int brightness;
        int colorRange;
        int[] luma;
        int[] hist;
    }

    private static final class VisualResult {
        JSONArray samples = new JSONArray();
        JSONArray timeline = new JSONArray();
        JSONArray cuts = new JSONArray();
        JSONArray segments = new JSONArray();
        double averageChange = 0;
        double averageMotion = 0;
        double cutThreshold = 0;
        long stepMs = 0;
    }
}
