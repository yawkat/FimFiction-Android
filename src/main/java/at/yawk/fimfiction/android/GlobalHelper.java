package at.yawk.fimfiction.android;

import android.content.Context;
import at.yawk.fimfiction.net.SessionManager;
import de.mindpipe.android.logging.log4j.LogConfigurator;
import java.io.File;
import lombok.Getter;
import org.apache.log4j.Level;

/**
 * Global helper with singleton.
 *
 * @author Jonas Konrad (yawkat)
 */
@Getter
public class GlobalHelper extends Helper {
    private static GlobalHelper instance;

    public static synchronized GlobalHelper getGlobalHelper(Context context) {
        if (instance == null) { instance = new GlobalHelper(context); }
        return instance;
    }

    private GlobalHelper(Context context) {
        super(context);

        LogConfigurator l = new LogConfigurator();
        l.setFileName(new File(baseDir(), "logs/fimfiction-android.log").getAbsolutePath());
        l.setRootLevel(Level.DEBUG);
        l.configure();
    }

    private final TaskManager taskManager = new TaskManager();
    private final CharacterManager characterManager = new CharacterManager(this);
    private final ImageCache imageCache = new ImageCache(new File(baseDir(), "images"));
    private final SessionManager session = SessionManager.create();
    private final SearchParameterManager parameterManager = new SearchParameterManager();
}

