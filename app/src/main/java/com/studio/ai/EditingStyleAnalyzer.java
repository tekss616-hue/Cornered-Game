package com.studio.ai;

import org.json.JSONArray;
import org.json.JSONObject;

public final class EditingStyleAnalyzer {
    private EditingStyleAnalyzer() {}

    public static JSONObject analyze(JSONObject inspection) throws Exception {
        JSONObject out = new JSONObject();
        long duration = inspection.optLong("durationMs", 0);
        JSONArray cuts = inspection.optJSONArray("cutCandidates");
        JSONArray segments = inspection.optJSONArray("segments");
        JSONArray timeline = inspection.optJSONArray("visualTimeline");
        int cutCount = cuts == null ? 0 : cuts.length();
        int segmentCount = segments == null ? 0 : segments.length();

        double avgShot = 0, minShot = 0, maxShot = 0, variance = 0;
        if (segments != null && segments.length() > 0) {
            double sum = 0;
            minShot = Double.MAX_VALUE;
            for (int i=0;i<segments.length();i++) {
                double d = segments.optJSONObject(i).optLong("durationMs",0) / 1000.0;
                sum += d; minShot = Math.min(minShot,d); maxShot = Math.max(maxShot,d);
            }
            avgShot = sum / segments.length();
            for (int i=0;i<segments.length();i++) {
                double d = segments.optJSONObject(i).optLong("durationMs",0) / 1000.0;
                variance += (d-avgShot)*(d-avgShot);
            }
            variance = Math.sqrt(variance / segments.length());
        }

        double cutsPerMinute = duration > 0 ? cutCount * 60000.0 / duration : 0;
        double avgMotion = inspection.optDouble("averageMotion",0);
        double avgChange = inspection.optDouble("averageChange",0);
        double brightness = average(timeline,"brightness");
        double colorRange = average(timeline,"colorRange");
        double activeRatio = activeRatio(timeline, avgMotion);

        String pace;
        if (cutsPerMinute >= 20 || (avgShot > 0 && avgShot < 3.0)) pace="fast";
        else if (cutsPerMinute >= 8 || (avgShot > 0 && avgShot < 7.0)) pace="medium";
        else pace="slow";

        String motion = avgMotion >= 18 ? "high" : avgMotion >= 8 ? "medium" : "low";
        String visualEnergy = avgChange >= 25 ? "high" : avgChange >= 12 ? "medium" : "low";
        String tone = brightness < 75 ? "dark" : brightness > 175 ? "bright" : "balanced";
        String color = colorRange < 22 ? "muted" : colorRange > 58 ? "vivid" : "balanced";
        String rhythm = variance > Math.max(2.5, avgShot * .75) ? "variable" : "steady";

        out.put("version",5);
        out.put("createdAt",System.currentTimeMillis());
        out.put("cutCount",cutCount);
        out.put("segmentCount",segmentCount);
        out.put("cutsPerMinute",round(cutsPerMinute));
        out.put("averageShotSeconds",round(avgShot));
        out.put("shortestShotSeconds",round(minShot == Double.MAX_VALUE ? 0 : minShot));
        out.put("longestShotSeconds",round(maxShot));
        out.put("shotLengthVariation",round(variance));
        out.put("averageMotion",round(avgMotion));
        out.put("averageVisualChange",round(avgChange));
        out.put("averageBrightness",round(brightness));
        out.put("averageColorRange",round(colorRange));
        out.put("activeTimelineRatio",round(activeRatio));
        out.put("pace",pace);
        out.put("motionLevel",motion);
        out.put("visualEnergy",visualEnergy);
        out.put("tone",tone);
        out.put("colorCharacter",color);
        out.put("rhythm",rhythm);

        // A compact numeric fingerprint for later comparison/learning batches.
        JSONArray fingerprint = new JSONArray();
        fingerprint.put(round(norm(cutsPerMinute,0,30)));
        fingerprint.put(round(norm(avgShot,0,12)));
        fingerprint.put(round(norm(avgMotion,0,35)));
        fingerprint.put(round(norm(avgChange,0,50)));
        fingerprint.put(round(norm(brightness,0,255)));
        fingerprint.put(round(norm(colorRange,0,100)));
        fingerprint.put(round(activeRatio));
        fingerprint.put(round(norm(variance,0,10)));
        out.put("fingerprint",fingerprint);
        return out;
    }

    private static double average(JSONArray a,String key){
        if(a==null||a.length()==0)return 0; double s=0; int n=0;
        for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null){s+=o.optDouble(key,0);n++;}}
        return n==0?0:s/n;
    }
    private static double activeRatio(JSONArray a,double baseline){
        if(a==null||a.length()==0)return 0; int active=0,n=0; double threshold=Math.max(7,baseline*1.15);
        for(int i=0;i<a.length();i++){JSONObject o=a.optJSONObject(i);if(o!=null){if(o.optDouble("motion",0)>=threshold)active++;n++;}}
        return n==0?0:active/(double)n;
    }
    private static double norm(double v,double min,double max){return Math.max(0,Math.min(1,(v-min)/(max-min)));}
    private static double round(double v){return Math.round(v*100.0)/100.0;}
}
