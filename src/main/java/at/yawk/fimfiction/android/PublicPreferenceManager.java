package at.yawk.fimfiction.android;

import com.google.common.io.Closer;
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
    @Getter private final CustomSearchConfig searchConfig = new CustomSearchConfig(this);

    @Getter private JsonObject config = new JsonObject();

    public void load() {
        if (configFile().exists()) {
            Closer closer = Closer.create();
            try {
                Reader reader = closer.register(new FileReader(configFile()));
                config = new JsonParser().parse(reader).getAsJsonObject();
                queryConfig.load();
            } catch (Exception e) {
                log.error(e);

                // backup config to config.json.<number> with lowest number possible
                for (int i = 1; ; i++) {
                    File backup = new File(helper.baseDir(), "config.json." + i);
                    if (backup.exists()) { continue; }
                    configFile().renameTo(backup);
                    break;
                }
            } finally {
                try {
                    closer.close();
                } catch (IOException ignored) {}
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
        log.info("Saving config...");

        File tmpFile = new File(helper.baseDir(), "config.json.tmp");

        Closer closer = Closer.create();
        try {
            Writer writer = closer.register(new FileWriter(tmpFile));
            writer.write(config.toString());
        } catch (IOException e) {
            log.error(e);
            return;
        } finally {
            try {
                closer.close();
            } catch (IOException ce) {
                log.error(ce);
            }
        }

        File cfg = configFile();
        boolean result = tmpFile.renameTo(cfg);
        if (!result) { log.error("Could not save config: Rename failed"); }
    }

    private File configFile() { return new File(helper.baseDir(), "config.json"); }

    public boolean getBoolean(String key, boolean defaultVal) {
        return config.has(key) ? config.get(key).getAsBoolean() : defaultVal;
    }
}
