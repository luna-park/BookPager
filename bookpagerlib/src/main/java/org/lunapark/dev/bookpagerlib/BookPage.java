package org.lunapark.dev.bookpagerlib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Book page
 * Created by znak on 13.04.2017.
 */

public class BookPage {

    private Bitmap bitmapForeground, bitmapBackground;
    private int width, height;
    private int fontSize;
    private boolean fontAdaptiveWidth;

    void setViewSize(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        if (fontSize == 0) {
            fontAdaptiveWidth = true;
        }
        createBitmaps();
    }

    private void createBitmaps() {
        bitmapForeground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapBackground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    Bitmap getPage(String pageContent, boolean isForegroudPage) {
        Bitmap bitmap;
        if (isForegroudPage) {
            bitmap = bitmapForeground;
        } else {
            bitmap = bitmapBackground;
        }

        bitmap.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);

        paint.setTextAlign(Paint.Align.LEFT);
        addText(pageContent, canvas, paint);
        return bitmap;
    }

    private void addText(String pageContent, Canvas canvas, Paint paint) {
        String s = pageContent;
        String[] strings = s.split("\n");
        int rows = strings.length;

        int maxSize = 0;
        for (String string : strings) {
            int rowSize = string.length();
            if (rowSize > maxSize) maxSize = rowSize;
        }

        // FIXME Magic numbers
        if (fontAdaptiveWidth) {
            if (rows > maxSize - 7) {
                fontSize = Math.round(height / rows * 0.8f);
            } else {
                fontSize = Math.round(width / maxSize * 1.6f);
            }
        }

        int deltaY = (height - rows * fontSize) / 2;
        int margin = fontSize / 2;

        for (int i = 0; i < rows; i++) {
            String row = strings[i];
            paint.setTextSize(fontSize);
            canvas.drawText(row, margin, i * fontSize + deltaY, paint);
        }

    }
}
