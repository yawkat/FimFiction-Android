package at.yawk.fimfiction.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import java.io.*;
import java.net.URL;

/**
 * @author Yawkat
 */

public class ImageCache  {
    private final File dir;

    ImageCache(File dir) { this.dir = dir; }

    public synchronized Bitmap getImage(URL url) {
        Bitmap b = getCachedImage(url);
        if (b != null) { return b; }
        try {
            loadImage(url);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not download image " + file(url), e);
        }
        return getCachedImage(url);
    }

    public synchronized Bitmap getCachedImage(URL url) {
        try {
            if (file(url).exists()) {
                return BitmapFactory.decodeFile(file(url).getAbsolutePath());
            }
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not load image " + file(url) + " from cache", e);
        }
        return null;
    }

    private File file(URL url) {
        String file = url.getFile();
        if (file.indexOf('?') != -1) {
            file = file.substring(0, file.indexOf('?'));
        }
        String ext = file.indexOf('.') != -1 ? file.substring(file.lastIndexOf('.')) : file;
        return new File(dir, url.hashCode() + ext);
    }

    private void loadImage(URL url) throws IOException {
        File target = file(url);
        target.getParentFile().mkdirs();
        InputStream i = url.openStream();
        try {
            OutputStream o = new FileOutputStream(target);
            boolean successful = false;
            try {
                byte[] buf = new byte[1024];
                int len;
                while ((len = i.read(buf)) > 0) { o.write(buf, 0, len); }
                successful = true;
            } finally {
                o.close();
                if (!successful && target.exists()) {
                    target.delete();
                }
            }
        } finally {
            i.close();
        }
    }
}
