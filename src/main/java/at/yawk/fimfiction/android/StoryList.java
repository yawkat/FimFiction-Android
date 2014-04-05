package at.yawk.fimfiction.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import at.yawk.fimfiction.data.SearchParameters;
import com.google.common.collect.Maps;
import java.util.Map;
import lombok.extern.log4j.Log4j;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Activity that displays a search.
 */
@Log4j
public class StoryList extends Fimtivity {
    private StoryListWorker worker;

    private Map<Button, SearchParameters> categoryButtons = Maps.newHashMap();

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
            r = new ParamReader(SearchParameterHelper.getDefault());
        }

        prepareListButton(helper().<Button>view(R.id.unread), SearchParameterHelper.UNREAD);
        prepareListButton(helper().<Button>view(R.id.favorite), SearchParameterHelper.FAVORITE);
        prepareListButton(helper().<Button>view(R.id.readlater), SearchParameterHelper.READ_LATER);

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
    }

    private void prepareListButton(Button button, final SearchParameters parameters) {
        categoryButtons.put(button, parameters);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                replaceParameters(parameters, false);
                helper().<DrawerLayout>view(R.id.drawer).closeDrawer(Gravity.LEFT);
            }
        });
    }

    private void updateListButton(Button button, SearchParameters parameters) {
        button.setBackgroundColor(worker.getParameters().equals(parameters) ? 0xFF333333 : 0);
    }

    private void updateCategoryButtons() {
        for (Map.Entry<Button, SearchParameters> entry : categoryButtons.entrySet()) {
            updateListButton(entry.getKey(), entry.getValue());
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
        menu.findItem(R.id.search).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SearchBuilderActivity.class);
                intent.putExtra("defaults", new ParamReader(worker.getParameters()));
                helper().openActivity(intent, false);
                return true;
            }
        });
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
}
