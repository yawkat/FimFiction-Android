package at.yawk.fimfiction.android;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import at.yawk.fimfiction.data.ContentRating;
import at.yawk.fimfiction.data.Order;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.data.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * Activity helper that provides options for custom search.
 *
 * @author Yawkat
 */
@Log4j
@RequiredArgsConstructor
public abstract class SearchBuilder {
    private final SearchParameters defaults;

    protected abstract void openSearch(SearchParameters parameters);

    public View createView(Helper helper) {
        final View root = helper.layoutInflater().inflate(R.layout.search, null);

        /* TODO

        ((CheckBox) root.findViewById(R.id.favorite)).setChecked(defaults.getBoolean(SearchParameters.SearchParameter
        .FAVORITED,
                                                                                     false));
        ((CheckBox) root.findViewById(R.id.unread)).setChecked(defaults.getBoolean(SearchParameters.SearchParameter
        .UNREAD,
                                                                                   false));
        ((CheckBox) root.findViewById(R.id.readlater)).setChecked(defaults.getBoolean(SearchParameters
        .SearchParameter.READ_LATER,
                                                                                      false));
        ((CheckBox) root.findViewById(R.id.gore)).setChecked(defaults.getBoolean(SearchParameters.SearchParameter.GORE,
                                                                                 false));
        ((CheckBox) root.findViewById(R.id.sex)).setChecked(defaults.getBoolean(SearchParameters.SearchParameter.SEX,
                                                                                false));
        ((EditText) root.findViewById(R.id.title)).setText(defaults.getString(SearchParameters.SearchParameter.NAME,
                                                                              ""));
        int uv = defaults.getInt(SearchParameters.SearchParameter.USER, -1);
        if (uv != -1) { ((EditText) root.findViewById(R.id.author)).setText(Integer.toString(uv)); }
        ((Spinner) root.findViewById(R.id.content_rating)).setSelection(defaults.<ContentRating>get(SearchParameters
        .SearchParameter.CONTENT_RATING)
                                                                                .ordinal());
        ((Spinner) root.findViewById(R.id.order)).setSelection(defaults.<Order>get(SearchParameters.SearchParameter
        .ORDER)
                                                                       .ordinal());

        */

        final CharacterManager.CharacterList l = helper.getCharacterManager().createCharacterList(true);
        ((ViewGroup) root.findViewById(R.id.character_incl_container)).addView(l.getView());
        root.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchParameters parameters = defaults.mutableCopy();
                parameters.set(SearchParameters.SearchParameter.FAVORITED,
                               ((CheckBox) root.findViewById(R.id.favorite)).isChecked());
                parameters.set(SearchParameters.SearchParameter.UNREAD,
                               ((CheckBox) root.findViewById(R.id.unread)).isChecked());
                parameters.set(SearchParameters.SearchParameter.READ_LATER,
                               ((CheckBox) root.findViewById(R.id.readlater)).isChecked());
                parameters.set(SearchParameters.SearchParameter.GORE,
                               ((CheckBox) root.findViewById(R.id.gore)).isChecked());
                parameters.set(SearchParameters.SearchParameter.SEX,
                               ((CheckBox) root.findViewById(R.id.sex)).isChecked());
                parameters.set(SearchParameters.SearchParameter.NAME,
                               ((EditText) root.findViewById(R.id.title)).getText().toString());
                int contentRatingId = (int) ((Spinner) root.findViewById(R.id.content_rating)).getSelectedItemId();
                if (contentRatingId != 0) {
                    parameters.set(SearchParameters.SearchParameter.CONTENT_RATING,
                                   ContentRating.values()[contentRatingId - 1]);
                }
                parameters.set(SearchParameters.SearchParameter.ORDER,
                               Order.values()[(int) ((Spinner) root.findViewById(R.id.order)).getSelectedItemId()]);
                try {
                    parameters.set(SearchParameters.SearchParameter.USER,
                                   User.createMutable()
                                       .set(User.UserKey.ID,
                                            Integer.parseInt(((EditText) root.findViewById(R.id.author)).getText()
                                                                                                        .toString())));
                } catch (NumberFormatException ignored) {}
                parameters.set(SearchParameters.SearchParameter.CHARACTERS_INCLUDED, l.getCharacters());
                log.debug("Search " + parameters);
                openSearch(parameters);
            }
        });
        return root;
    }
}
