package com.studio.ai;

import android.content.SharedPreferences;
import org.json.*;
import java.util.*;

public final class EffectLibrary {
    private EffectLibrary() {}

    private static String key(String style) {
        return "fxprefs_" + Integer.toHexString(style.hashCode());
    }

    public static JSONArray catalog(String style) {
        JSONArray out = new JSONArray();
        if ("رعب".equals(style)) {
            add(out,"horror_flicker","effect","وميض خافت","وميض إضاءة قصير وغير منتظم للحظات التوتر","low","visual");
            add(out,"horror_vignette","effect","تعتيم الحواف","خفض تدريجي للحواف لتركيز النظر","low","visual");
            add(out,"horror_noise","effect","ضوضاء فيلم خفيفة","نسيج حبيبي خفيف بدون تشويه الصورة","low","visual");
            add(out,"horror_pulse","effect","نبضة تكبير","تكبير/رجوع قصير مع لحظة صدمة","medium","motion");
            add(out,"horror_low_rumble","effect","دوي منخفض","طبقة صوتية منخفضة للحظات الترقب","medium","audio");
            add(out,"horror_breath_hit","effect","نَفَس/ضربة صوتية","تأكيد لحظة خوف بصوت قصير","medium","audio");
            add(out,"horror_dip_black","transition","غمس إلى السواد","انتقال سريع إلى الأسود ثم اللقطة التالية","low","transition");
            add(out,"horror_flash_cut","transition","قطع بوميض","وميض قصير عند الانتقال للحظة حادة","medium","transition");
            add(out,"horror_blur_snap","transition","ضبابية خاطفة","طمس قصير جدًا بين لقطتين","medium","transition");
            add(out,"horror_hard_cut","transition","قطع حاد","قطع مباشر بلا تزويق للحفاظ على التوتر","low","transition");
        } else if ("أكشن".equals(style)) {
            add(out,"action_impact_shake","effect","اهتزاز ضربة","اهتزاز قصير متزامن مع الاصطدام","medium","motion");
            add(out,"action_speed_ramp","effect","تسارع حركة","تغيير سرعة قصير قبل/بعد لحظة مهمة","high","motion");
            add(out,"action_punch_zoom","effect","Punch Zoom","تكبير سريع لتأكيد ضربة أو حركة","medium","motion");
            add(out,"action_whoosh","effect","Whoosh","صوت حركة قصير مع انتقال سريع","low","audio");
            add(out,"action_impact","effect","Impact","ضربة صوتية قصيرة وقوية","low","audio");
            add(out,"action_motion_blur","effect","طمس حركة","طمس محدود أثناء الحركة السريعة","medium","visual");
            add(out,"action_whip","transition","Whip Pan","انتقال اتجاهي سريع","high","transition");
            add(out,"action_match_motion","transition","مطابقة الحركة","ربط لقطتين بحركة متشابهة","medium","transition");
            add(out,"action_flash","transition","Flash","فلاش قصير بين لقطتين","medium","transition");
            add(out,"action_hard_cut","transition","قطع على الضربة","قطع مباشر عند ذروة الحركة","low","transition");
        } else if ("إعلانات الشركات".equals(style)) {
            add(out,"ad_clean_zoom","effect","تكبير نظيف","تكبير بسيط لإبراز المنتج أو النص","low","motion");
            add(out,"ad_soft_shadow","effect","ظل نص ناعم","فصل النص عن الخلفية بشكل نظيف","low","visual");
            add(out,"ad_highlight","effect","إبراز عنصر","زيادة تركيز خفيفة على المنتج/الشعار","low","visual");
            add(out,"ad_click","effect","نقرة UI","صوت خفيف مناسب للواجهة أو النص","low","audio");
            add(out,"ad_soft_whoosh","effect","Whoosh ناعم","صوت حركة خفيف بدون مبالغة","low","audio");
            add(out,"ad_text_slide","effect","حركة نص نظيفة","دخول/خروج نص قصير ومنضبط","low","motion");
            add(out,"ad_dissolve","transition","Dissolve نظيف","مزج قصير وهادئ","low","transition");
            add(out,"ad_slide","transition","Slide","انتقال اتجاهي بسيط","low","transition");
            add(out,"ad_match_cut","transition","Match Cut","ربط بصري بين شكلين أو حركتين","medium","transition");
            add(out,"ad_hard_cut","transition","قطع نظيف","قطع مباشر عندما يكون أسرع وأوضح","low","transition");
        } else if ("تجمعيات أنمي".equals(style)) {
            add(out,"anime_impact_zoom","effect","تكبير ضربة","تكبير سريع على لحظة قوة","medium","motion");
            add(out,"anime_shake","effect","اهتزاز مضبوط","اهتزاز قصير في اللقطات القوية","medium","motion");
            add(out,"anime_glow","effect","Glow خفيف","توهج محدود حول اللقطات المضيئة","low","visual");
            add(out,"anime_flash","effect","فلاش إيقاعي","فلاش قصير على النبضات أو الضربات","medium","visual");
            add(out,"anime_whoosh","effect","Whoosh","صوت حركة مختصر","low","audio");
            add(out,"anime_impact","effect","Impact","تعزيز صوتي للحظات الذروة","low","audio");
            add(out,"anime_hard_cut","transition","قطع إيقاعي","قطع مباشر على النبضة","low","transition");
            add(out,"anime_whip","transition","Whip","انتقال سريع باتجاه الحركة","high","transition");
            add(out,"anime_flash_cut","transition","Flash Cut","فلاش قصير بين لقطتين","medium","transition");
            add(out,"anime_zoom_match","transition","Zoom Match","مطابقة تكبير بين لقطتين","medium","transition");
        } else if ("TikTok".equals(style)) {
            add(out,"tt_punch_zoom","effect","Punch Zoom","تكبير قصير لإبقاء الانتباه","medium","motion");
            add(out,"tt_caption_pop","effect","Pop للنص","حركة نص قصيرة وواضحة","low","motion");
            add(out,"tt_ui_click","effect","نقرة","صوت خفيف للنصوص/الأزرار","low","audio");
            add(out,"tt_whoosh","effect","Whoosh","تعزيز انتقال سريع","low","audio");
            add(out,"tt_freeze_emphasis","effect","تأكيد لحظة","تثبيت قصير فقط عند الحاجة مع عنصر بصري","medium","motion");
            add(out,"tt_crop_push","effect","دفع الكادر","تحريك/تكبير بسيط داخل الكادر","medium","motion");
            add(out,"tt_hard_cut","transition","قطع سريع","قطع مباشر للحفاظ على الإيقاع","low","transition");
            add(out,"tt_swipe","transition","Swipe","انتقال اتجاهي قصير","medium","transition");
            add(out,"tt_zoom","transition","Zoom","انتقال تكبير سريع","medium","transition");
            add(out,"tt_match","transition","Match Cut","مطابقة وضع/شكل بين لقطتين","medium","transition");
        } else if ("YouTube".equals(style)) {
            add(out,"yt_punch_zoom","effect","Punch Zoom","تكبير خفيف للتأكيد","low","motion");
            add(out,"yt_lower_third","effect","Lower Third","حركة عنوان سفلي نظيفة","low","motion");
            add(out,"yt_broll_push","effect","B-roll Push","تحريك بسيط للقطات التوضيحية","low","motion");
            add(out,"yt_click","effect","Click","صوت خفيف للإشارات والعناصر","low","audio");
            add(out,"yt_whoosh","effect","Whoosh خفيف","صوت انتقال غير مزعج","low","audio");
            add(out,"yt_highlight","effect","Highlight","إبراز نص/منطقة مهمة","low","visual");
            add(out,"yt_hard_cut","transition","قطع مباشر","الانتقال الأساسي للحوار والمحتوى","low","transition");
            add(out,"yt_dissolve","transition","Dissolve","مزج قصير عند تغير الجو","low","transition");
            add(out,"yt_slide","transition","Slide","انتقال بسيط للعناصر أو الأقسام","low","transition");
            add(out,"yt_match","transition","Match Cut","مطابقة بصرية بين مشهدين","medium","transition");
        } else {
            add(out,"general_zoom","effect","تكبير خفيف","تكبير قصير للتأكيد","low","motion");
            add(out,"general_vignette","effect","تعتيم حواف","تركيز بصري خفيف","low","visual");
            add(out,"general_whoosh","effect","Whoosh","صوت حركة قصير","low","audio");
            add(out,"general_impact","effect","Impact","تأكيد صوتي محدود","low","audio");
            add(out,"general_hard_cut","transition","قطع مباشر","انتقال نظيف وسريع","low","transition");
            add(out,"general_dissolve","transition","Dissolve","مزج خفيف بين لقطتين","low","transition");
            add(out,"general_slide","transition","Slide","انتقال اتجاهي بسيط","medium","transition");
            add(out,"general_match","transition","Match Cut","مطابقة شكل أو حركة","medium","transition");
        }
        return out;
    }

