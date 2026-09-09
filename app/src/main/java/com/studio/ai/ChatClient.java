package com.studio.ai;

import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public final class ChatClient {
    private static final String ENDPOINT = "https://studio-ai-api.tekss616.workers.dev";

    private ChatClient() {}

    public static String ask(String message, String project, String style, JSONObject memory, JSONObject video) throws Exception {
        JSONObject body = new JSONObject();
        body.put("message", message);
        body.put("project", project);
        body.put("style", style);
        body.put("memory", memory == null ? new JSONObject() : memory);
        body.put("video", video == null ? new JSONObject() : video);

        HttpURLConnection connection = (HttpURLConnection) new URL(ENDPOINT).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");

        byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(payload);
        }

        int code = connection.getResponseCode();
        InputStream input = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        StringBuilder raw = new StringBuilder();
        if (input != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) raw.append(line);
            }
        }
        connection.disconnect();

        JSONObject result = raw.length() == 0 ? new JSONObject() : new JSONObject(raw.toString());
        if (code < 200 || code >= 300 || !result.optBoolean("ok")) {
            throw new Exception(result.optString("error", "فشل الاتصال بمحرك الذكاء الاصطناعي"));
        }
        String reply = result.optString("reply", "").trim();
        if (reply.isEmpty()) throw new Exception("وصل رد فارغ من المحرك");
        return reply;
    }
}
