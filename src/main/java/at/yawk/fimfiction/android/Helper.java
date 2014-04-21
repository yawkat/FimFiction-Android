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
 * Basic, application-wide helper methods.
 *
 * @author Jonas Konrad (yawkat)
 */
public abstract class Helper implements TaskManager.TaskContext {
    private final Context context;
    private boolean enabled = true;

    public Helper(Context context) { this.context = context; }

    /**
     * @return the owner context of this helper.
     */
    public Context context() { return context; }

    /**
     * @return the GlobalHelper singleton.
     */
    public GlobalHelper global() { return GlobalHelper.getGlobalHelper(context()); }

    /**
     * @return true if this Helper is still enabled.
     */
    public boolean enabled() { return enabled; }

    /**
     * @return a LayoutInflater instance associated to the owning Context.
     */
    public LayoutInflater layoutInflater() { return LayoutInflater.from(context()); }

    /**
     * @return The base directory for non-sensitive information (images, stories, etc).
     */
    public File baseDir() { return new File(Environment.getExternalStorageDirectory(), "FimFiction"); }

    /**
     * Dispose this Helper and stop its tasks.
     */
    public void shutdown() {
        enabled = false;
        global().getTaskManager().interruptScheduler();
    }

    /**
     * Perform a task associated with this helper, meaning it will be interrupted if the helper is closed before
     * completion.
     */
    public void executeTask(Runnable runnable) {
        global().getTaskManager().execute(this, runnable);
    }

    /**
     * Perform a task that will not get interrupted.
     */
    public void executeGlobalTask(Runnable runnable) { global().executeTask(runnable); }

    /**
     * @return The CharacterManager singleton.
     */
    public TagManager getTagManager() { return global().getTagManager(); }

    /**
     * @return The ImageCache singleton.
     */
    public ImageCache getImageCache() { return global().getImageCache(); }

    public SearchParameterManager getParameterManager() { return global().getParameterManager(); }

    public PublicPreferenceManager getPreferences() { return global().getPreferences(); }

    /**
     * Perform a task on the main thread.
     */
    public void runOnMainThread(Runnable task) {
        new Handler(context().getMainLooper()).post(task);
    }

    /**
     * @return The SessionManager singleton.
     */
    public SessionManager getSession() { return global().getSession(); }

    /**
     * @return True if large, cosmetic downloads (story cover images) are allowed.
     */
    public boolean bigDownloads() {
        return !((ConnectivityManager) context().getSystemService(Context.CONNECTIVITY_SERVICE))
                .isActiveNetworkMetered();
    }

    /**
     * Returns the SharedPreferences object that can be used for sensitive data such as passwords.
     */
    public SharedPreferences getSecretPreferences() {
        return context().getSharedPreferences("at.yawk.fimfiction.android", Context.MODE_PRIVATE);
    }

    public boolean showMature() { return getPreferences().getBoolean("mature", true); }
}
