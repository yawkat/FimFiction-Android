package at.yawk.fimfiction.android;

import android.app.Activity;
import android.app.AlertDialog;
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

    public abstract void assignMessage(AlertDialog.Builder builder);

    public TextView textView(Helper helper, int layout) {
        TextView view = (TextView) helper.layoutInflater().inflate(layout, null);
        assign(view);
        return view;
    }

    public TextView textView(Helper helper, int layout, int color) {
        TextView view = textView(helper, layout);
        view.setTextColor(0xFF000000 | color);
        return view;
    }

    public static TranslatableText string(final CharSequence text) {
        return new TranslatableText() {
            @Override
            public String toString(Helper helper) { return text.toString(); }

            @Override
            public void assign(TextView view) { view.setText(text); }

            @Override
            public void assignTitle(Activity activity) { activity.setTitle(text); }

            @Override
            public void assignMessage(AlertDialog.Builder builder) { builder.setMessage(text); }
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

            @Override
            public void assignMessage(AlertDialog.Builder builder) { builder.setMessage(id); }
        };
    }
}
