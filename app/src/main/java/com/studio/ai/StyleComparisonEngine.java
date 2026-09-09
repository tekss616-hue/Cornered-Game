package com.studio.ai;

import org.json.JSONArray;
import org.json.JSONObject;

public final class StyleComparisonEngine {
    private StyleComparisonEngine() {}

    public static JSONObject compare(JSONObject memory) throws Exception {
        JSONObject out = new JSONObject();
        JSONArray samples = memory.optJSONArray("samples");
        JSONObject summary = memory.optJSONObject("summary");
        int n = samples == null ? 0 : samples.length();
        out.put("sampleCount", n);
        out.put("ready", n >= 2);
        if (n == 0 || summary == null) return out;

        JSONArray mean = summary.optJSONArray("meanFingerprint");
        JSONArray ranked = new JSONArray();
        double simSum = 0;
        int simCount = 0;
        for (int i = 0; i < n; i++) {
            JSONObject sample = samples.optJSONObject(i);
            if (sample == null) continue;
            JSONObject profile = sample.optJSONObject("profile");
            JSONArray fp = profile == null ? null : profile.optJSONArray("fingerprint");
            double similarity = similarity(fp, mean);
            JSONObject item = new JSONObject();
            item.put("project", sample.optString("project", ""));
            item.put("video", sample.optString("video", "فيديو"));
            item.put("uri", sample.optString("uri", ""));
            item.put("similarity", round(similarity));
            item.put("distance", round(100.0 - similarity));
            ranked.put(item);
            simSum += similarity;
            simCount++;
        }
        sortDescending(ranked);
        out.put("videos", ranked);
        out.put("averageSimilarity", round(simCount == 0 ? 0 : simSum / simCount));
        out.put("cohesion", cohesion(simCount == 0 ? 0 : simSum / simCount));

        JSONObject repeated = new JSONObject();
        repeated.put("pace", summary.optString("dominantPace", ""));
        repeated.put("tone", summary.optString("dominantTone", ""));
        repeated.put("cutsPerMinute", summary.optDouble("avgCutsPerMinute", 0));
        repeated.put("averageShotSeconds", summary.optDouble("avgShotSeconds", 0));
        repeated.put("motion", summary.optDouble("avgMotion", 0));
        repeated.put("visualChange", summary.optDouble("avgVisualChange", 0));
        repeated.put("brightness", summary.optDouble("avgBrightness", 0));
        repeated.put("colorRange", summary.optDouble("avgColorRange", 0));
        repeated.put("activeRatio", summary.optDouble("avgActiveRatio", 0));
        out.put("learnedPattern", repeated);

        if (ranked.length() > 0) {
            out.put("closestVideo", new JSONObject(ranked.getJSONObject(0).toString()));
            out.put("mostDifferentVideo", new JSONObject(ranked.getJSONObject(ranked.length()-1).toString()));
        }
        out.put("version", 8);
        out.put("computedAt", System.currentTimeMillis());
        return out;
    }

    private static double similarity(JSONArray a, JSONArray b) {
        if (a == null || b == null) return 0;
        int n = Math.min(a.length(), b.length());
        if (n == 0) return 0;
        double sumSq = 0;
        for (int i=0;i<n;i++) {
            double d = a.optDouble(i,0)-b.optDouble(i,0);
            sumSq += d*d;
        }
        double normalizedDistance = Math.sqrt(sumSq / n);
        return clamp((1.0-normalizedDistance)*100.0, 0, 100);
    }

    private static void sortDescending(JSONArray a) throws Exception {
        for (int i=0;i<a.length()-1;i++) for(int j=i+1;j<a.length();j++) {
            if (a.getJSONObject(j).optDouble("similarity") > a.getJSONObject(i).optDouble("similarity")) {
                JSONObject tmp = a.getJSONObject(i);
                a.put(i, a.getJSONObject(j));
                a.put(j, tmp);
            }
        }
    }

    private static String cohesion(double s) {
        if (s >= 88) return "very_high";
        if (s >= 76) return "high";
        if (s >= 62) return "medium";
        return "low";
    }
    private static double clamp(double v,double min,double max){return Math.max(min,Math.min(max,v));}
    private static double round(double v){return Math.round(v*100.0)/100.0;}
}
