package com.studio.ai;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.OptIn;
import androidx.media3.common.C;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MimeTypes;
import androidx.media3.common.audio.SpeedProvider;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.transformer.Composition;
import androidx.media3.transformer.EditedMediaItem;
import androidx.media3.transformer.EditedMediaItemSequence;
import androidx.media3.transformer.ExportException;
import androidx.media3.transformer.ExportResult;
import androidx.media3.transformer.Transformer;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

@OptIn(markerClass = UnstableApi.class)
public final class TimelineRenderer {
    public interface Listener { void onCompleted(File file, ExportResult result); void onError(Exception error); }
    private TimelineRenderer() {}

    public static Transformer render(Context context, JSONObject video, JSONObject timeline, File output, Listener listener) throws Exception {
        String source = video.optString("uri", "");
        if (source.isEmpty()) throw new IllegalArgumentException("مصدر الفيديو غير موجود");
        JSONArray clips = timeline.optJSONArray("clips");
        List<EditedMediaItem> items = new ArrayList<>();
        for (int i=0; clips!=null && i<clips.length(); i++) {
            JSONObject c = clips.optJSONObject(i);
            if (c==null || !c.optBoolean("enabled",true)) continue;
            long start=c.optLong("sourceStartMs",0), end=c.optLong("sourceEndMs",0);
            if (end<=start) continue;
            MediaItem media = new MediaItem.Builder()
                    .setUri(Uri.parse(source))
                    .setClippingConfiguration(new MediaItem.ClippingConfiguration.Builder().setStartPositionMs(start).setEndPositionMs(end).build())
                    .build();
            EditedMediaItem.Builder edit = new EditedMediaItem.Builder(media);
            double speed=c.optDouble("speed",1.0);
            if (Math.abs(speed-1.0)>0.001) edit.setSpeed(constantSpeed((float)speed));
            items.add(edit.build());
        }
        if (items.isEmpty()) throw new IllegalStateException("لا توجد لقطات مفعلة للرندر");
        if (output.exists() && !output.delete()) throw new IllegalStateException("تعذر استبدال ملف الرندر السابق");
        EditedMediaItemSequence sequence = EditedMediaItemSequence.withAudioAndVideoFrom(items);
        Composition composition = new Composition.Builder(sequence).build();
        Transformer transformer = new Transformer.Builder(context)
                .setVideoMimeType(MimeTypes.VIDEO_H264)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(new Transformer.Listener() {
                    @Override public void onCompleted(Composition composition, ExportResult result) { listener.onCompleted(output,result); }
                    @Override public void onError(Composition composition, ExportResult result, ExportException exception) { listener.onError(exception); }
                }).build();
        transformer.start(composition, output.getAbsolutePath());
        return transformer;
    }

    private static SpeedProvider constantSpeed(final float speed) {
        return new SpeedProvider() {
            @Override public float getSpeed(long timeUs) { return speed; }
            @Override public long getNextSpeedChangeTimeUs(long timeUs) { return C.TIME_UNSET; }
        };
    }
}
