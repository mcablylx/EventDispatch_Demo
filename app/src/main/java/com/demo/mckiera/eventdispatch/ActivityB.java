package com.demo.mckiera.eventdispatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.demo.mckiera.eventdispatch.view.MyViewGroup;

/**
 * author by Mckiera
 * time: 2016/3/22  16:21
 * description:
 * updateTime:
 * update description:
 */
public class ActivityB extends Activity {
    private static final String TAG = "FUCK";
    private MyViewGroup my;
    private Button btn1;
    private Button btn2;
    private Button btn3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_viewgroup);
        init();
        setLisenser();
    }

    private void setLisenser() {
        my.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "MyViewGroup onTouch event.getAction()="+event.getAction());
                return false;
            }
        });

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button1 onClick");
            }
        });
        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button2 onClick");
            }
        });
        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button2 onClick");
            }
        });
    }


    private void init() {
        my = (MyViewGroup) findViewById(R.id.my);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);
        btn3 = (Button) findViewById(R.id.btn3);
    }
}
