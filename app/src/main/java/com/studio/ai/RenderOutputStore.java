package com.studio.ai;
import android.content.SharedPreferences;
import org.json.JSONObject;
import java.io.File;
public final class RenderOutputStore {
 private RenderOutputStore(){}
 private static String key(String p,String s,String uri){return "render_"+Integer.toHexString((p+"|"+s+"|"+uri).hashCode());}
 public static void save(SharedPreferences prefs,String project,String style,JSONObject video,File file,long durationMs){try{JSONObject o=new JSONObject();o.put("path",file.getAbsolutePath());o.put("name",file.getName());o.put("size",file.length());o.put("durationMs",durationMs);o.put("createdAt",System.currentTimeMillis());prefs.edit().putString(key(project,style,video.optString("uri","")),o.toString()).apply();}catch(Exception ignored){}}
 public static JSONObject load(SharedPreferences prefs,String project,String style,JSONObject video){try{return new JSONObject(prefs.getString(key(project,style,video.optString("uri","")),"{}"));}catch(Exception e){return new JSONObject();}}
}
