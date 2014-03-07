package at.yawk.fimfiction.android;

import android.graphics.Bitmap;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import at.yawk.fimfiction.core.AccountActions;
import at.yawk.fimfiction.data.*;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author Yawkat
 */
public class StoryDetail implements Constants {
    private Runnable listUpdateCallback = new Runnable() {
        @Override
        public void run() {
        }
    };
    private Story story;

    public StoryDetail(final Story story) {
        this.story = story;
    }

    public void setListUpdateCallback(final Runnable listUpdateCallback) {
        this.listUpdateCallback = listUpdateCallback;
    }

    public View createView(final Fimtivity context) {
        final View root = View.inflate(context, R.layout.story_detail, null);
        ((TextView) root.findViewById(R.id.title)).setText(story.getString(Story.StoryKey.TITLE));
        ((TextView) root.findViewById(R.id.author)).setText(story.<User>get(Story.StoryKey.AUTHOR)
                                                                 .getString(User.UserKey.NAME));
        String html = story.<FormattedString>get(Story.StoryKey.DESCRIPTION)
                           .buildFormattedText(FormattedString.Markup.HTML);
        ((TextView) root.findViewById(R.id.description)).setText(Html.fromHtml(html));
        root.findViewById(R.id.favorite)
            .setAlpha(story.<FavoriteState>get(Story.StoryKey.FAVORITE_STATE).isFavorited() ? 1 : 0.5F);
        root.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                BrowserApp.runTask(context, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FavoriteState n = story.<FavoriteState>get(Story.StoryKey.FAVORITE_STATE).isFavorited() ?
                                    FavoriteState.NOT_FAVORITED :
                                    FavoriteState.FAVORITED;
                            Log.d(TAG, "Marking " + story.getInt(Story.StoryKey.ID) + " as " + n);
                            story.set(AccountActions.setFavorite(session.getHttpClient(), story, n));
                            if (root.getHandler() != null) {
                                root.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        root.findViewById(R.id.favorite)
                                            .setAlpha(story.<FavoriteState>get(Story.StoryKey.FAVORITE_STATE)
                                                           .isFavorited() ? 1 : 0.5F);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Could not change favorite status for " + story.getInt(Story.StoryKey.ID), e);
                        }
                    }
                });
            }
        });
        root.findViewById(R.id.readlater).setAlpha(story.getBoolean(Story.StoryKey.READ_LATER_STATE) ? 1 : 0.5F);
        root.findViewById(R.id.readlater).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                BrowserApp.runTask(context, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean readLater = !story.getBoolean(Story.StoryKey.READ_LATER_STATE);
                            Log.d(TAG, "Marking " + story.get(Story.StoryKey.ID) + " as readLater=" + readLater);
                            story.set(AccountActions.setReadLater(session.getHttpClient(), story, readLater));
                            if (root.getHandler() != null) {
                                root.getHandler().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        root.findViewById(R.id.readlater)
                                            .setAlpha(story.getBoolean(Story.StoryKey.READ_LATER_STATE) ? 1 : 0.5F);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Could not change read later status for " + story.get(Story.StoryKey.ID), e);
                        }
                    }
                }

                                  );
            }
        });
        if (getImage() != null) {
            BrowserApp.runTask(context, new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = Connectivity.bigDownloads(context) ?
                            imageCache.getImage(getImage()) :
                            imageCache.getCachedImage(getImage());
                    if (bitmap != null) {
                        new Handler(context.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                ((ImageView) root.findViewById(R.id.cover)).setImageBitmap(bitmap);
                                root.findViewById(R.id.cover).setVisibility(View.VISIBLE);
                            }
                        });
                    }
                }
            });
        }

        boolean first = true;
        for (final Chapter chapter : story.<List<Chapter>>get(Story.StoryKey.CHAPTERS)) {
            if (!first) {
                View separator = new View(context);
                separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(0x88FFFFFF);
                ((ViewGroup) root.findViewById(R.id.chapters)).addView(separator);
            }
            first = false;
            final View chapterView = View.inflate(context, R.layout.chapter, null);
            ((TextView) chapterView.findViewById(R.id.title)).setText(chapter.getString(Chapter.ChapterKey.TITLE));
            chapterView.findViewById(R.id.unread)
                       .setVisibility(chapter.getBoolean(Chapter.ChapterKey.UNREAD) ? View.VISIBLE : View.GONE);
            chapterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    BrowserApp.runTask(context, new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.e(TAG, "UPDATE_READ");
                                chapter.set(AccountActions.toggleRead(session.getHttpClient(), chapter));
                                listUpdateCallback.run();
                                if (chapterView.getHandler() != null) {
                                    chapterView.getHandler().post(new Runnable() {
                                        @Override
                                        public void run() {
                                            chapterView.findViewById(R.id.unread)
                                                       .setVisibility(chapter.getBoolean(Chapter.ChapterKey.UNREAD) ?
                                                                              View.VISIBLE :
                                                                              View.GONE);
                                        }
                                    });
                                }
                            } catch (Exception e) {
                                Log.e(TAG,
                                      "Could not toggle read status for " + chapter.getInt(Chapter.ChapterKey.ID),
                                      e);
                            }
                        }
                    });
                }
            });
            ((ViewGroup) root.findViewById(R.id.chapters)).addView(chapterView);
        }
        if (SHOW_CHARACTERS) {
            final Set<FimCharacter> characters = story.get(Story.StoryKey.CHARACTERS);
            Characters.CharacterList list = Characters.createCharacterList(context,
                                                                           false,
                                                                           characters.toArray(new FimCharacter[characters
                                                                                   .size()]));
            list.getView()
                .setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                 ViewGroup.LayoutParams.WRAP_CONTENT));
            ((ViewGroup) root.findViewById(R.id.characters)).addView(list.getView());
        }
        return root;
    }

    private URL getImage() {
        return story.<Optional<URL>>get(Story.StoryKey.URL_COVER).getOrNull();
    }
}