    private static void add(JSONArray a,String id,String type,String name,String description,String intensity,String family){
        try {
            JSONObject o=new JSONObject();
            o.put("id",id); o.put("type",type); o.put("name",name); o.put("description",description);
            o.put("intensity",intensity); o.put("family",family); a.put(o);
        } catch (JSONException ignored) {}
    }

    public static JSONObject preferences(SharedPreferences prefs,String style){
        try { return new JSONObject(prefs.getString(key(style),"{}")); }
        catch(Exception e){ return new JSONObject(); }
    }

    public static String state(SharedPreferences prefs,String style,String id){
        return preferences(prefs,style).optString(id,"neutral");
    }

    public static void setState(SharedPreferences prefs,String style,String id,String state){
        JSONObject p=preferences(prefs,style);
        try { p.put(id,state); prefs.edit().putString(key(style),p.toString()).apply(); }
        catch(JSONException ignored){}
    }

    public static JSONObject summary(SharedPreferences prefs,String style){
        JSONObject out=new JSONObject();
        JSONObject p=preferences(prefs,style);
        int liked=0,rejected=0,neutral=0;
        JSONArray cat=catalog(style);
        for(int i=0;i<cat.length();i++){
            JSONObject x=cat.optJSONObject(i); if(x==null) continue;
            String st=p.optString(x.optString("id"),"neutral");
            if("liked".equals(st)) liked++; else if("rejected".equals(st)) rejected++; else neutral++;
        }
        try { out.put("liked",liked); out.put("rejected",rejected); out.put("neutral",neutral); out.put("total",cat.length()); }
        catch(JSONException ignored){}
        return out;
    }

