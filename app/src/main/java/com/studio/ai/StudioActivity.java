package com.studio.ai;

import android.content.Intent;
import org.json.*;

public class StudioActivity extends MainActivity {
    @Override void workspace(String n,String s){
        super.workspace(n,s);
        JSONArray videos=list(n,s);
        int ready=0;
        for(int i=0;i<videos.length();i++){
            JSONObject v=videos.optJSONObject(i);
            if(v!=null&&v.optJSONObject("inspection")!=null)ready++;
        }
        final int count=ready;
        card("✂️ التايملاين القابل للتحرير",count==0?"حلل فيديو أولًا لبناء خطة اللقطات":count+" فيديو جاهز للتحرير • قص • تقسيم • ترتيب • سرعة",()->timelineVideos(n,s));
    }

    void timelineVideos(String n,String s){
        project=n;style=s;base("اختر فيديو للتايملاين",n+" • "+s);
        JSONArray videos=list(n,s);boolean any=false;
        for(int i=videos.length()-1;i>=0;i--){
            JSONObject v=videos.optJSONObject(i);if(v==null||v.optJSONObject("inspection")==null)continue;any=true;
            JSONObject timeline=EditTimelineStore.load(prefs,n,s,v);
            JSONArray clips=timeline.optJSONArray("clips");
            String sub=(clips==null?0:clips.length())+" لقطة • مدة الخطة "+time(EditTimelineStore.duration(timeline));
            card("✂️ "+v.optString("name","فيديو"),sub,()->openTimeline(n,s,v));
        }
        if(!any)card("لا يوجد فيديو جاهز","استورد فيديو ودع التحليل يكتمل أولًا.",null);
        back("المشروع",()->workspace(n,s));
    }

    void openTimeline(String n,String s,JSONObject v){
        Intent i=new Intent(this,TimelineActivity.class);
        i.putExtra("project",n);i.putExtra("style",s);i.putExtra("video",v.toString());
        startActivity(i);
    }

    String time(long ms){long sec=Math.max(0,ms)/1000;return String.format(java.util.Locale.US,"%d:%02d",sec/60,sec%60);}
}
