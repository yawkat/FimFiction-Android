package at.yawk.fimfiction.android;

import at.yawk.fimfiction.net.SessionManager;
import java.io.File;

/**
 * @author Yawkat
 */
public interface Constants {
    static final String TAG = "FIMFIC";
    static final String PREFERENCES = "at.yawk.fimfiction.android";
    static final SessionManager session = SessionManager.create();
    static final File root = new File("/storage/extSdCard/FimFiction/");
    static final ImageCache imageCache = new ImageCache();
    static final boolean SHOW_CHARACTERS = true;
}
