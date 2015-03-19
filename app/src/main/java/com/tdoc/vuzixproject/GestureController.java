package com.tdoc.vuzixproject;

import android.content.Context;
import android.util.Log;

import com.vuzix.hardware.GestureSensor;

/**
 * Created by ZoxDK on 15-03-2015.
 */
public class GestureController extends GestureSensor {
    String TAG = "Gesture";
    public GestureController(Context context){
        super(context);
    }

    @Override protected void onBackSwipe(int speed) { Log.i(TAG, "Left"); }
    @Override protected void onForwardSwipe(int speed) { Log.i(TAG, "Right"); }
    @Override protected void onFar() { Log.i(TAG, "Far"); }
    @Override protected void onNear() { Log.i(TAG, "Near"); }
    @Override protected void onDown(int speed) { Log.i(TAG, "Down"); }
    @Override protected void onUp(int speed) { Log.i(TAG, "Up"); }

}
