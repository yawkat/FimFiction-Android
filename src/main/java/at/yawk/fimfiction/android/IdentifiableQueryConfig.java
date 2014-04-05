package at.yawk.fimfiction.android;

import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.json.Deserializer;
import at.yawk.fimfiction.json.Serializer;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.util.List;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * @author Jonas Konrad (yawkat)
 */
@RequiredArgsConstructor
@Log4j
public class IdentifiableQueryConfig {
    private final PublicPreferenceManager preferenceManager;
    private final List<SearchParameters> queries = Lists.newArrayList();

    public synchronized int getQueryId(SearchParameters query) {
        int i = queries.indexOf(query);
        if (i == -1) {
            if (!preferenceManager.getConfig().has("queries")) {
                preferenceManager.getConfig().add("queries", new JsonArray());
            }
            preferenceManager.getConfig().get("queries").getAsJsonArray().add(new Serializer().serializeBundle(query));
            i = queries.size();
            queries.add(query);
            log.info("Appending query #" + i + ": " + query);
        }
        return i;
    }

    @Nullable
    public synchronized SearchParameters getQuery(int id) {
        return id >= 0 && id < queries.size() ? queries.get(id) : null;
    }

    synchronized void load() {
        if (!preferenceManager.getConfig().has("queries")) { return; }
        queries.clear();
        for (JsonElement element : preferenceManager.getConfig().get("queries").getAsJsonArray()) {
            try {
                queries.add(new Deserializer().deserializeBundle(element.getAsJsonObject(), SearchParameters.class));
            } catch (Exception e) { log.error(e); }
        }
        log.info("Loaded " + queries.size() + " queries");
    }
}
