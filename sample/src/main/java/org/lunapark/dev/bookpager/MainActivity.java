package org.lunapark.dev.bookpager;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.lunapark.dev.bookpagerlib.ChangePageListener;
import org.lunapark.dev.bookpagerlib.PageCurlView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity implements ChangePageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create sample book
        List<String> book = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String page = "Page: " + i + " Content:\n" + "blabla";
            book.add(page);
        }
        // Define page curl view and set content
        PageCurlView pageCurlView = (PageCurlView) findViewById(R.id.pager);
//        pageCurlView.setBook(book);
        pageCurlView.setBook(book, 200);
        pageCurlView.setOnChangePageListener(this);

        // Optional
        pageCurlView.setCurlMode(PageCurlView.CURLMODE_SIMPLE);
        pageCurlView.setCurlSpeed(30);
        pageCurlView.setShowPageNumber(true);
        pageCurlView.setEnableDebugMode(true);

    }

    @Override
    public void onPageChange(int pageNum) {
        Log.e("Book Pager", "Page: " + pageNum);
    }
}
