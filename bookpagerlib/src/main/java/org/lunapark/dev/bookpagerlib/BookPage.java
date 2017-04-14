package org.lunapark.dev.bookpagerlib;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

/**
 * Book pages
 * Created by znak on 13.04.2017.
 */

public class BookPage {

    private Bitmap bitmapOdd, bitmapEven;
    private int width, height;
    private List<String> book;
    private int fontSize;


    BookPage(List<String> book, int fontSize) {
        this.book = book;
        this.fontSize = fontSize;
    }

    void setViewSize(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    private void init() {
        if (fontSize == 0) fontSize = height / 32;
        createBitmaps();
    }

    private void createBitmaps() {
        bitmapOdd = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmapEven = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    Bitmap getPage(int pageNum) {
        Bitmap bitmap = bitmapEven;
        if ((pageNum & 1) == 0) {
            bitmap = bitmapOdd;
        }

        bitmap.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.LEFT);
        addText(pageNum, canvas, paint);
        return bitmap;
    }

    private void addText(int index, Canvas canvas, Paint paint) {
        String s = book.get(index);
        String[] strings = s.split("\n");
        for (int i = 0; i < strings.length; i++) {
            String row = strings[i];
            canvas.drawText(row, 0, i * fontSize + fontSize, paint);
        }

    }

    int size() {
        return book.size();
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }
}
