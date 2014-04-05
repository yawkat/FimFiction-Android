package at.yawk.fimfiction.android;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import at.yawk.fimfiction.data.SearchParameters;

/**
 * Helper extension that provides methods for replacing it's owning activity with new activities.
 *
 * @author Jonas Konrad (yawkat)
 */
public class ActivityHelper extends Helper {
    public ActivityHelper(Activity activity) { super(activity); }

    public Activity activity() { return (Activity) context(); }

    public void openSearchActivity(SearchParameters parameters) {
        Intent intent = new Intent();
        intent.setClass(context(), StoryList.class);
        intent.putExtra("search", new ParamReader(parameters));
        openActivity(intent, true);
    }

    public void openActivity(Intent intent, boolean replaceCurrentActivity) {
        if (replaceCurrentActivity) { intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); }
        context().startActivity(intent);
        if (replaceCurrentActivity) {
            activity().finish();
            activity().overridePendingTransition(0, 0);
        }
    }

    public <V extends View> V view(int id) { return (V) activity().findViewById(id); }
}
