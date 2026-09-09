package com.studio.ai;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;

public final class ExportManager {
    private ExportManager() {}

    public static Uri saveToGallery(Context context, File source, String displayName) throws Exception {
        if (source == null || !source.isFile() || source.length() == 0) throw new IllegalArgumentException("ملف الرندر غير صالح");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) throw new UnsupportedOperationException("الحفظ المباشر للمعرض في هذه النسخة يتطلب Android 10 أو أحدث");
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(MediaStore.Video.Media.DISPLAY_NAME, displayName.endsWith(".mp4") ? displayName : displayName + ".mp4");
        values.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4");
        values.put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/StudioAI");
        values.put(MediaStore.Video.Media.IS_PENDING, 1);
        Uri uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
        if (uri == null) throw new IllegalStateException("تعذر إنشاء ملف الفيديو في المعرض");
        boolean ok = false;
        try (FileInputStream in = new FileInputStream(source); OutputStream out = resolver.openOutputStream(uri, "w")) {
            if (out == null) throw new IllegalStateException("تعذر فتح ملف التصدير");
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            out.flush();
            ok = true;
        } finally {
            if (!ok) resolver.delete(uri, null, null);
        }
        ContentValues done = new ContentValues();
        done.put(MediaStore.Video.Media.IS_PENDING, 0);
        resolver.update(uri, done, null, null);
        return uri;
    }

    public static void share(Context context, Uri uri) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType("video/mp4");
        send.putExtra(Intent.EXTRA_STREAM, uri);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(send, "مشاركة الفيديو النهائي"));
    }
}
