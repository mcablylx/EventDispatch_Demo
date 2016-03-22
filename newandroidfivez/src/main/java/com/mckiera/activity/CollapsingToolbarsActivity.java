package com.mckiera.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.util.Log;

import com.mckiera.newandroid_fivez.R;

/**
 * author by Mckiera
 * time: 2016/3/22  09:50
 * description:
 * updateTime:
 * update description:
 */
public class CollapsingToolbarsActivity extends Activity implements AppBarLayout.OnOffsetChangedListener {
    private static String TAG = "FUCK";
    private AppBarLayout abl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.collapsingtoolbar_activity);
        abl = (AppBarLayout) findViewById(R.id.abl);
        abl.addOnOffsetChangedListener(this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        Log.i(TAG, "verticalOffset 偏移量 = "+ verticalOffset);
    }
}
