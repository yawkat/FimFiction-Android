package at.yawk.fimfiction.android;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.webkit.MimeTypeMap;
import java.io.File;
import javax.annotation.Nullable;

/**
 * Helper extension that provides methods for replacing it's owning activity with new activities.
 *
 * @author Jonas Konrad (yawkat)
 */
public class ActivityHelper extends Helper {
    public ActivityHelper(Activity activity) { super(activity); }

    public Activity activity() { return (Activity) context(); }

    public void openActivity(Intent intent, boolean replaceCurrentActivity) {
        if (replaceCurrentActivity) { intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); }
        context().startActivity(intent);
        if (replaceCurrentActivity) {
            activity().finish();
            activity().overridePendingTransition(0, 0);
        }
    }

    public <V extends View> V view(int id) { return (V) activity().findViewById(id); }

    public void openFileExternal(File file,
                                 TranslatableText missingApp,
                                 int recommendationName,
                                 @Nullable final Runnable recommendation) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                              MimeTypeMap.getSingleton().getMimeTypeFromExtension(Files.getExtension(file)));
        try {
            openActivity(intent, false);
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context());
            builder.setCancelable(true).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            if (recommendation != null) {
                builder.setNeutralButton(recommendationName, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        recommendation.run();
                    }
                });
            }
            missingApp.assignMessage(builder);
            builder.show();
        }
    }
}
