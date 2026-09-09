package com.studio.ai;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;

public final class VideoInspector {
    private VideoInspector() {}

    public static JSONObject inspect(Context context, Uri uri) throws Exception {
        MediaMetadataRetriever r = new MediaMetadataRetriever();
        try {
            r.setDataSource(context, uri);
            JSONObject out = new JSONObject();
            long durationMs = number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            int width = (int) number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
            int height = (int) number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
            int rotation = (int) number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
            long bitrate = number(r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE));
            String mime = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            String fps = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
            String hasAudio = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO);
            String hasVideo = r.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);

            out.put("durationMs", durationMs);
            out.put("width", width);
            out.put("height", height);
            out.put("rotation", rotation);
            out.put("bitrate", bitrate);
            out.put("mime", mime == null ? "" : mime);
            out.put("fps", fps == null ? "" : fps);
            out.put("hasAudio", "yes".equalsIgnoreCase(hasAudio));
            out.put("hasVideo", "yes".equalsIgnoreCase(hasVideo));
            out.put("inspectedAt", System.currentTimeMillis());

            // Sample real frames across the source. We store measurable frame statistics,
            // not fake scene labels. Shot/transition understanding belongs to Batch 4/5.
            JSONArray samples = new JSONArray();
            int count = durationMs <= 0 ? 0 : (int)Math.min(12, Math.max(4, durationMs / 5000L + 1));
            for (int i = 0; i < count; i++) {
                long atMs = count == 1 ? 0 : (durationMs - 1) * i / (count - 1);
                Bitmap b = r.getFrameAtTime(atMs * 1000L, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                if (b == null) continue;
                JSONObject s = frameStats(b, atMs);
                samples.put(s);
                b.recycle();
            }
            out.put("samples", samples);
            return out;
        } finally {
            try { r.release(); } catch (Exception ignored) {}
        }
    }

    private static JSONObject frameStats(Bitmap source, long atMs) throws Exception {
        Bitmap b = Bitmap.createScaledBitmap(source, 48, 48, true);
        long lum = 0, motionColor = 0;
        int n = b.getWidth() * b.getHeight();
        int[] px = new int[n];
        b.getPixels(px, 0, b.getWidth(), 0, 0, b.getWidth(), b.getHeight());
        for (int c : px) {
            int r = (c >> 16) & 255, g = (c >> 8) & 255, bl = c & 255;
            lum += (299L*r + 587L*g + 114L*bl) / 1000L;
            motionColor += Math.max(r, Math.max(g, bl)) - Math.min(r, Math.min(g, bl));
        }
        b.recycle();
        JSONObject o = new JSONObject();
        o.put("timeMs", atMs);
        o.put("brightness", n == 0 ? 0 : lum / n);
        o.put("colorRange", n == 0 ? 0 : motionColor / n);
        return o;
    }

    private static long number(String s) {
        try { return s == null ? 0L : Long.parseLong(s); } catch (Exception e) { return 0L; }
    }
}
