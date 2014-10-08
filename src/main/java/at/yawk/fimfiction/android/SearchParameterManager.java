package at.yawk.fimfiction.android;

import static at.yawk.fimfiction.data.SearchParameters.SearchParameter.*;

import android.widget.Button;
import at.yawk.fimfiction.data.Order;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.data.Timeframe;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;

/**
 * @author Jonas Konrad (yawkat)
 */
@RequiredArgsConstructor
public class SearchParameterManager {
    private final Helper helper;
    private final Map<SearchParameters, Integer> names = Maps.newHashMap();
    private final Map<Integer, SearchParameters> buttons = Maps.newHashMap();

    { init(); }

    private void init() {
        add(SearchParameters.createMutable().set(ORDER, Order.HOT), R.id.hot, R.string.hot);
        add(SearchParameters.createMutable().set(ORDER, Order.UPDATE_DATE), R.id.updates, R.string.updates);
        add(SearchParameters.createMutable()
                            .set(ORDER, Order.RATING)
                            .set(PUBLISH_TIMEFRAME, Timeframe.DefaultTimeframe.ONE_DAY),
            R.id.top_today,
            R.string.top_today
        );
        add(SearchParameters.createMutable().set(ORDER, Order.UPDATE_DATE).set(COMPLETED, true),
            R.id.completed,
            R.string.completed);
        add(SearchParameters.createMutable().set(ORDER, Order.VIEW_COUNT), R.id.most_viewed, R.string.most_viewed);
        add(SearchParameters.createMutable().set(ORDER, Order.RATING), R.id.top_all_time, R.string.top_all_time);
        add(SearchParameters.createMutable()
                            .set(ORDER, Order.RATING)
                            .set(PUBLISH_TIMEFRAME, Timeframe.DefaultTimeframe.ONE_WEEK),
            R.id.top_this_week,
            R.string.top_this_week
        );
        add(SearchParameters.createMutable().set(ORDER, Order.WORD_COUNT), R.id.longest, R.string.longest);
        add(SearchParameters.createMutable().set(ORDER, Order.COMMENT_COUNT),
            R.id.most_comments,
            R.string.most_comments);
    }

    private void add(SearchParameters parameters, int button, int name) {
        parameters = parameters.immutableCopy();
        names.put(parameters, name);
        if (button != 0) { buttons.put(button, parameters); }
    }

    private String customNameOrNull(SearchParameters parameters) {
        JsonObject conf = helper.getPreferences().getConfig().getAsJsonObject("names");
        if (conf == null) { return null; }
        String code = getCodeId(parameters);
        return conf.has(code) ? conf.get(code).getAsString() : null;
    }

    private String getCodeId(SearchParameters parameters) {
        return ImageCache.padLeftZeros(Integer.toHexString(helper.getPreferences()
                                                                 .getQueryConfig()
                                                                 .getQueryId(parameters)));
    }

    public boolean hasFixedName(SearchParameters parameters) {
        return customNameOrNull(parameters) != null || names.containsKey(parameters);
    }

    public TranslatableText getName(SearchParameters parameters) {
        String custom = customNameOrNull(parameters);
        if (custom != null) {
            return TranslatableText.string(custom);
        } else if (hasFixedName(parameters)) {
            return TranslatableText.id(names.get(parameters));
        } else {
            return TranslatableText.id(R.string.search);
        }
    }

    public Map<Button, SearchParameters> findButtons(ActivityHelper helper) {
        Map<Button, SearchParameters> res = Maps.newHashMap();
        for (int k : buttons.keySet()) {
            Button button = helper.view(k);
            if (button != null) { res.put(button, buttons.get(k)); }
        }
        return res;
    }

    public void setCustomName(SearchParameters parameters, @Nullable String name) {
        JsonObject jconf = helper.getPreferences().getConfig();
        if (!jconf.has("names")) { jconf.add("names", new JsonObject()); }
        JsonObject conf = jconf.getAsJsonObject("names");
        if (name == null) {
            conf.remove(getCodeId(parameters));
        } else {
            conf.add(getCodeId(parameters), new JsonPrimitive(name));
        }
        helper.getPreferences().save();
    }
}
