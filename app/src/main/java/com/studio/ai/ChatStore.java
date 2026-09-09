package com.studio.ai;

import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;

public final class ChatStore {
    private static final int MAX_MESSAGES = 40;

    private ChatStore() {}

    private static String key(String project, String style) {
        return "chat_" + Integer.toHexString((project + "|" + style).hashCode());
    }

    public static JSONArray load(SharedPreferences prefs, String project, String style) {
        try {
            return new JSONArray(prefs.getString(key(project, style), "[]"));
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }

    public static void append(SharedPreferences prefs, String project, String style, String role, String text) {
        JSONArray messages = load(prefs, project, style);
        JSONObject item = new JSONObject();
        try {
            item.put("role", role);
            item.put("text", text);
            item.put("time", System.currentTimeMillis());
            messages.put(item);
            while (messages.length() > MAX_MESSAGES) {
                JSONArray trimmed = new JSONArray();
                for (int i = 1; i < messages.length(); i++) trimmed.put(messages.opt(i));
                messages = trimmed;
            }
            prefs.edit().putString(key(project, style), messages.toString()).apply();
        } catch (Exception ignored) {}
    }

    public static JSONArray recent(SharedPreferences prefs, String project, String style, int limit) {
        JSONArray all = load(prefs, project, style);
        JSONArray out = new JSONArray();
        int start = Math.max(0, all.length() - Math.max(1, limit));
        for (int i = start; i < all.length(); i++) out.put(all.opt(i));
        return out;
    }
}
