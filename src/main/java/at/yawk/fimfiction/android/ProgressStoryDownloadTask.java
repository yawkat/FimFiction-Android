package at.yawk.fimfiction.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * @author Yawkat
 */
public class ProgressStoryDownloadTask extends StoryDownloadTask {
    private final Context context;
    private ProgressDialog dialog;

    public ProgressStoryDownloadTask(final Context context) {
        this.context = context;
    }

    @Override
    protected void onPostExecute(final Boolean result) {
        dialog.hide();
        dialog = null;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setMax(0);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setTitle("Downloading");
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(final DialogInterface dialog) {
                cancel(false);
            }
        });
        dialog.setProgressNumberFormat("...");
        dialog.show();
    }

    @Override
    protected void onProgressUpdate(final Progress... values) {
        dialog.setIndeterminate(values[0].getMax() == -1);
        dialog.setMax(values[0].getMax());
        dialog.setProgress(values[0].getCurrent());
        dialog.setProgressNumberFormat("%1d/%2d");
    }
}
