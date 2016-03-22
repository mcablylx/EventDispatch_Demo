package com.demo.mckiera.eventdispatch;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * author by Mckiera
 * time: 2016/3/21  13:27
 * description:
 * updateTime:
 * update description:
 */
public class ActivityA extends Activity {
    private static final String TAG = "FUCK";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button btn = new Button(this);
        ImageView img = new ImageView(this);
        setContentView(img);
        //onClick_onTouch(btn);
//        onTouch1(btn);
        onDown(img);
    }

    private void onDown(View btn){
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        Log.i(TAG, "ImageView onTouch event.getAction() : " + event.getAction() );
                        return false;
                    case MotionEvent.ACTION_UP:
                        Log.i(TAG, "ImageView onTouch event.getAction() : " + event.getAction() );
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        Log.i(TAG, "ImageView onTouch event.getAction() : " + event.getAction() );
                        return false;
                }
                return false;
            }
        });
    }

    private void onTouch1(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button onClick");
            }
        });
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "Button onTouch event.getAction() : " + event.getAction() );
                return true;
            }
        });
    }

    private void onClick_onTouch(Button btn) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "Button onClick");
            }
        });
        btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i(TAG, "Button onTouch event.getAction() : " + event.getAction() );
                return false;
            }
        });
    }
}
