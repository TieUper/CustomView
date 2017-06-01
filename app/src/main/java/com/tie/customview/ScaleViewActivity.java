package com.tie.customview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.lichfaker.scaleview.ScaleView;

public class ScaleViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scale_view);

        ScaleView mScaleView = (ScaleView) findViewById(R.id.view_scale);

        final TextView mTextView = (TextView) findViewById(R.id.text_view_show);

        mScaleView.setOnScrollListener(new ScaleView.OnScrollListener() {
            @Override
            public void onScaleScroll(int scale) {
                mTextView.setText(String.valueOf(scale));
            }
        });
    }
}
