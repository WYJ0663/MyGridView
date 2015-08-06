package com.example.MyGridView;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class MyActivity extends Activity {
    /**
     * Called when the activity is first created.
     */
    private DragGridView dragGridView;
    private GridAdapter gridAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        dragGridView = (DragGridView) findViewById(R.id.grid_view);

        List<String> nameList = new ArrayList<String>();

        for (int i = 0; i < 20; i++) {
            nameList.add("name:" + i);
        }
        gridAdapter = new GridAdapter(this, nameList);

        dragGridView.setAdapter(gridAdapter);
        dragGridView.setNumColumns(4);


    }
}
