package at.yawk.fimfiction.android;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import at.yawk.fimfiction.data.FimCharacter;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Manager for character icons.
 *
 * @author Yawkat
 */
@RequiredArgsConstructor
public class CharacterManager {
    private final Helper helper;

    public View createCharacterView(final FimCharacter character) {
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

    private void setImage(ImageView view, Bitmap image) {
        view.setImageBitmap(Bitmap.createScaledBitmap(image, image.getWidth() * 2, image.getHeight() * 2, false));
    }

    public CharacterList createCharacterList(final Helper helper, boolean editable, FimCharacter... defaults) {
        ViewGroup v = (ViewGroup) helper.layoutInflater().inflate(R.layout.character_list, null);
        final CharacterList l = new CharacterList(helper, editable, v);
        if (editable) {
            View add = helper.layoutInflater().inflate(R.layout.character_add, null);
            v.addView(add);
            add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(helper.context());
                    CharacterList selection = createCharacterList(helper, false);
                    for (final FimCharacter character : FimCharacter.DefaultCharacter.values()) {
                        if (!l.getCharacters().contains(character)) {
                            View view = selection.addCharacterView(character);
                            view.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    l.addCharacter(character);
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
        for (FimCharacter character : defaults) { l.addCharacter(character); }
        return l;
    }

    @RequiredArgsConstructor
    public static class CharacterList {
        private final Helper helper;
        @Getter private final Set<FimCharacter> characters = new HashSet<FimCharacter>();
        private final boolean editable;
        @Getter private final ViewGroup view;

        private View addCharacterView(final FimCharacter character) {
            final View v = this.helper.getCharacterManager().createCharacterView(character);
            view.addView(v, view.getChildCount() - (editable ? 1 : 0));
            if (editable) {
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View c) {
                        removeCharacter(character, v);
                    }
                });
            }
            return v;
        }

        public View addCharacter(FimCharacter character) {
            if (characters.add(character)) {
                return addCharacterView(character);
            } else { return null; }
        }

        public void removeCharacter(FimCharacter character, View view) {
            if (characters.remove(character)) { this.view.removeView(view); }
        }
    }
}
