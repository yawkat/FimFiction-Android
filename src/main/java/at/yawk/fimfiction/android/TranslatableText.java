package at.yawk.fimfiction.android;

import android.app.Activity;
import android.widget.TextView;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author Jonas Konrad (yawkat)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class TranslatableText {
    public abstract String toString(Helper helper);

    public abstract void assign(TextView view);

    public abstract void assignTitle(Activity activity);

    public static TranslatableText string(final String text) {
        return new TranslatableText() {
            @Override
            public String toString(Helper helper) { return text; }

            @Override
            public void assign(TextView view) { view.setText(text); }

            @Override
            public void assignTitle(Activity activity) { activity.setTitle(text); }
        };
    }

    public static TranslatableText id(final int id) {
        return new TranslatableText() {
            @Override
            public String toString(Helper helper) { return helper.context().getResources().getString(id); }

            @Override
            public void assign(TextView view) { view.setText(id); }

            @Override
            public void assignTitle(Activity activity) { activity.setTitle(id); }
        };
    }
}
