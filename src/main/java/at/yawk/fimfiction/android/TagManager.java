package at.yawk.fimfiction.android;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import at.yawk.fimfiction.data.Category;
import at.yawk.fimfiction.data.FimCharacter;
import at.yawk.fimfiction.data.Story;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import lombok.*;

/**
 * Manager for character icons.
 *
 * @author Yawkat
 */
@RequiredArgsConstructor
public class TagManager {
    private final Helper helper;

    public TagList createTagList(ActivityHelper helper, boolean editable) {
        return createTagList(helper, editable, ImmutableSet.<Tag>of());
    }

    public TagList createTagList(ActivityHelper helper, boolean editable, Story data) {
        return createTagList(helper, editable, toTags(data));
    }

    public TagList createTagList(final ActivityHelper helper, boolean editable, Iterable<Tag> tags) {
        ViewGroup v = (ViewGroup) helper.layoutInflater().inflate(R.layout.character_list, null);
        final TagList l = new TagList(helper, editable, v);
        if (editable) {
            View add = helper.layoutInflater().inflate(R.layout.tag_add, null);
            v.addView(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(helper.activity());
                    TagList selection = createTagList(helper, false, ImmutableSet.<Tag>of());
                    for (final Tag tag : getAllTags()) {
                        if (!l.tags.contains(tag)) {
                            View view = selection.addTagView(tag);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    l.addTag(tag);
                                    dialog.hide();
                                }
                            });
                        }
                    }
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    ScrollView scrollView = new ScrollView(helper.context());
                    scrollView.addView(selection.getView());
                    dialog.setContentView(scrollView);
                    dialog.setTitle(R.string.add_tag);
                    dialog.show();
                }
            });
        }
        for (Tag tag : tags) { l.addTag(tag); }
        return l;
    }

    private Iterable<Tag> getAllTags() {
        List<Tag> tags = Lists.newArrayList();
        appendCategoriesAndCharacters(tags,
                                      Arrays.asList(Category.values()),
                                      Arrays.<FimCharacter>asList(FimCharacter.DefaultCharacter.values()));
        return tags;
    }

    @SuppressWarnings("unchecked")
    private Iterable<Tag> toTags(Story data) {
        List<Tag> result = Lists.newArrayList();
        appendCategoriesAndCharacters(result,
                                      (Iterable<Category>) data.get(Story.StoryKey.CATEGORIES),
                                      (Iterable<FimCharacter>) data.get(Story.StoryKey.CHARACTERS));
        return result;
    }

    private void appendCategoriesAndCharacters(List<Tag> result,
                                               Iterable<Category> categories,
                                               Iterable<FimCharacter> characters) {
        for (Category category : categories) {
            result.add(new CategoryTag(helper, category));
        }
        for (FimCharacter character : characters) {
            result.add(new CharacterTag(character));
        }
    }

    @RequiredArgsConstructor
    public static class TagList {
        private final Helper helper;
        private final Set<Tag> tags = new HashSet<Tag>();
        private final boolean editable;
        @Getter private final ViewGroup view;

        private View addTagView(final Tag tag) {
            final View v = tag.createView(helper);
            view.addView(v, view.getChildCount() - (editable ? 1 : 0));
            if (editable) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View c) {
                        removeTag(tag, v);
                    }
                });
            }
            return v;
        }

        public View addTag(Tag tag) {
            if (tags.add(tag)) {
                return addTagView(tag);
            } else { return null; }
        }

        public void removeTag(Tag tag, View view) {
            if (tags.remove(tag)) { this.view.removeView(view); }
        }

        public Iterable<FimCharacter> getCharacters() {
            return Iterables.transform(Iterables.filter(tags, Predicates.instanceOf(CharacterTag.class)),
                                       new Function<Tag, FimCharacter>() {
                                           @Nullable
                                           @Override
                                           public FimCharacter apply(@Nullable Tag tag) {
                                               assert tag != null;
                                               return ((CharacterTag) tag).character;
                                           }
                                       }
                                      );
        }

        public Iterable<Category> getCategories() {
            return Iterables.transform(Iterables.filter(tags, Predicates.instanceOf(CategoryTag.class)),
                                       new Function<Tag, Category>() {
                                           @Nullable
                                           @Override
                                           public Category apply(@Nullable Tag tag) {
                                               assert tag != null;
                                               return ((CategoryTag) tag).category;
                                           }
                                       }
                                      );
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static abstract class Tag {
        protected abstract View createView(Helper helper);
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class CharacterTag extends Tag {
        private final FimCharacter character;

        @Override
        protected View createView(final Helper helper) {
            final View v = helper.layoutInflater().inflate(R.layout.tag_character, null);
            helper.executeGlobalTask(new Runnable() {
                @Override
                public void run() {
                    final Bitmap image = helper.getImageCache().getImage(character.getImageUrl());
                    final View view = v.findViewById(R.id.icon);
                    helper.runOnMainThread(new Runnable() {
                        @Override
                        public void run() { setImage((ImageView) view, image); }
                    });
                }
            });
            return v;
        }

        private static void setImage(ImageView view, Bitmap image) {
            view.setImageBitmap(Bitmap.createScaledBitmap(image, image.getWidth() * 2, image.getHeight() * 2, false));
        }
    }


    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode(callSuper = false)
    private static class TextTag extends Tag {
        private final TranslatableText name;
        private final int color;

        @Override
        protected View createView(Helper helper) {
            View view = helper.layoutInflater().inflate(R.layout.tag_category, null);
            name.assign((TextView) view.findViewById(R.id.category));
            if (color != 0) {
                // replace color
                view.findViewById(R.id.colored).getBackground().setColorFilter(color | 0xFF000000, PorterDuff.Mode.SRC);
            }
            return view;
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = false)
    public static class CategoryTag extends TextTag {
        private final Category category;

        public CategoryTag(Helper helper, Category category) {
            super(TranslatableText.string(name(helper, category)), color(category));
            this.category = category;
        }

        private static int color(Category category) {
            switch (category) {
            case ADVENTURE:
                return 0x45c950;
            case ALTERNATE_UNIVERSE:
                return 0x888888;
            case ANTHRO:
                return 0xb5695a;
            case COMEDY:
                return 0xcaa600;
            case CROSSOVER:
                return 0x47b8a0;
            case DARK:
                return 0x982323;
            case HUMAN:
                return 0xb5835a;
            case RANDOM:
                return 0x3f74ce;
            case ROMANCE:
                return 0x773db3;
            case TRAGEDY:
                return 0xe09d2b;
            case SAD:
                return 0xd95e87;
            case SLICE_OF_LIFE:
                return 0x3f49cf;
            default:
                return 0;
            }
        }

        private static CharSequence name(Helper helper, Category category) {
            CharSequence[] categories = helper.context().getResources().getTextArray(R.array.category);
            return categories[category.ordinal()];
        }
    }
}
