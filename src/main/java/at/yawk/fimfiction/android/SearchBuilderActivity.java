package at.yawk.fimfiction.android;

import android.content.Intent;
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
                Intent result = new Intent();
                result.putExtra("parameters", new ParamReader(parameters));
                setResult(RESULT_OK, result);
                finish();
            }
        };
        setContentView(builder.createView(helper()));
    }
}
