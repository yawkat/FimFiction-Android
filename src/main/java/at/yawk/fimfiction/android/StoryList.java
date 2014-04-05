package at.yawk.fimfiction.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.*;
import android.widget.Button;
import at.yawk.fimfiction.data.SearchParameters;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import lombok.extern.log4j.Log4j;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Activity that displays a search.
 */
@Log4j
public class StoryList extends Fimtivity {
    private static final int REQUEST_CODE_SEARCH = 1;

    private StoryListWorker worker;

    private final Map<Button, SearchParameters> categoryButtons = Maps.newHashMap();
    private final List<Button> sortedCategoryButtons = Lists.newArrayList();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        worker = new StoryListWorker(helper());

        Intent intent = getIntent();
        ParamReader r;
        if (intent.hasExtra("search")) {
            r = intent.getParcelableExtra("search");
        } else {
            r = new ParamReader(helper().getParameterManager().getDefault());
        }

        for (Map.Entry<Button, SearchParameters> button : helper().getParameterManager()
                                                                  .findButtons(helper())
                                                                  .entrySet()) {
            prepareListButton(button.getKey(), button.getValue());
        }

        PullToRefreshLayout layout = helper().view(R.id.refresh);
        final PullToRefreshAttacher attacher = PullToRefreshAttacher.get(this);
        layout.setPullToRefreshAttacher(attacher, new PullToRefreshAttacher.OnRefreshListener() {
            @Override
            public void onRefreshStarted(View view) {
                replaceParameters(worker.getParameters(), true);
                attacher.setRefreshComplete();
            }
        });

        replaceParameters(r.getParameters(), false);

        helper().<Button>view(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent opener = new Intent(StoryList.this, SearchBuilderActivity.class);
                startActivityForResult(opener, REQUEST_CODE_SEARCH);
            }
        });
    }

    private void prepareListButton(Button button, final SearchParameters parameters) {
        categoryButtons.put(button, parameters);
        sortedCategoryButtons.add(button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceParameters(parameters, false);
                helper().<DrawerLayout>view(R.id.drawer).closeDrawer(Gravity.LEFT);
            }
        });
    }

    private void removeListButton(Button button) {
        categoryButtons.remove(button);
        sortedCategoryButtons.remove(button);
    }

    private void updateCategoryButtons() {
        int generated = 0;
        for (Button button : sortedCategoryButtons) {
            SearchParameters parameters = categoryButtons.get(button);
            TranslatableText name = helper().getParameterManager().getNameOrNull(parameters);
            if (name == null) {
                button.setText(helper().context().getResources().getString(R.string.search_entry, ++generated));
            } else {
                name.assign(button);
            }
            button.setBackgroundColor(worker.getParameters().equals(parameters) ? 0xFF333333 : 0);
        }
    }

    private void replaceParameters(SearchParameters parameters, boolean force) {
        worker.setParams(parameters);
        updateCategoryButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.mature).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getPreferences(MODE_PRIVATE).edit().putBoolean("mature", !helper().showMS()).commit();
                item.setTitle(helper().showMS() ? R.string.ms_hide : R.string.ms_show);
                worker.updateContent();
                return true;
            }
        });
        menu.findItem(R.id.mature).setTitle(helper().showMS() ? R.string.ms_hide : R.string.ms_show);
        menu.findItem(R.id.switch_account).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(StoryList.this, LoginActivity.class);
                intent.putExtra("autoLogin", false);
                helper().openActivity(intent, true);
                return true;
            }
        });
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case REQUEST_CODE_SEARCH:
            if (resultCode == RESULT_OK) {
                final SearchParameters parameters = data.<ParamReader>getParcelableExtra("parameters").getParameters();
                if (!categoryButtons.containsValue(parameters)) {
                    final View v = helper().layoutInflater().inflate(R.layout.search_button, null);
                    final Button button = (Button) v.findViewById(R.id.search_button);
                    prepareListButton(button, parameters);
                    final ViewGroup searches = helper().<ViewGroup>view(R.id.searches);
                    searches.addView(v);
                    v.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            searches.removeView(v);
                            removeListButton(button);
                            if (worker.getParameters().equals(parameters)) {
                                replaceParameters(helper().getParameterManager().getDefault(), false);
                            }
                        }
                    });
                }
                replaceParameters(parameters, false);
            }
            break;
        }
    }
}
