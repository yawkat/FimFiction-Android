package at.yawk.fimfiction.android;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import at.yawk.fimfiction.data.SearchParameters;

/**
 * @author Yawkat
 */
public abstract class Fimtivity extends Activity implements Constants {
    private boolean shutDown;

    protected void execute(Runnable runnable) {
        BrowserApp.runTask(this, runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        shutDown = true;
        ((BrowserApp) getApplication()).interruptScheduler();
        Log.d(TAG, "Shut down activity.");
    }

    protected void openParams(SearchParameters params, int titleResource) {
        openParams(params, getResources().getString(titleResource));
    }

    protected void openParams(SearchParameters params, String title) {
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), StoryList.class);
        intent.putExtra("search", new ParamReader(params, title));
        startIntent(intent, true);
    }

    protected void startIntent(Intent intent, boolean replace) {
        if (replace) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        startActivity(intent);
        if (replace) {
            finish();
            overridePendingTransition(0, 0);
        }
    }

    public boolean isShutDown() {
        return shutDown;
    }
}
