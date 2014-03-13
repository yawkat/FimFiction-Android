package at.yawk.fimfiction.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.yawk.fimfiction.data.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.extern.log4j.Log4j;

/**
 * Activity that displays a search.
 */
@Log4j
public class StoryList extends Fimtivity {
    private static final SearchParameters UNREAD = SearchParameters.createMutable()
                                                                   .set(SearchParameters.SearchParameter.ORDER,
                                                                        Order.UPDATE_DATE)
                                                                   .set(SearchParameters.SearchParameter.UNREAD, true)
                                                                   .set(SearchParameters.SearchParameter.FAVORITED,
                                                                        true)
                                                                   .immutableCopy();
    private static final SearchParameters FAVORITE = SearchParameters.createMutable()
                                                                     .set(SearchParameters.SearchParameter.ORDER,
                                                                          Order.UPDATE_DATE)
                                                                     .set(SearchParameters.SearchParameter.FAVORITED,
                                                                          true)
                                                                     .immutableCopy();
    private static final SearchParameters READ_LATER = SearchParameters.createMutable()
                                                                       .set(SearchParameters.SearchParameter.ORDER,
                                                                            Order.UPDATE_DATE)
                                                                       .set(SearchParameters.SearchParameter.READ_LATER,
                                                                            true)
                                                                       .immutableCopy();

    private SearchView view;
    private SearchParameters state;
    private boolean done = false;
    private boolean skipLoadChecks = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = getIntent();
        ParamReader r;
        if (intent.hasExtra("search")) {
            r = intent.getParcelableExtra("search");
        } else {
            r = new ParamReader(UNREAD, getResources().getString(R.string.unread));
        }
        setParams(r.getParameters());
        setTitle(r.getTitle());
        helper().executeTask(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted()) {
                    if (step()) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            break;
                        }
                    }
                }
                log.debug("We got interrupted, aborting!");
            }

            private boolean step() {
                if (!skipLoadChecks) {
                    if (view == null) { return true; }

                    ListView list = (ListView) findViewById(R.id.stories);
                    if (list.getLastVisiblePosition() < view.getStories().size() - 15) { return true; }
                } else {
                    // only skip once
                    skipLoadChecks = false;
                }

                return !continueLoading();
            }
        });
    }

    private synchronized void setParams(SearchParameters params) {
        state = params;
        view = new SearchView(helper(), params);
        updateContent();
    }

    private boolean continueLoading() {
        SearchView view = this.view;
        if (view.hasMore()) {
            updateContent();
            view.loadMoreData();
            /*if (view.isRelog()) {
                LoginActivity.attemptedLogin = false;
                Intent intent = new Intent();
                intent.setClass(this, LoginActivity.class);
                startIntent(intent, true);
            }*/
        }
        boolean ndone = !view.hasMore();
        if (done != ndone) {
            if (ndone) { log.debug("No more data!"); }
            done = ndone;
            updateContent();
        }
        return !ndone;
    }

    private synchronized void updateContent() {
        final List<Story> content = new ArrayList<Story>(view.getStories());
        if (!showMS()) {
            Iterator<Story> itr = content.iterator();
            while (itr.hasNext()) {
                Story story = itr.next();
                log.debug(story.toString());
                if (story.getBoolean(Story.StoryKey.SEX) &&
                    story.get(Story.StoryKey.CONTENT_RATING) == ContentRating.MATURE) {
                    itr.remove();
                }
            }
            log.debug("page done");
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setContent(content);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void setContent(List<Story> content) {
        log.debug("Updating display (done=" + done + ", content.length=" + content.size() + ")");
        ListView v = (ListView) findViewById(R.id.stories);
        if (v.getAdapter() == null) {
            v.setAdapter(new ArrayAdapter<Story>(this, 0) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    final Story story = getItem(position);
                    if (story == null) {
                        convertView = getLayoutInflater().inflate(done ? R.layout.done : R.layout.loading, null);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // force load one page
                                skipLoadChecks = true;
                            }
                        });
                    } else {
                        int read = 0;
                        for (Chapter chapter : story.<List<Chapter>>get(Story.StoryKey.CHAPTERS)) {
                            if (!chapter.getBoolean(Chapter.ChapterKey.UNREAD)) {
                                read++;
                            }
                        }
                        convertView = getLayoutInflater().inflate(R.layout.story, null);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                download(story);
                            }
                        });
                        convertView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                detail(story);
                                return true;
                            }
                        });
                        ((TextView) convertView.findViewById(R.id.title)).setText(story.getString(Story.StoryKey
                                                                                                          .TITLE));
                        ((TextView) convertView.findViewById(R.id.chapters)).setText(Integer.toString(story.getInt
                                (Story.StoryKey.CHAPTER_COUNT)));
                        ((TextView) convertView.findViewById(R.id.read)).setText(Integer.toString(read));
                    }
                    return convertView;
                }
            });
        }
        ((ArrayAdapter<Story>) v.getAdapter()).clear();
        ((ArrayAdapter<Story>) v.getAdapter()).addAll(content);
        ((ArrayAdapter<Story>) v.getAdapter()).add(null); // progress
    }

    private void detail(Story story) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCancelable(true);
        StoryDetail storyDetail = new StoryDetail(story);
        storyDetail.setListUpdateCallback(new Runnable() {
            @Override
            public void run() {
                updateContent();
            }
        });
        dialog.setView(storyDetail.createView(helper()));
        AlertDialog theDialog = dialog.create();
        theDialog.show();
    }

    private void download(Story story) {
        StringBuilder fileBuilder = new StringBuilder();
        fileBuilder.append("stories/");
        fileBuilder.append(Files.escape(story.getString(Story.StoryKey.TITLE)));
        fileBuilder.append(".epub");
        final File target = new File(helper().baseDir(), fileBuilder.toString());
        new ProgressStoryDownloadTask(helper()) {
            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(target),
                                          MimeTypeMap.getSingleton().getMimeTypeFromExtension("epub"));
                    startActivity(intent);
                }
                super.onPostExecute(result);
            }
        }.execute(new StoryDownloadTask.Params(story, target));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu, menu);
        menu.findItem(R.id.unread).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                helper().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        helper().openSearchActivity(UNREAD, R.string.unread);
                    }
                });
                return true;
            }
        });
        menu.findItem(R.id.readlater).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                helper().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        helper().openSearchActivity(READ_LATER, R.string.readlater);
                    }
                });
                return true;
            }
        });
        menu.findItem(R.id.favorite).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                helper().executeTask(new Runnable() {
                    @Override
                    public void run() {
                        helper().openSearchActivity(FAVORITE, R.string.favorites);
                    }
                });
                return true;
            }
        });
        menu.findItem(R.id.search).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SearchBuilderActivity.class);
                if (!state.equals(FAVORITE) && !state.equals(READ_LATER) && !state.equals(UNREAD)) {
                    intent.putExtra("defaults", new ParamReader(state, "Search"));
                }
                helper().openActivity(intent, false);
                return true;
            }
        });
        menu.findItem(R.id.mature).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                getPreferences(MODE_PRIVATE).edit().putBoolean("mature", !showMS()).commit();
                item.setTitle(showMS() ? R.string.ms_hide : R.string.ms_show);
                updateContent();
                return true;
            }
        });
        menu.findItem(R.id.mature).setTitle(showMS() ? R.string.ms_hide : R.string.ms_show);
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

    private boolean showMS() {
        return getPreferences(MODE_PRIVATE).getBoolean("mature", true);
    }
}
