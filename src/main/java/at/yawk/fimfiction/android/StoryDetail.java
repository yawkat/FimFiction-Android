package at.yawk.fimfiction.android;

import android.graphics.Bitmap;
import android.text.Html;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j;

/**
 * View factory to display information about a story.
 *
 * @author Yawkat
 */
@Log4j
@RequiredArgsConstructor
public class StoryDetail {
    private Runnable listUpdateCallback = new Runnable() {
        @Override
        public void run() {}
    };
    private final Story story;

    public void setListUpdateCallback(Runnable listUpdateCallback) {
        this.listUpdateCallback = listUpdateCallback;
    }

    public View createView(final Helper helper) {
        final View root = helper.layoutInflater().inflate(R.layout.story_detail, null);
        ((TextView) root.findViewById(R.id.title)).setText(story.getString(Story.StoryKey.TITLE));
        ((TextView) root.findViewById(R.id.author)).setText(story.<User>get(Story.StoryKey.AUTHOR)
                                                                 .getString(User.UserKey.NAME));
        String html =
                story.<FormattedString>get(Story.StoryKey.DESCRIPTION).buildFormattedText(FormattedString.Markup.HTML);
        ((TextView) root.findViewById(R.id.description)).setText(Html.fromHtml(html));
        root.findViewById(R.id.favorite)
            .setAlpha(story.<FavoriteState>get(Story.StoryKey.FAVORITE_STATE).isFavorited() ? 1 : 0.5F);
        root.findViewById(R.id.favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.executeTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            FavoriteState n = story.<FavoriteState>get(Story.StoryKey.FAVORITE_STATE).isFavorited() ?
                                    FavoriteState.NOT_FAVORITED :
                                    FavoriteState.FAVORITED;
                            log.debug("Marking " + story.getInt(Story.StoryKey.ID) + " as " + n);
                            story.set(AccountActions.setFavorite(helper.getSession().getHttpClient(), story, n));
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
                            log.error("Could not change favorite status for " + story.getInt(Story.StoryKey.ID), e);
                        }
                    }
                });
            }
        });
        root.findViewById(R.id.readlater).setAlpha(story.getBoolean(Story.StoryKey.READ_LATER_STATE) ? 1 : 0.5F);
        root.findViewById(R.id.readlater).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.executeTask(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean readLater = !story.getBoolean(Story.StoryKey.READ_LATER_STATE);
                            log.debug("Marking " + story.get(Story.StoryKey.ID) + " as readLater=" + readLater);
                            story.set(AccountActions.setReadLater(helper.getSession().getHttpClient(),
                                                                  story,
                                                                  readLater));
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
                            log.error("Could not change read later status for " + story.get(Story.StoryKey.ID), e);
                        }
                    }
                }

                );
            }
        });
        if (getImage() != null) {
            helper.executeTask(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = helper.bigDownloads() ?
                            helper.getImageCache().getImage(getImage()) :
                            helper.getImageCache().getCachedImage(getImage());
                    if (bitmap != null) {
                        helper.runOnMainThread(new Runnable() {
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
                View separator = new View(helper.context());
                separator.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                separator.setBackgroundColor(0x88FFFFFF);
                ((ViewGroup) root.findViewById(R.id.chapters)).addView(separator);
            }
            first = false;
            final View chapterView = helper.layoutInflater().inflate(R.layout.chapter, null);
            ((TextView) chapterView.findViewById(R.id.title)).setText(chapter.getString(Chapter.ChapterKey.TITLE));
            chapterView.findViewById(R.id.unread)
                       .setVisibility(chapter.getBoolean(Chapter.ChapterKey.UNREAD) ? View.VISIBLE : View.GONE);
            chapterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    helper.executeTask(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                chapter.set(AccountActions.toggleRead(helper.getSession().getHttpClient(), chapter));
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
                                log.error("Could not toggle read status for " + chapter.getInt(Chapter.ChapterKey.ID),
                                          e);
                            }
                        }
                    });
                }
            });
            ((ViewGroup) root.findViewById(R.id.chapters)).addView(chapterView);
        }
        Set<FimCharacter> characters = story.get(Story.StoryKey.CHARACTERS);
        CharacterManager.CharacterList list = helper.getCharacterManager()
                                                    .createCharacterList(false,
                                                                         characters.toArray(new
                                                                                                    FimCharacter[characters.size()]));
        list.getView()
            .setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                             ViewGroup.LayoutParams.WRAP_CONTENT));
        ((ViewGroup) root.findViewById(R.id.characters)).addView(list.getView());
        return root;
    }

    private URL getImage() {
        return story.<Optional<URL>>get(Story.StoryKey.URL_COVER).getOrNull();
    }
}
