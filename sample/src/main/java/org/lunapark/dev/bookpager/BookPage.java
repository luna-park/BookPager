package org.lunapark.dev.bookpager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.view.Display;

import java.util.List;

/**
 * Created by znak on 13.04.2017.
 */

public class BookPage {

    private Bitmap bitmap;
    private Context context;
    private int width, height;
    private List<String> book;
    private int fontSize;
//    private Typeface tanger;

    public BookPage(Activity activity, List<String> book) {
        context = activity.getApplicationContext();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        fontSize = height / 32;
        this.book = book;
        //        tanger = Typeface.createFromAsset(context.getAssets(),"fonts/UnifrakturCook-Bold.ttf");
    }


    public Bitmap getPage(int pageNum) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.eraseColor(Color.WHITE);

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
//        paint.setTypeface(tanger);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.BLACK);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.LEFT);
//        canvas.drawText(s, width / 2, height / 2, paint);
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


}
