package org.lunapark.dev.bookpager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import app.num.numandroidpagecurleffect.PageCurlView;


public class MainActivity extends Activity {

    private PageCurlView pageCurlView;
    private List<String> book;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pageCurlView = (PageCurlView) findViewById(R.id.pager);
        pageCurlView.SetCurlMode(PageCurlView.CURLMODE_SIMPLE);
        pageCurlView.setCurlSpeed(10);

        book = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String page = "Page:\n" + i + "\n\nContent:\n" + "blabla";
            book.add(page);
        }

        List<Bitmap> bitmaps = new ArrayList<>();

        BookPage bookPage = new BookPage(this, book);

        for (int i = 0; i < 3; i++) {
            Bitmap bitmap = bookPage.getPage(i);
            bitmaps.add(bitmap);
        }

        pageCurlView.setCurlViewBitmap(bitmaps);

//        Intent intent = new Intent(this, AndroidPageCurlActivity.class);
//        startActivity(intent);
    }
}
