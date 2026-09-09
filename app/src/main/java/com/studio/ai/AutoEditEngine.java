package com.studio.ai;

import org.json.*;
import java.util.*;

/** Builds an executable first-pass edit from measured scene data and learned category metrics. */
public final class AutoEditEngine {
    private AutoEditEngine() {}

    private static final class Shot {
        JSONObject clip; double motion, change, score; long start,end;
        Shot(JSONObject c,long s,long e,double m,double ch){clip=c;start=s;end=e;motion=m;change=ch;}
    }

    public static JSONObject build(JSONObject video, JSONObject categoryMemory) throws Exception {
        JSONObject inspection=video.optJSONObject("inspection");
        if(inspection==null) throw new IllegalStateException("حلل الفيديو أولًا قبل المونتاج التلقائي");
        JSONArray segments=inspection.optJSONArray("segments");
        if(segments==null||segments.length()==0) throw new IllegalStateException("التحليل لا يحتوي على مشاهد قابلة للمونتاج");
        JSONObject summary=categoryMemory==null?null:categoryMemory.optJSONObject("summary");
        int samples=summary==null?0:summary.optInt("sampleCount",0);
        double targetShot=samples>0?summary.optDouble("avgShotSeconds",2.5):2.5;
        double targetMotion=samples>0?summary.optDouble("avgMotion",50):50;
        targetShot=clamp(targetShot,0.6,8.0);

        ArrayList<Shot> shots=new ArrayList<>();
        for(int i=0;i<segments.length();i++){
            JSONObject s=segments.optJSONObject(i);if(s==null)continue;
            long start=ms(s,"startMs","start"),end=ms(s,"endMs","end");if(end-start<250)continue;
            double motion=first(s,"averageMotion","motion","motionScore");
            double change=first(s,"averageVisualChange","visualChange","changeScore");
            JSONObject c=new JSONObject();c.put("id","auto_"+i+"_"+start);c.put("sourceStartMs",start);c.put("sourceEndMs",end);c.put("enabled",true);c.put("speed",1.0);c.put("note","AI Auto Edit");
            Shot sh=new Shot(c,start,end,motion,change);
            double length=(end-start)/1000.0;
            double lengthFit=1.0-Math.min(1.0,Math.abs(length-targetShot)/Math.max(targetShot,0.5));
            double motionFit=1.0-Math.min(1.0,Math.abs(motion-targetMotion)/100.0);
            sh.score=.55*motionFit+.30*lengthFit+.15*Math.min(1.0,change/100.0);shots.add(sh);
        }
        if(shots.isEmpty())throw new IllegalStateException("لم أجد لقطات صالحة للمونتاج");

        ArrayList<Double> scores=new ArrayList<>();for(Shot s:shots)scores.add(s.score);Collections.sort(scores);
        double threshold=scores.get(Math.max(0,(int)Math.floor((scores.size()-1)*.25)));
        JSONArray clips=new JSONArray();int excluded=0,trimmed=0;
        for(Shot sh:shots){
            long len=sh.end-sh.start;
            if(shots.size()>3&&sh.score<threshold){sh.clip.put("enabled",false);sh.clip.put("note","AI Auto Edit • منخفض الملاءمة للبصمة");excluded++;}
            else if(len>Math.round(targetShot*1600)){long desired=Math.round(targetShot*1000);long center=(sh.start+sh.end)/2;long ns=Math.max(sh.start,center-desired/2),ne=Math.min(sh.end,ns+desired);if(ne-ns>=300){sh.clip.put("sourceStartMs",ns);sh.clip.put("sourceEndMs",ne);sh.clip.put("note","AI Auto Edit • تقصير نحو إيقاع القسم");trimmed++;}}
            clips.put(sh.clip);
        }
        JSONObject out=new JSONObject();out.put("version",16);out.put("mode","automatic-first-pass");out.put("videoName",video.optString("name","فيديو"));out.put("videoUri",video.optString("uri",""));out.put("clips",clips);out.put("updatedAt",System.currentTimeMillis());
        JSONObject report=new JSONObject();report.put("memorySamples",samples);report.put("targetShotSeconds",round(targetShot));report.put("targetMotion",round(targetMotion));report.put("sceneCount",shots.size());report.put("excludedScenes",excluded);report.put("trimmedScenes",trimmed);report.put("usesLearnedCategory",samples>0);out.put("autoEditReport",report);
        return out;
    }

    private static long ms(JSONObject o,String ms,String seconds){if(o.has(ms))return o.optLong(ms);double x=o.optDouble(seconds,0);return x<10000?Math.round(x*1000):Math.round(x);}
    private static double first(JSONObject o,String... keys){for(String k:keys)if(o.has(k))return o.optDouble(k,0);return 0;}
    private static double clamp(double x,double a,double b){return Math.max(a,Math.min(b,x));}
    private static double round(double x){return Math.round(x*100.0)/100.0;}
}
