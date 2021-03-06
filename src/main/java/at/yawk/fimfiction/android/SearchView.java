package at.yawk.fimfiction.android;

import at.yawk.fimfiction.core.Search;
import at.yawk.fimfiction.core.SearchUrl;
import at.yawk.fimfiction.data.Optional;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.data.SearchResult;
import at.yawk.fimfiction.data.Story;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import lombok.Getter;
import lombok.extern.log4j.Log4j;
import org.xml.sax.SAXException;

/**
 * Wrapper class for FimFiction-Java search functions.
 *
 * @author Jonas Konrad (yawkat)
 */
@Log4j
public class SearchView {
    private final Helper helper;
    private final SearchUrl.CompiledSearchParameters parameters;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") @Getter private List<Story> stories;
    private int page;
    private boolean hasMore;

    @Getter private boolean relog = false;

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
                                                }
                                               );
        stories.addAll(storyList);
        hasMore = !(storyList/*.isEmpty() replaced for performance */.size() < 10);
        log.debug("Loaded data on page " + page + " (" + storyList.size() + " " + getStories().size() + ")");
    }

    public synchronized void loadMoreData() {
        try {
            loadMoreData(page);
            page++;
        } catch (Exception e) {
            log.error("Could not load more data on page " + page, e);
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

    public boolean hasMore() {
        return hasMore;
    }
}
