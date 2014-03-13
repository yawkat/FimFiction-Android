package at.yawk.fimfiction.android;

import android.os.AsyncTask;
import at.yawk.fimfiction.core.Download;
import at.yawk.fimfiction.data.Story;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import lombok.Value;
import lombok.extern.log4j.Log4j;

/**
 * Downloads a story.
 *
 * @author Yawkat
 * @see ProgressStoryDownloadTask
 */
@Log4j
public class StoryDownloadTask extends AsyncTask<StoryDownloadTask.Params, StoryDownloadTask.Progress, Boolean> {
    @Override
    protected Boolean doInBackground(Params... params) {
        assert params.length == 1;
        Story story = params[0].getStory();
        File target = params[0].getTarget();

        try {
            URL url = Download.getStoryDownloadUrl(story, Download.Format.EPUB);
            URLConnection con = url.openConnection();
            con.connect();
            int max = con.getContentLength();

            if (isCancelled()) { return false; }
            log.debug("Downloading " + story.get(Story.StoryKey.ID) + " (len=" + max + " ex=" + target.isFile() +
                      " exlen=" + target.length() + ")");
            if (target.isFile() && Math.abs(target.length() - max) < 10) { return true; }

            target.getParentFile().mkdirs();
            OutputStream o = new FileOutputStream(target);
            boolean successful = false;
            try {
                int len;
                byte[] buffer = new byte[1024];
                int done = 0;
                while ((len = con.getInputStream().read(buffer)) > 0 && !isCancelled()) {
                    publishProgress(new Progress(done, max));
                    o.write(buffer, 0, len);
                    done += len;
                }
                successful = !isCancelled();
            } finally {
                o.close();
            }
            if (!successful) {
                target.delete();
            }
            log.debug("Downloaded " + story.get(Story.StoryKey.ID) + " (success=" + successful + ")");
            return successful;
        } catch (Exception e) {
            log.error("Could not download " + story.get(Story.StoryKey.ID), e);
        }
        return false;
    }

    @Value
    public static class Params {
        private final Story story;
        private final File target;
    }

    @Value
    public static class Progress {
        private final int current;
        private final int max;
    }
}
