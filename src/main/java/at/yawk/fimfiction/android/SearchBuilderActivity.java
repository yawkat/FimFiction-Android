package at.yawk.fimfiction.android;

import android.os.Bundle;
import at.yawk.fimfiction.data.SearchParameters;

/**
 * Activity for SearchBuilder.
 *
 * @author Yawkat
 */
public class SearchBuilderActivity extends Fimtivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SearchBuilder builder = new SearchBuilder(getIntent().hasExtra("defaults") ?
                                                          getIntent().<ParamReader>getParcelableExtra("defaults")
                                                                  .getParameters() :
                                                          SearchParameters.createImmutable()) {
            @Override
            protected void openSearch(SearchParameters parameters) {
                helper().openSearchActivity(parameters, R.string.search);
            }
        };
        setContentView(builder.createView(helper()));
    }
}
