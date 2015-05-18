package com.tdoc.vuzixproject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TDOCHandsfreeAutostartReceiver extends BroadcastReceiver {
    public TDOCHandsfreeAutostartReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent autoService = new Intent(context, TDOCHandsfreeAutostartService.class);
        context.startService(autoService);
        Log.i("AutoReceiver", "onReceive");
    }
}
