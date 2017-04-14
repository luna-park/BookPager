package org.lunapark.dev.bookpagerlib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.List;

/**
 * Book pages
 * Created by znak on 13.04.2017.
 */

public class BookPage {

    private String TAG = "BookPage";
    private Bitmap bitmapForeground, bitmapBackground;
    private int width, height;
    private List<String> book;
    private int fontSize;
    private boolean fontAdaptiveWidth;


    BookPage(List<String> book) {
        this.book = book;
    }

    void setViewSize(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        if (fontSize == 0) {
            fontAdaptiveWidth = true;
//            fontSize = height / 32;
        }
        createBitmaps();
    }

    private void createBitmaps() {
        bitmapForeground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapBackground = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    Bitmap getPage(int pageNum, boolean fore) {
        Bitmap bitmap;
        if (fore) {
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
        addText(pageNum, canvas, paint);
        return bitmap;
    }

    private void addText(int index, Canvas canvas, Paint paint) {
        String s = book.get(index);
        String[] strings = s.split("\n");
        int rows = strings.length;

        int maxSize = 0;
        for (String string : strings) {
            int rowSize = string.length();
            if (rowSize > maxSize) maxSize = rowSize;
        }

        if (fontAdaptiveWidth) {

            if (rows > maxSize - 7) {
                fontSize = Math.round(height / rows * 0.8f);
            } else {
                fontSize = Math.round(width / maxSize * 1.6f);
            }

            Log.e(TAG, String.format("Page: %s / %s; Font size: %s; Rows: %s; maxSize: %s",
                    index, book.size(), fontSize, rows, maxSize));
        }

        int deltaY = (height - rows * fontSize) / 2;
        int margin = fontSize / 2;

        for (int i = 0; i < rows; i++) {
            String row = strings[i];
            paint.setTextSize(fontSize);
            canvas.drawText(row, margin, i * fontSize + deltaY, paint);
        }

    }

    int size() {
        return book.size();
    }
}
