package at.yawk.fimfiction.android;

import com.google.common.io.Closeables;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * @author Jonas Konrad (yawkat)
 */
@RequiredArgsConstructor
@Log4j
public class PublicPreferenceManager {
    private final Helper helper;
    @Getter private final IdentifiableQueryConfig queryConfig = new IdentifiableQueryConfig(this);

    @Getter private JsonObject config = new JsonObject();

    public void load() {
        if (configFile().exists()) {
            Reader reader = null;
            try {
                reader = new FileReader(configFile());
                config = new JsonParser().parse(reader).getAsJsonObject();
                queryConfig.load();
            } catch (IOException e) {
                log.error(e);
            } finally {
                Closeables.closeQuietly(reader);
            }
        }
    }

    public void save() {
        helper.executeGlobalTask(new Runnable() {
            @Override
            public void run() {
                save0();
            }
        });
    }

    private synchronized void save0() {
        Writer writer = null;
        try {
            writer = new FileWriter(configFile());
            writer.write(config.toString());
        } catch (IOException e) {
            log.error(e);
        } finally {
            Closeables.closeQuietly(writer);
        }
    }

    private File configFile() { return new File(helper.baseDir(), "config.json"); }

    public boolean getBoolean(String key, boolean defaultVal) {
        return config.has(key) ? config.get(key).getAsBoolean() : defaultVal;
    }
}
