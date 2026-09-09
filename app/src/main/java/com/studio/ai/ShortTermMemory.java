package com.studio.ai;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ShortTermMemory {
    private static final int MAX_EVENTS = 24;
    private ShortTermMemory() {}

    private static String key(String project,String style){
        return "stm_"+Integer.toHexString((project+"|"+style).hashCode());
    }

    public static JSONObject load(SharedPreferences prefs,String project,String style){
        try{return new JSONObject(prefs.getString(key(project,style),"{}"));}
        catch(Exception e){return new JSONObject();}
    }

    public static void rememberVideo(SharedPreferences prefs,String project,String style,JSONObject video){
        try{
            JSONObject m=load(prefs,project,style);
            m.put("project",project);m.put("style",style);m.put("updatedAt",System.currentTimeMillis());
            m.put("activeVideoName",video.optString("name","فيديو"));
            m.put("activeVideoUri",video.optString("uri",""));
            JSONObject inspection=video.optJSONObject("inspection");
            if(inspection!=null){
                JSONObject summary=new JSONObject();
                summary.put("durationMs",inspection.optLong("durationMs"));
                summary.put("averageMotion",inspection.optDouble("averageMotion"));
                summary.put("averageChange",inspection.optDouble("averageChange"));
                JSONArray cuts=inspection.optJSONArray("cutCandidates");summary.put("cutCandidates",cuts==null?0:cuts.length());
                JSONObject st=inspection.optJSONObject("editingStyle");if(st!=null)summary.put("editingStyle",new JSONObject(st.toString()));
                m.put("latestAnalysis",summary);
            }
            event(m,"video_analysis",video.optString("name","فيديو"));save(prefs,project,style,m);
        }catch(Exception ignored){}
    }

    public static void rememberOpenedVideo(SharedPreferences prefs,String project,String style,String name,String uri){
        try{JSONObject m=load(prefs,project,style);m.put("project",project);m.put("style",style);m.put("updatedAt",System.currentTimeMillis());m.put("activeVideoName",name);m.put("activeVideoUri",uri);event(m,"opened_video",name);save(prefs,project,style,m);}catch(Exception ignored){}
    }

    public static void addNote(SharedPreferences prefs,String project,String style,String text){
        try{JSONObject m=load(prefs,project,style);m.put("project",project);m.put("style",style);m.put("updatedAt",System.currentTimeMillis());JSONArray notes=m.optJSONArray("notes");if(notes==null)notes=new JSONArray();JSONObject n=new JSONObject();n.put("text",text);n.put("at",System.currentTimeMillis());notes.put(n);while(notes.length()>12)removeFirst(notes);m.put("notes",notes);event(m,"note",text);save(prefs,project,style,m);}catch(Exception ignored){}
    }

    public static void clear(SharedPreferences prefs,String project,String style){prefs.edit().remove(key(project,style)).apply();}

    private static void event(JSONObject m,String type,String value)throws Exception{JSONArray a=m.optJSONArray("events");if(a==null)a=new JSONArray();JSONObject e=new JSONObject();e.put("type",type);e.put("value",value);e.put("at",System.currentTimeMillis());a.put(e);while(a.length()>MAX_EVENTS)removeFirst(a);m.put("events",a);}
    private static void removeFirst(JSONArray a)throws Exception{JSONArray b=new JSONArray();for(int i=1;i<a.length();i++)b.put(a.get(i));while(a.length()>0)a.remove(a.length()-1);for(int i=0;i<b.length();i++)a.put(b.get(i));}
    private static void save(SharedPreferences prefs,String project,String style,JSONObject m){prefs.edit().putString(key(project,style),m.toString()).apply();}
}
