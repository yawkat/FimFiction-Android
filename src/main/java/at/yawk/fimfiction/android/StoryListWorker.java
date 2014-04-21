package at.yawk.fimfiction.android;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import at.yawk.fimfiction.data.Chapter;
import at.yawk.fimfiction.data.ContentRating;
import at.yawk.fimfiction.data.SearchParameters;
import at.yawk.fimfiction.data.Story;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * @author Jonas Konrad (yawkat)
 */
@Log4j
@RequiredArgsConstructor
public class StoryListWorker {
    private final ActivityHelper helper;

    @Nullable private WorkerSession session = null;

    /**
     * Start the search thread.
     */
    private void start() {
        helper.global().getTaskManager().execute(session, session);
        helper.global().getTaskManager().interruptScheduler();
    }

    /**
     * Set the parameters of this worker, clear the story list and start searching.
     */
    public synchronized void setParams(SearchParameters params) {
        log.info("Changing parameters to " + params);
        session = new WorkerSession(new SearchView(helper, params), params);
        updateContent();
        updateTitle();
        start();
    }

    public void updateTitle() {
        helper.getParameterManager().getName(session.state).assignTitle(helper.activity());
    }

    /**
     * Updates the content list of the associated activity according to the current status of the SearchView.
     */
    public synchronized void updateContent() {
        final List<Story> content;
        if (session != null) {
            content = new ArrayList<Story>(session.view.getStories());
            if (!helper.showMature()) {
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
        } else { content = ImmutableList.of(); }
        helper.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                setContent(content);
            }
        });
    }

    /**
     * Replaces this lists content with the given story list.
     */
    @SuppressWarnings("unchecked")
    private void setContent(List<Story> content) {
        log.debug("Updating display (done=" + (session == null ? "null" : session.done) + ", " +
                  "content.length=" + content.size() + ")");
        ListView v = helper.view(R.id.stories);
        if (v.getAdapter() == null) {
            v.setAdapter(new ArrayAdapter<Story>(helper.context(), 0) {
                public View getView(int position, View convertView, ViewGroup parent) {
                    final Story story = getItem(position);
                    if (story == null) {
                        convertView = helper.layoutInflater()
                                            .inflate(session == null || session.done ? R.layout.done : R.layout.loading,
                                                     null);
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // force load one page
                                if (session != null) { session.skipLoadChecks = true; }
                            }
                        });
                    } else {
                        int read = 0;
                        for (Chapter chapter : story.<List<Chapter>>get(Story.StoryKey.CHAPTERS)) {
                            if (!chapter.getBoolean(Chapter.ChapterKey.UNREAD)) {
                                read++;
                            }
                        }
                        convertView = helper.layoutInflater().inflate(R.layout.story, null);
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
                        ((TextView) convertView.findViewById(R.id.title)).setText(story.getString(Story.StoryKey.TITLE));
                        ((TextView) convertView.findViewById(R.id.chapters)).setText(Integer.toString(story.getInt(Story.StoryKey.CHAPTER_COUNT)));
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

    /**
     * Opens a detail view for the given story.
     */
    private void detail(Story story) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(helper.context());
        dialog.setCancelable(true);
        StoryDetail storyDetail = new StoryDetail(story);
        storyDetail.setListUpdateCallback(new Runnable() {
            @Override
            public void run() {
                updateContent();
            }
        });
        dialog.setView(storyDetail.createView(helper));
        AlertDialog theDialog = dialog.create();
        theDialog.show();
    }

    /**
     * Downloads the given story, showing a progress dialog while doing so.
     */
    private void download(Story story) {
        final File target = new File(helper.baseDir(),
                                     "stories/" + Files.escape(story.getString(Story.StoryKey.TITLE)) + ".epub");
        new ProgressStoryDownloadTask(helper) {
            @Override
            protected void onPostExecute(Boolean result) {
                if (result) {
                    helper.openFileExternal(target,
                                            TranslatableText.id(R.string.missing_reader),
                                            R.string.missing_reader_link,
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    String pkgname = TranslatableText.id(R.string.missing_package_name)
                                                                                     .toString(helper);
                                                    try {
                                                        helper.openActivity(new Intent(Intent.ACTION_VIEW,
                                                                                       Uri.parse("market://details?id=" +
                                                                                                 pkgname)
                                                        ), false);
                                                    } catch (ActivityNotFoundException e) {
                                                        helper.openActivity(new Intent(Intent.ACTION_VIEW,
                                                                                       Uri.parse(
                                                                                               "https://play.google.com/store/apps/details?id=" +
                                                                                               pkgname
                                                                                                )
                                                        ), false);
                                                    }
                                                }
                                            }
                                           );
                }
                super.onPostExecute(result);
            }
        }.execute(new StoryDownloadTask.Params(story, target));
    }

    public SearchParameters getParameters() { return session.state; }

    @RequiredArgsConstructor
    private class WorkerSession implements Runnable, TaskManager.TaskContext {
        private final SearchView view;
        private final SearchParameters state;
        private boolean done = false;
        private boolean skipLoadChecks = false;

        @Override
        public void run() {
            while (!isInterrupted()) {
                if (step()) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) { break; }
                }
            }
            log.debug("We got interrupted, aborting!");
        }

        private boolean isInterrupted() { return Thread.interrupted(); }

        private boolean step() {
            if (!skipLoadChecks) {
                if (view == null) { return true; }

                ListView list = helper.view(R.id.stories);
                if (list.getLastVisiblePosition() < view.getStories().size() - 15) { return true; }
            } else {
                // only skip once
                skipLoadChecks = false;
            }

            return !continueLoading();
        }

        /**
         * Tries to load one more page.
         */
        private boolean continueLoading() {
            if (view.hasMore()) {
                updateContent();
                view.loadMoreData();
                    /*
                    if (view.isRelog()) {
                        LoginActivity.attemptedLogin = false;
                        Intent intent = new Intent();
                        intent.setClass(this, LoginActivity.class);
                        startIntent(intent, true);
                    }
                    */
            }
            boolean ndone = !view.hasMore();
            if (done != ndone) {
                if (ndone) { log.debug("No more data!"); }
                done = ndone;
                updateContent();
            }
            return !ndone;
        }

        @Override
        public boolean enabled() {
            return helper.enabled() && session == this;
        }
    }
}

