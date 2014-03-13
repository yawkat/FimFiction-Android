package at.yawk.fimfiction.android;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.os.Handler;
import android.view.LayoutInflater;
import at.yawk.fimfiction.net.SessionManager;
import java.io.File;

/**
 * @author Jonas Konrad (yawkat)
 */
public abstract class Helper {
    private final Context context;
    private boolean enabled = true;

    public Helper(Context context) { this.context = context; }

    public Context context() { return context; }

    private GlobalHelper global() { return GlobalHelper.getGlobalHelper(context()); }

    public boolean enabled() { return enabled; }

    public LayoutInflater layoutInflater() { return LayoutInflater.from(context()); }

    public File baseDir() { return new File(Environment.getExternalStorageDirectory(), "FimFiction"); }

    public void shutdown() {
        enabled = false;
        global().getTaskManager().interruptScheduler();
    }

    public void executeTask(Runnable runnable) {
        global().getTaskManager().execute(this, runnable);
    }

    public void executeGlobalTask(Runnable runnable) { global().executeTask(runnable); }

    public CharacterManager getCharacterManager() { return global().getCharacterManager(); }

    public ImageCache getImageCache() { return global().getImageCache(); }

    public void runOnMainThread(Runnable task) {
        new Handler(context().getMainLooper()).post(task);
    }

    public SessionManager getSession() { return global().getSession(); }

    public boolean bigDownloads() {
        return !((ConnectivityManager) context().getSystemService(Context.CONNECTIVITY_SERVICE))
                .isActiveNetworkMetered();
    }

    public SharedPreferences getPreferences() {
        return context().getSharedPreferences("at.yawk.fimfiction.android", Context.MODE_PRIVATE);
    }
}
