package com.studio.ai;

import org.json.JSONArray;
import org.json.JSONObject;

public final class EditingSuggestionEngine {
    private EditingSuggestionEngine() {}

    public static JSONObject build(JSONObject inspection, JSONObject categoryMemory) throws Exception {
        JSONObject out=new JSONObject();
        JSONObject current=inspection==null?null:inspection.optJSONObject("editingStyle");
        JSONObject learned=categoryMemory==null?null:categoryMemory.optJSONObject("summary");
        int samples=learned==null?0:learned.optInt("sampleCount",0);
        out.put("version",9);out.put("sampleCount",samples);out.put("ready",current!=null&&samples>0);
        JSONArray actions=new JSONArray();
        if(current==null){out.put("message","يحتاج الفيديو إلى بصمة مونتاج أولًا.");out.put("actions",actions);return out;}
        if(samples==0){out.put("message","لا توجد عينات متعلمة في هذا القسم بعد.");out.put("actions",actions);return out;}

        double curCuts=current.optDouble("cutsPerMinute"), targetCuts=learned.optDouble("avgCutsPerMinute");
        double curShot=current.optDouble("averageShotSeconds"), targetShot=learned.optDouble("avgShotSeconds");
        double curMotion=current.optDouble("averageMotion"), targetMotion=learned.optDouble("avgMotion");
        double curChange=current.optDouble("averageVisualChange"), targetChange=learned.optDouble("avgVisualChange");
        double curBright=current.optDouble("averageBrightness"), targetBright=learned.optDouble("avgBrightness");
        double curColor=current.optDouble("averageColorRange"), targetColor=learned.optDouble("avgColorRange");
        double curActive=current.optDouble("activeTimelineRatio"), targetActive=learned.optDouble("avgActiveRatio");

        addTarget(actions,"كثافة القص","قصات/دقيقة",curCuts,targetCuts,Math.max(1.5,targetCuts*.12),"زد كثافة القص تدريجيًا","خفّض كثافة القص واترك اللقطات تتنفس");
        addTarget(actions,"طول اللقطة","ثانية",curShot,targetShot,Math.max(.35,targetShot*.15),"استخدم لقطات أطول قليلًا","قصّر متوسط اللقطات");
        addTarget(actions,"الحركة البصرية","/100",curMotion,targetMotion,Math.max(2.0,targetMotion*.18),"اختر مناطق أكثر حركة أو زد سرعة الإيقاع البصري","خفف الاعتماد على المناطق كثيرة الحركة");
        addTarget(actions,"التغير البصري","/100",curChange,targetChange,Math.max(2.0,targetChange*.18),"ارفع معدل التغير بين اللقطات","خفف التغيرات الحادة بين اللقطات");
        addTarget(actions,"الإضاءة","/255",curBright,targetBright,10,"ارفع السطوع العام نحو بصمة القسم","اخفض السطوع العام نحو بصمة القسم");
        addTarget(actions,"المدى اللوني","/100",curColor,targetColor,6,"استخدم لقطات ذات تنوع لوني أكبر","قلل التنوع اللوني للوصول لطابع القسم");
        addTarget(actions,"المناطق النشطة","%",curActive*100,targetActive*100,8,"زد نسبة المقاطع النشطة","زد المساحات الهادئة بين المقاطع النشطة");

        JSONArray curFp=current.optJSONArray("fingerprint"), meanFp=learned.optJSONArray("meanFingerprint");
        double similarity=similarity(curFp,meanFp);out.put("similarityToLearnedStyle",round(similarity));
        out.put("targetCutsPerMinute",round(targetCuts));out.put("targetAverageShotSeconds",round(targetShot));out.put("targetMotion",round(targetMotion));out.put("targetBrightness",round(targetBright));
        out.put("dominantPace",learned.optString("dominantPace",""));out.put("dominantTone",learned.optString("dominantTone",""));out.put("actions",actions);
        out.put("message",actions.length()==0?"الفيديو قريب من النطاق العددي المتعلم لهذا القسم.":"هذه تعديلات قياسية للوصول أقرب إلى بصمة القسم المتعلمة.");
        return out;
    }

    private static void addTarget(JSONArray a,String metric,String unit,double current,double target,double tolerance,String whenLow,String whenHigh)throws Exception{
        double d=current-target;if(Math.abs(d)<=tolerance)return;JSONObject x=new JSONObject();x.put("metric",metric);x.put("current",round(current));x.put("target",round(target));x.put("unit",unit);x.put("direction",d<0?"increase":"decrease");x.put("action",d<0?whenLow:whenHigh);x.put("gap",round(Math.abs(d)));a.put(x);
    }
    private static double similarity(JSONArray a,JSONArray b){if(a==null||b==null)return 0;int n=Math.min(a.length(),b.length());if(n==0)return 0;double s=0;for(int i=0;i<n;i++){double d=a.optDouble(i)-b.optDouble(i);s+=d*d;}return Math.max(0,Math.min(100,(1-Math.sqrt(s/n))*100));}
    private static double round(double v){return Math.round(v*100.0)/100.0;}
}
