package at.yawk.fimfiction.android;

import at.yawk.fimfiction.data.SearchParameters;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.Iterator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * @author Jonas Konrad (yawkat)
 */
@RequiredArgsConstructor
@Log4j
public class CustomSearchConfig {
    private final PublicPreferenceManager preferenceManager;

    public synchronized void add(SearchParameters parameters) {
        int id = preferenceManager.getQueryConfig().getQueryId(parameters);
        searchArray().add(new JsonPrimitive(id));
    }

    public synchronized void remove(SearchParameters parameters) {
        int id = preferenceManager.getQueryConfig().getQueryId(parameters);
        for (Iterator<JsonElement> itr = searchArray().iterator(); itr.hasNext(); ) {
            JsonPrimitive ele = itr.next().getAsJsonPrimitive();
            if (ele.getAsInt() == id) { itr.remove(); }
        }
        preferenceManager.save();
    }

    public synchronized List<SearchParameters> get() {
        List<SearchParameters> result = Lists.newArrayList();
        for (JsonElement etr : searchArray()) {
            SearchParameters query = preferenceManager.getQueryConfig().getQuery(etr.getAsInt());
            if (query != null) {
                result.add(query);
            } else {
                log.warn("Cannot load search #" + etr);
            }
        }
        return result;
    }

    private JsonArray searchArray() {
        if (!preferenceManager.getConfig().has("searches")) {
            preferenceManager.getConfig().add("searches", new JsonArray());
        }
        return preferenceManager.getConfig().get("searches").getAsJsonArray();
    }
}
