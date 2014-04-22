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

    public View createView(final ActivityHelper helper) {
        final View root = helper.layoutInflater().inflate(R.layout.story_detail, null);
        ((TextView) root.findViewById(R.id.title)).setText(story.getString(Story.StoryKey.TITLE));
        root.findViewById(R.id.title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                helper.downloadAndOpen(story);
            }
        });
        root.findViewById(R.id.title).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                helper.openAddressExternal((URL) story.get(Story.StoryKey.URL));
                return true;
            }
        });
        ((TextView) root.findViewById(R.id.author)).setText(story.<User>get(Story.StoryKey.AUTHOR)
                                                                 .getString(User.UserKey.NAME));
        ViewGroup statusTags = (ViewGroup) root.findViewById(R.id.status_tags);
        switch ((StoryStatus) story.get(Story.StoryKey.STATUS)) {
        case COMPLETED:
            statusTags.addView(TranslatableText.id(R.string.complete).textView(helper, R.layout.status_tag));
            break;
        case INCOMPLETE:
            statusTags.addView(TranslatableText.id(R.string.incomplete).textView(helper, R.layout.status_tag));
            break;
        case ON_HIATUS:
            statusTags.addView(TranslatableText.id(R.string.on_hiatus).textView(helper, R.layout.status_tag));
            break;
        case CANCELLED:
            statusTags.addView(TranslatableText.id(R.string.cancelled).textView(helper, R.layout.status_tag));
            break;
        }
        switch ((ContentRating) story.get(Story.StoryKey.CONTENT_RATING)) {
        case EVERYONE:
            statusTags.addView(TranslatableText.id(R.string.everyone).textView(helper, R.layout.status_tag, 0x89C738));
            break;
        case TEEN:
            statusTags.addView(TranslatableText.id(R.string.teen).textView(helper, R.layout.status_tag, 0xC78238));
            break;
        case MATURE:
            statusTags.addView(TranslatableText.id(R.string.mature).textView(helper, R.layout.status_tag, 0xC73838));
            break;
        }
        if (story.getBoolean(Story.StoryKey.SEX, false)) {
            statusTags.addView(TranslatableText.id(R.string.sex_short).textView(helper, R.layout.status_tag, 0x7B33DD));
        }
        if (story.getBoolean(Story.StoryKey.GORE, false)) {
            statusTags.addView(TranslatableText.id(R.string.gore_short)
                                               .textView(helper, R.layout.status_tag, 0xDD3333));
        }
        String html = story.<FormattedString>get(Story.StoryKey.DESCRIPTION)
                           .buildFormattedText(FormattedString.Markup.HTML);
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
                                               log.debug("Marking " + story.get(Story.StoryKey.ID) + " as readLater=" +
                                                         readLater);
                                               story.set(AccountActions.setReadLater(helper.getSession()
                                                                                           .getHttpClient(),
                                                                                     story,
                                                                                     readLater
                                                                                    ));
                                               if (root.getHandler() != null) {
                                                   root.getHandler().post(new Runnable() {
                                                       @Override
                                                       public void run() {
                                                           root.findViewById(R.id.readlater)
                                                               .setAlpha(story.getBoolean(Story.StoryKey.READ_LATER_STATE) ?
                                                                                 1 :
                                                                                 0.5F);
                                                       }
                                                   });
                                               }
                                           } catch (Exception e) {
                                               log.error("Could not change read later status for " +
                                                         story.get(Story.StoryKey.ID), e);
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
                                root.findViewById(R.id.cover).setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        helper.openFileExternal(helper.getImageCache().file(getImage()),
                                                                TranslatableText.id(R.string.missing_gallery),
                                                                0,
                                                                null);
                                    }
                                });
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
                ((ViewGroup) root.findViewById(R.id.chapter_list)).addView(separator);
            }
            first = false;
            final View chapterView = helper.layoutInflater().inflate(R.layout.chapter, null);
            ((TextView) chapterView.findViewById(R.id.title)).setText(chapter.getString(Chapter.ChapterKey.TITLE));
            chapterView.findViewById(R.id.unread_query)
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
                                            chapterView.findViewById(R.id.unread_query)
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
            ((ViewGroup) root.findViewById(R.id.chapter_list)).addView(chapterView);
        }
        TagManager.TagList list = helper.getTagManager().createTagList(false, story);
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