    public static JSONArray recommended(SharedPreferences prefs,String style,JSONObject learnedSummary){
        JSONArray src=catalog(style), out=new JSONArray();
        double motion=learnedSummary==null?0:learnedSummary.optDouble("avgMotion",0);
        String pace=learnedSummary==null?"":learnedSummary.optString("dominantPace","");
        List<JSONObject> items=new ArrayList<>();
        for(int i=0;i<src.length();i++){
            JSONObject x=src.optJSONObject(i); if(x==null) continue;
            if("rejected".equals(state(prefs,style,x.optString("id")))) continue;
            try {
                JSONObject copy=new JSONObject(x.toString());
                double score=50;
                if("liked".equals(state(prefs,style,x.optString("id")))) score+=35;
                String intensity=x.optString("intensity");
                if("fast".equals(pace)||motion>=28) {
                    if("medium".equals(intensity)||"high".equals(intensity)) score+=12;
                } else if("slow".equals(pace)||motion<15) {
                    if("low".equals(intensity)) score+=12;
                } else if("low".equals(intensity)||"medium".equals(intensity)) score+=8;
                copy.put("score",score);
                items.add(copy);
            } catch(Exception ignored){}
        }
        Collections.sort(items,(a,b)->Double.compare(b.optDouble("score"),a.optDouble("score")));
        for(JSONObject x:items) out.put(x);
        return out;
    }
}
