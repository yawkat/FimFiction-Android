package at.yawk.fimfiction.android;

import at.yawk.fimfiction.net.SessionManager;

/**
 * @author Yawkat
 */
public interface Constants {
    static final String TAG = "FIMFIC";
    static final String PREFERENCES = "at.yawk.fimfiction.android";
    static final SessionManager session = SessionManager.create();
    static final boolean SHOW_CHARACTERS = true;
}
