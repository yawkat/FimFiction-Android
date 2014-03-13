package at.yawk.fimfiction.android;

import android.app.Activity;

/**
 * @author Yawkat
 */
public abstract class Fimtivity extends Activity {
    private final ActivityHelper helper = new ActivityHelper(this);

    protected ActivityHelper helper() { return helper; }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        helper.shutdown();
    }
}
