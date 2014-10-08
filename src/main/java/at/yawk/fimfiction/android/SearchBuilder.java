package at.yawk.fimfiction.android;

import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import at.yawk.fimfiction.data.*;
import com.google.common.collect.ImmutableSet;
import lombok.extern.log4j.Log4j;

/**
 * Activity helper that provides options for custom search.
 *
 * @author Yawkat
 */
@Log4j
public abstract class SearchBuilder {
    protected abstract void openSearch(SearchParameters parameters);

    public View createView(ActivityHelper helper) {
        final View root = helper.layoutInflater().inflate(R.layout.search, null);
        final TagManager.TagList l = helper.getTagManager().createTagList(helper, true);
        ((ViewGroup) root.findViewById(R.id.character_incl_container)).addView(l.getView());
        root.findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SearchParameters p = SearchParameters.createMutable();
                try {
                    p.set(SearchParameters.SearchParameter.SHELF,
                        Shelf.createMutable().set(Shelf.ShelfKey.ID, Integer.parseInt(((TextView) root.findViewById(R.id.query_shelf)).getText().toString())));
                } catch (NumberFormatException ignored) {
                }
                p.set(SearchParameters.SearchParameter.UNREAD,
                      ((CheckBox) root.findViewById(R.id.unread_checkbox)).isChecked());
                p.set(SearchParameters.SearchParameter.GORE, ((CheckBox) root.findViewById(R.id.gore)).isChecked());
                p.set(SearchParameters.SearchParameter.SEX, ((CheckBox) root.findViewById(R.id.sex)).isChecked());
                p.set(SearchParameters.SearchParameter.NAME,
                      ((EditText) root.findViewById(R.id.query_title)).getText().toString());

                int contentRatingId = (int) ((Spinner) root.findViewById(R.id.content_rating)).getSelectedItemId();
                if (contentRatingId != 0) {
                    p.set(SearchParameters.SearchParameter.CONTENT_RATING, ContentRating.values()[contentRatingId - 1]);
                }

                p.set(SearchParameters.SearchParameter.ORDER,
                      Order.values()[(int) ((Spinner) root.findViewById(R.id.order)).getSelectedItemId()]);

                try {
                    p.set(SearchParameters.SearchParameter.USER,
                          User.createMutable()
                              .set(User.UserKey.ID,
                                   Integer.parseInt(((EditText) root.findViewById(R.id.query_author)).getText()
                                                                                                     .toString())
                                  )
                         );
                } catch (NumberFormatException ignored) {}

                p.set(SearchParameters.SearchParameter.CHARACTERS_INCLUDED, ImmutableSet.copyOf(l.getCharacters()));
                p.set(SearchParameters.SearchParameter.CATEGORIES_INCLUDED, ImmutableSet.copyOf(l.getCategories()));

                log.debug("Search " + p);
                openSearch(p);
            }
        });
        return root;
    }
}
