package at.yawk.fimfiction.android;

import android.widget.Button;
import at.yawk.fimfiction.data.Order;
import at.yawk.fimfiction.data.SearchParameters;
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
        add(SearchParameters.createMutable()
                            .set(SearchParameters.SearchParameter.ORDER, Order.UPDATE_DATE)
                            .set(SearchParameters.SearchParameter.UNREAD, true)
                            .set(SearchParameters.SearchParameter.FAVORITED, true), R.id.unread, R.string.unread);
        add(SearchParameters.createMutable()
                            .set(SearchParameters.SearchParameter.ORDER, Order.UPDATE_DATE)
                            .set(SearchParameters.SearchParameter.FAVORITED, true), R.id.favorite, R.string.favorite);
        add(SearchParameters.createMutable()
                            .set(SearchParameters.SearchParameter.ORDER, Order.UPDATE_DATE)
                            .set(SearchParameters.SearchParameter.READ_LATER, true), R.id.readlater, R.string.readlater
        );
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
