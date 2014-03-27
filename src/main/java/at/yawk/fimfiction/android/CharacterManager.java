package at.yawk.fimfiction.android;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import at.yawk.fimfiction.data.Category;
import at.yawk.fimfiction.data.FimCharacter;
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
public class CharacterManager {
    private final Helper helper;

    public CharacterList createCharacterList(Helper helper, boolean editable) {
        return createCharacterList(helper, editable, ImmutableSet.<Tag>of());
    }

    public CharacterList createCharacterList(Helper helper,
                                             boolean editable,
                                             Iterable<? extends FimCharacter> characters,
                                             Iterable<Category> categories) {
        return createCharacterList(helper, editable, toTags(characters, categories));
    }

    public CharacterList createCharacterList(final Helper helper, boolean editable, Iterable<Tag> tags) {
        ViewGroup v = (ViewGroup) helper.layoutInflater().inflate(R.layout.character_list, null);
        final CharacterList l = new CharacterList(helper, editable, v);
        if (editable) {
            View add = helper.layoutInflater().inflate(R.layout.character_add, null);
            v.addView(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(helper.context());
                    CharacterList selection = createCharacterList(helper, false, ImmutableSet.<Tag>of());
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
                    dialog.setTitle("Add Character");
                    dialog.show();
                }
            });
        }
        for (Tag tag : tags) { l.addTag(tag); }
        return l;
    }

    private static Iterable<Tag> getAllTags() {
        return toTags(Arrays.asList(FimCharacter.DefaultCharacter.values()), Arrays.asList(Category.values()));
    }

    private static Iterable<Tag> toTags(Iterable<? extends FimCharacter> characters, Iterable<Category> categories) {
        List<Tag> result = Lists.newArrayList();
        for (Category category : categories) {
            result.add(new CategoryTag(category));
        }
        for (FimCharacter character : characters) {
            result.add(new CharacterTag(character));
        }
        return result;
    }

    @RequiredArgsConstructor
    public static class CharacterList {
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
                                               return ((CharacterTag) tag).character;
                                           }
                                       });
        }

        public Iterable<Category> getCategories() {
            return Iterables.transform(Iterables.filter(tags, Predicates.instanceOf(CategoryTag.class)),
                                       new Function<Tag, Category>() {
                                           @Nullable
                                           @Override
                                           public Category apply(@Nullable Tag tag) {
                                               return ((CategoryTag) tag).category;
                                           }
                                       });
        }
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static abstract class Tag {
        protected abstract View createView(Helper helper);
    }

    @RequiredArgsConstructor
    @Getter
    @EqualsAndHashCode
    public static class CharacterTag extends Tag {
        private final FimCharacter character;

        @Override
        protected View createView(final Helper helper) {
            final View v = helper.layoutInflater().inflate(R.layout.character, null);
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
    @EqualsAndHashCode
    public static class CategoryTag extends Tag {
        private final Category category;

        @Override
        protected View createView(Helper helper) {
            View view = helper.layoutInflater().inflate(R.layout.category, null);
            CharSequence[] categories = helper.context().getResources().getTextArray(R.array.category);
            ((TextView) view.findViewById(R.id.category)).setText(categories[category.ordinal()]);
            return view;
        }
    }
}
