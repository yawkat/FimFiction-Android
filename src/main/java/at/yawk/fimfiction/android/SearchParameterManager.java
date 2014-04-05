package at.yawk.fimfiction.android;

import static at.yawk.fimfiction.data.SearchParameters.SearchParameter.*;

import android.widget.Button;
import at.yawk.fimfiction.data.Order;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.data.Timeframe;
import com.google.common.collect.Maps;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * @author Jonas Konrad (yawkat)
 */
public class SearchParameterManager {
    private final Map<SearchParameters, Integer> names = Maps.newHashMap();
    private final Map<Integer, SearchParameters> buttons = Maps.newHashMap();

    { init(); }

    private void init() {
        add(SearchParameters.createMutable().set(ORDER, Order.UPDATE_DATE).set(UNREAD, true).set(FAVORITED, true),
            R.id.unread,
            R.string.unread);
        add(SearchParameters.createMutable().set(ORDER, Order.UPDATE_DATE).set(FAVORITED, true),
            R.id.favorite,
            R.string.favorite);
        add(SearchParameters.createMutable().set(ORDER, Order.UPDATE_DATE).set(READ_LATER, true),
            R.id.readlater,
            R.string.readlater);
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

    public SearchParameters getDefault() { return buttons.get(R.id.unread); }

    @Nullable
    public TranslatableText getNameOrNull(SearchParameters parameters) {
        return names.containsKey(parameters) ? TranslatableText.id(names.get(parameters)) : null;
    }

    public TranslatableText getName(SearchParameters parameters) {
        TranslatableText orNull = getNameOrNull(parameters);
        return orNull == null ? TranslatableText.id(R.string.search) : orNull;
    }

    public Map<Button, SearchParameters> findButtons(ActivityHelper helper) {
        Map<Button, SearchParameters> res = Maps.newHashMap();
        for (int k : buttons.keySet()) {
            Button button = helper.view(k);
            if (button != null) { res.put(button, buttons.get(k)); }
        }
        return res;
    }
}
