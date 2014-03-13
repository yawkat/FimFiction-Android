package at.yawk.fimfiction.android;

import android.util.Log;
import at.yawk.fimfiction.core.Search;
import at.yawk.fimfiction.core.SearchUrl;
import at.yawk.fimfiction.data.Optional;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.data.SearchResult;
import at.yawk.fimfiction.data.Story;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.xml.sax.SAXException;

/**
 * @author Jonas Konrad (yawkat)
 */
public class SearchView {
    private final Helper helper;
    private final SearchUrl.CompiledSearchParameters parameters;

    private List<Story> stories;
    private int page;
    private boolean hasMore;

    private boolean relog = false;

    public SearchView(Helper helper, SearchParameters parameters) {
        this.helper = helper;
        this.parameters = SearchUrl.CompiledSearchParameters.compile(parameters);
        reset();
    }

    public synchronized void reset() {
        stories = new ArrayList<Story>();
        page = 0;
        hasMore = true;
    }

    private synchronized void loadMoreData(int page) throws Exception {
        Search request = Search.create();
        request.parameters(parameters, page);
        SearchResult result = request.search(helper.getSession().getHttpClient());
        relog = result.has(SearchResult.SearchResultKey.LOGGED_IN_USER) &&
                !result.<Optional>get(SearchResult.SearchResultKey.LOGGED_IN_USER).exists();
        List<Story> storyList = Lists.transform(result.<List<Story>>get(SearchResult.SearchResultKey.STORIES),
                                                new Function<Story, Story>() {
                                                    @Nullable
                                                    @Override
                                                    public Story apply(@Nullable Story input) {
                                                        return input == null ? null : input.mutableVersion();
                                                    }
                                                });
        stories.addAll(storyList);
        hasMore = !(storyList/*.isEmpty() replaced for performance */.size() < 10);
        Log.d(Constants.TAG, "Loaded data on page " + page + " (" + storyList.size() + " " + getStories().size() + ")");
    }

    public synchronized void loadMoreData() {
        try {
            loadMoreData(page);
            page++;
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not load more data on page " + page, e);
            if (e instanceof SAXException) {
                page++;
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public List<Story> getStories() {
        return Collections.unmodifiableList(stories);
    }

    public boolean hasMore() {
        return hasMore;
    }

    public boolean isRelog() {
        return relog;
    }
}
