package at.yawk.fimfiction.android;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import at.yawk.fimfiction.data.FimCharacter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author Yawkat
 */
public class Characters implements Constants {
    private static final Executor loader = Executors.newFixedThreadPool(1);

    public static View createCharacterView(Activity context, final FimCharacter character) {
        final View v = context.getLayoutInflater().inflate(R.layout.character, null);
        loader.execute(new Runnable() {
            @Override
            public void run() {
                final Bitmap image = ImageCache.instance.getImage(character.getImageUrl());
                final View view = v.findViewById(R.id.icon);
                if (view.getHandler() != null) {
                    view.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            setImage((ImageView) view, image);
                        }
                    });
                } else {
                    setImage((ImageView) view, image);
                }
            }
        });
        return v;
    }

    private static void setImage(ImageView view, Bitmap image) {
        ((ImageView) view).setImageBitmap(Bitmap.createScaledBitmap(image,
                                                                    image.getWidth() * 2,
                                                                    image.getHeight() * 2,
                                                                    false));
    }

    public static CharacterList createCharacterList(final Activity context,
                                                    boolean editable,
                                                    FimCharacter... defaults) {
        ViewGroup v = (ViewGroup) context.getLayoutInflater().inflate(R.layout.character_list, null);
        final CharacterList l = new CharacterList(editable, v);
        if (editable) {
            View add = context.getLayoutInflater().inflate(R.layout.character_add, null);
            v.addView(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View v) {
                    final Dialog dialog = new Dialog(v.getContext());
                    CharacterList selection = createCharacterList(context, false);
                    for (final FimCharacter character : FimCharacter.DefaultCharacter.values()) {
                        if (!l.getCharacters().contains(character)) {
                            View view = selection.addCharacterView(context, character);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(final View v) {
                                    l.addCharacter(context, character);
                                    dialog.hide();
                                }
                            });
                        }
                    }
                    dialog.setCancelable(true);
                    dialog.setCanceledOnTouchOutside(true);
                    ScrollView scrollView = new ScrollView(context);
                    scrollView.addView(selection.getView());
                    dialog.setContentView(scrollView);
                    dialog.setTitle("Add Character");
                    dialog.show();
                }
            });
        }
        for (final FimCharacter character : defaults) {
            l.addCharacter(context, character);
        }
        return l;
    }

    public static class CharacterList {
        private final Set<FimCharacter> characters = new HashSet<FimCharacter>();
        private final boolean editable;
        private final ViewGroup view;

        public CharacterList(final boolean editable, final ViewGroup view) {
            this.editable = editable;
            this.view = view;
        }

        public ViewGroup getView() {
            return view;
        }

        private View addCharacterView(Activity context, final FimCharacter character) {
            final View v = createCharacterView(context, character);
            view.addView(v, view.getChildCount() - (editable ? 1 : 0));
            if (editable) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View c) {
                        removeCharacter(character, v);
                    }
                });
            }
            return v;
        }

        public View addCharacter(Activity context, FimCharacter character) {
            if (characters.add(character)) {
                return addCharacterView(context, character);
            } else {
                return null;
            }
        }

        public void removeCharacter(FimCharacter character, View view) {
            if (characters.remove(character)) { this.view.removeView(view); }
        }

        public Set<FimCharacter> getCharacters() {
            return Collections.unmodifiableSet(characters);
        }
    }
}
