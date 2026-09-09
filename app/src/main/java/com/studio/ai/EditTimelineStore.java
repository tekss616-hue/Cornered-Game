package com.studio.ai;

import android.content.SharedPreferences;
import org.json.*;

public final class EditTimelineStore {
    private EditTimelineStore() {}

    private static String key(String project,String style,String uri){
        return "timeline_"+Integer.toHexString((project+"|"+style+"|"+uri).hashCode());
    }

    public static JSONObject load(SharedPreferences prefs,String project,String style,JSONObject video){
        String uri=video.optString("uri","");
        try{
            String raw=prefs.getString(key(project,style,uri),null);
            if(raw!=null) return new JSONObject(raw);
        }catch(Exception ignored){}
        JSONObject created=fromAnalysis(video);
        save(prefs,project,style,video,created);
        return created;
    }

    public static void save(SharedPreferences prefs,String project,String style,JSONObject video,JSONObject timeline){
        prefs.edit().putString(key(project,style,video.optString("uri","")),timeline.toString()).apply();
    }

    public static void reset(SharedPreferences prefs,String project,String style,JSONObject video){
        prefs.edit().remove(key(project,style,video.optString("uri",""))).apply();
    }

    private static JSONObject fromAnalysis(JSONObject video){
        JSONObject out=new JSONObject(); JSONArray clips=new JSONArray();
        try{
            out.put("version",13); out.put("videoName",video.optString("name","فيديو")); out.put("videoUri",video.optString("uri",""));
            JSONObject in=video.optJSONObject("inspection"); long duration=in==null?0:in.optLong("durationMs",0);
            JSONArray segments=in==null?null:in.optJSONArray("segments");
            if(segments!=null&&segments.length()>0){
                for(int i=0;i<segments.length();i++){
                    JSONObject s=segments.optJSONObject(i); if(s==null)continue;
                    long start=readMs(s,"startMs","start",0), end=readMs(s,"endMs","end",0);
                    if(end>start) clips.put(clip(i,start,end));
                }
            }
            if(clips.length()==0&&duration>0) clips.put(clip(0,0,duration));
            out.put("clips",clips); out.put("updatedAt",System.currentTimeMillis());
        }catch(Exception ignored){}
        return out;
    }

    private static long readMs(JSONObject o,String a,String b,long fallback){
        if(o.has(a))return o.optLong(a,fallback);
        double v=o.optDouble(b,-1); if(v<0)return fallback;
        return v<10000?Math.round(v*1000):Math.round(v);
    }

    private static JSONObject clip(int index,long start,long end)throws JSONException{
        JSONObject c=new JSONObject(); c.put("id","clip_"+index+"_"+start); c.put("sourceStartMs",start); c.put("sourceEndMs",end);
        c.put("enabled",true); c.put("speed",1.0); c.put("note",""); return c;
    }

    public static long duration(JSONObject timeline){
        long total=0; JSONArray a=timeline.optJSONArray("clips");
        for(int i=0;a!=null&&i<a.length();i++){JSONObject c=a.optJSONObject(i);if(c==null||!c.optBoolean("enabled",true))continue;double speed=Math.max(.25,c.optDouble("speed",1));total+=Math.round((c.optLong("sourceEndMs")-c.optLong("sourceStartMs"))/speed);}
        return Math.max(0,total);
    }

    public static boolean trim(JSONObject timeline,int index,long startMs,long endMs){
        JSONArray a=timeline.optJSONArray("clips"); JSONObject c=a==null?null:a.optJSONObject(index); if(c==null)return false;
        long originalStart=c.optLong("sourceStartMs"),originalEnd=c.optLong("sourceEndMs");
        startMs=Math.max(originalStart,startMs); endMs=Math.min(originalEnd,endMs); if(endMs-startMs<100)return false;
        try{c.put("sourceStartMs",startMs);c.put("sourceEndMs",endMs);timeline.put("updatedAt",System.currentTimeMillis());return true;}catch(Exception e){return false;}
    }

    public static boolean split(JSONObject timeline,int index,long atMs){
        JSONArray a=timeline.optJSONArray("clips"); JSONObject c=a==null?null:a.optJSONObject(index); if(c==null)return false;
        long start=c.optLong("sourceStartMs"),end=c.optLong("sourceEndMs"); if(atMs-start<100||end-atMs<100)return false;
        try{
            JSONObject left=new JSONObject(c.toString()),right=new JSONObject(c.toString()); left.put("sourceEndMs",atMs);right.put("sourceStartMs",atMs);right.put("id",c.optString("id")+"_s"+atMs);
            JSONArray n=new JSONArray();for(int i=0;i<a.length();i++){if(i==index){n.put(left);n.put(right);}else n.put(a.opt(i));}timeline.put("clips",n);timeline.put("updatedAt",System.currentTimeMillis());return true;
        }catch(Exception e){return false;}
    }

    public static boolean move(JSONObject timeline,int from,int to){
        JSONArray a=timeline.optJSONArray("clips");if(a==null||from<0||from>=a.length()||to<0||to>=a.length()||from==to)return false;
        try{Object item=a.get(from);JSONArray n=new JSONArray();for(int i=0;i<a.length();i++){if(i==to)n.put(item);if(i!=from)n.put(a.get(i));}timeline.put("clips",n);timeline.put("updatedAt",System.currentTimeMillis());return true;}catch(Exception e){return false;}
    }

    public static boolean setEnabled(JSONObject timeline,int index,boolean enabled){try{JSONObject c=timeline.getJSONArray("clips").getJSONObject(index);c.put("enabled",enabled);timeline.put("updatedAt",System.currentTimeMillis());return true;}catch(Exception e){return false;}}
    public static boolean setSpeed(JSONObject timeline,int index,double speed){if(speed!=.5&&speed!=1.0&&speed!=1.5&&speed!=2.0)return false;try{JSONObject c=timeline.getJSONArray("clips").getJSONObject(index);c.put("speed",speed);timeline.put("updatedAt",System.currentTimeMillis());return true;}catch(Exception e){return false;}}
    public static boolean setNote(JSONObject timeline,int index,String note){try{JSONObject c=timeline.getJSONArray("clips").getJSONObject(index);c.put("note",note==null?"":note.trim());timeline.put("updatedAt",System.currentTimeMillis());return true;}catch(Exception e){return false;}}
}
