package org.lunapark.dev.bookpager;

import android.app.Activity;
import android.os.Bundle;

import org.lunapark.dev.bookpagerlib.PageCurlView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create sample book
        List<String> book = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String page = "Page:\n" + i + "\n\nContent:\n" + "blabla";
            book.add(page);
        }
        // Define page curl view and set content
        PageCurlView pageCurlView = (PageCurlView) findViewById(R.id.pager);
        pageCurlView.setBook(book);

        // Optional
        pageCurlView.setCurlMode(PageCurlView.CURLMODE_SIMPLE);
        pageCurlView.setCurlSpeed(30);
        pageCurlView.setShowPageNumber(true);

    }
}
