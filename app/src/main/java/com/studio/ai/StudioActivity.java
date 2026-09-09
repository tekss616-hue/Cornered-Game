package com.studio.ai;

import android.content.Intent;
import org.json.*;

public class StudioActivity extends MainActivity {
    @Override void workspace(String n,String s){
        super.workspace(n,s);
        JSONArray videos=list(n,s);int ready=0;
        for(int i=0;i<videos.length();i++){JSONObject v=videos.optJSONObject(i);if(v!=null&&v.optJSONObject("inspection")!=null)ready++;}
        final int count=ready;
        card("🎓 مراجع تعلّم الأسلوب","أنت تختار المرجع بنفسك • فيديو العميل لا يُفترض أنه مرجع",()->references(n,s));
        card("✂️ AI Editor",count==0?"حلل فيديو أولًا":count+" فيديو جاهز • مونتاج تلقائي • مراجعة • رندر",()->timelineVideos(n,s));
    }

    void references(String n,String s){
        project=n;style=s;base("مراجع تعلّم الأسلوب",s+" • تعلّم اختياري");
        card("قاعدة التعلم","اعتمد فقط الفيديو الذي تريد أن يتعلم التطبيق أسلوبه. اضغط المرجع مرة أخرى لإزالته من ذاكرة القسم.",null);
        JSONArray a=list(n,s);boolean any=false;
        for(int i=a.length()-1;i>=0;i--){JSONObject v=a.optJSONObject(i);if(v==null||v.optJSONObject("inspection")==null)continue;any=true;boolean learned=LongTermMemory.isLearned(prefs,s,v.optString("uri",""));card((learned?"✓ مرجع • ":"＋ اعتماد كمرجع • ")+v.optString("name","فيديو"),learned?"موجود في ذاكرة القسم — اضغط لإلغاء التعلم":"محلل فقط — لن يدخل ذاكرة الأسلوب حتى تعتمده",()->{String uri=v.optString("uri","");if(LongTermMemory.isLearned(prefs,s,uri))LongTermMemory.unlearn(prefs,s,uri);else LongTermMemory.learn(prefs,n,s,v);references(n,s);});}
        if(!any)card("لا توجد فيديوهات محللة","استورد وحلل فيديو أولًا.",null);
        back("المشروع",()->workspace(n,s));
    }

    void timelineVideos(String n,String s){
        project=n;style=s;base("اختر فيديو للـ AI Editor",n+" • "+s);JSONArray videos=list(n,s);boolean any=false;
        for(int i=videos.length()-1;i>=0;i--){JSONObject v=videos.optJSONObject(i);if(v==null||v.optJSONObject("inspection")==null)continue;any=true;JSONObject timeline=EditTimelineStore.load(prefs,n,s,v);JSONArray clips=timeline.optJSONArray("clips");String sub=(clips==null?0:clips.length())+" لقطة • مدة الخطة "+time(EditTimelineStore.duration(timeline));card("✂️ "+v.optString("name","فيديو"),sub,()->openTimeline(n,s,v));}
        if(!any)card("لا يوجد فيديو جاهز","استورد فيديو ودع التحليل يكتمل أولًا.",null);back("المشروع",()->workspace(n,s));
    }
    void openTimeline(String n,String s,JSONObject v){Intent i=new Intent(this,TimelineActivity.class);i.putExtra("project",n);i.putExtra("style",s);i.putExtra("video",v.toString());startActivity(i);}
    String time(long ms){long sec=Math.max(0,ms)/1000;return String.format(java.util.Locale.US,"%d:%02d",sec/60,sec%60);}
}
