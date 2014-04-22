package at.yawk.fimfiction.android;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import at.yawk.fimfiction.core.Search;
import at.yawk.fimfiction.data.SearchResult;
import at.yawk.fimfiction.data.Story;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.log4j.Log4j;

/**
 * @author Yawkat
 */
@Log4j
public class ExternalStoryOpenActivity extends Fimtivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.loading);

        helper().executeTask(new Runnable() {
            @Override
            public void run() {
                String url = getIntent().getDataString();
                Pattern number = Pattern.compile("[0-9]+");
                Matcher matcher = number.matcher(url);

                if (!matcher.find()) {
                    log.error("Failed to parse story ID from " + url);
                    error(R.string.error_cannot_parse);
                    return;
                }

                Story story = Story.createMutable().set(Story.StoryKey.ID, Integer.parseInt(matcher.group()));

                try {
                    SearchResult result = Search.create()
                                                .full()
                                                .story(story)
                                                .search(helper().getSession().getHttpClient());

                    final List<Story> stories = result.get(SearchResult.SearchResultKey.STORIES);

                    if (stories.size() != 1) {
                        log.error("Couldn't find story " + url);
                        error(R.string.error_cannot_find);
                        return;
                    }

                    helper().runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            show(stories.get(0));
                        }
                    });
                } catch (Throwable e) {
                    log.error("Failed to load story " + url, e);
                    error(R.string.error_cannot_load);
                }
            }
        });
    }

    private void show(Story story) {
        StoryDetail detail = new StoryDetail(story);
        View view = detail.createView(helper());
        setContentView(view);
    }

    private void error(final int id) {
        helper().runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setContentView(R.layout.error);
                helper().<TextView>view(R.id.error_message).setText(id);
            }
        });
    }
}
