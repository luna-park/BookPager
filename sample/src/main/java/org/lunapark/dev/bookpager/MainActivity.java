package org.lunapark.dev.bookpager;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.lunapark.dev.bookpagerlib.ChangePageListener;
import org.lunapark.dev.bookpagerlib.PageCurlView;

import java.util.List;

public class MainActivity extends Activity implements ChangePageListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create sample book
//        List<String> book = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            String page = "Page: " + i + " Content:\n" + "blablabqwertyuiop";
//            book.add(page);
//        }

        GetBook getBook = new GetBook();
        List<String> book = getBook.get(this);

        // Define page curl view and set content
        final PageCurlView pageCurlView = (PageCurlView) findViewById(R.id.pager);
        pageCurlView.setBook(book, 59);
        pageCurlView.setOnChangePageListener(this);

        // Optional
        pageCurlView.setCurlMode(PageCurlView.CURLMODE_SIMPLE);
        pageCurlView.setCurlSpeed(30);
        pageCurlView.setShowPageNumber(true);

        Button button = (Button) findViewById(R.id.btnGo);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageCurlView.goToPage(500);
            }
        });

    }

    @Override
    public void onPageChange(int pageNum) {
//        Log.e("Book Pager", "Page: " + pageNum);
    }
}
