package at.yawk.fimfiction.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class DynamicImageView extends ImageView {
    public DynamicImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Drawable d = this.getDrawable();

        if (d != null) {
            float prop = (float) d.getIntrinsicHeight() / d.getIntrinsicWidth();
            // scale down if height > width
            float wscale = Math.min(1, 1 / prop);
            // ceil not round - avoid thin vertical gaps along the left/right edges
            int width = (int) Math.ceil(MeasureSpec.getSize(widthMeasureSpec));
            int height = (int) Math.ceil(width * prop * wscale);
            this.setMeasuredDimension(width, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }
}
