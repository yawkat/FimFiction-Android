package at.yawk.fimfiction.android;

import android.content.Context;
import at.yawk.fimfiction.net.SessionManager;
import java.io.File;

/**
 * @author Jonas Konrad (yawkat)
 */
public class GlobalHelper extends Helper {
    private static GlobalHelper instance;

    public static synchronized GlobalHelper getGlobalHelper(Context context) {
        if (instance == null) { instance = new GlobalHelper(context); }
        return instance;
    }

    private GlobalHelper(Context context) { super(context); }

    private final TaskManager taskManager = new TaskManager();
    private final CharacterManager characterManager = new CharacterManager(this);
    private final ImageCache imageCache = new ImageCache(new File(baseDir(), "images"));
    private final SessionManager session = SessionManager.create();

    public TaskManager getTaskManager() { return taskManager; }

    @Override
    public CharacterManager getCharacterManager() { return characterManager; }

    @Override
    public ImageCache getImageCache() { return imageCache; }

    @Override
    public SessionManager getSession() { return session; }
}

