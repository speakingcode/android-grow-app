package org.tndata.android.grow.util;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.widget.ImageView;

import org.tndata.android.grow.R;

public class ImageHelper {
    public final static int SELECTED = 1;
    public final static int ADD = 2;

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and
            // keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static void setupImageViewButton(final Resources resources, ImageView imageView,
                                            int style) {
        GradientDrawable buttonDrawable = (GradientDrawable) imageView.getBackground();
        int color = -1;
        switch (style) {
            case SELECTED:
                color = resources.getColor(R.color.grow_primary_dark);
                imageView.setImageResource(R.drawable.ic_selected_white);
                break;
            case ADD:
                color = resources.getColor(R.color.grow_secondary);
                imageView.setImageResource(R.drawable.ic_action_new_large);
                break;
        }
        if (color != -1) {
            buttonDrawable.setColor(color);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            imageView.setBackground(buttonDrawable);
        } else {
            imageView.setBackgroundDrawable(buttonDrawable);
        }
    }

}
