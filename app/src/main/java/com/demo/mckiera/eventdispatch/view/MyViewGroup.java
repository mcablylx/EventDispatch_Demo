package com.demo.mckiera.eventdispatch.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

/**
 * author by Mckiera
 * time: 2016/3/22  16:21
 * description:
 * updateTime:
 * update description:
 */
public class MyViewGroup extends LinearLayout {

    public MyViewGroup(Context context) {
        super(context);
    }

    public MyViewGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }
}
