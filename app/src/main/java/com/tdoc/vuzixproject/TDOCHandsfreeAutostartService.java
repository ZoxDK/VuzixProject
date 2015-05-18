package com.tdoc.vuzixproject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TDOCHandsfreeAutostartService extends Service {
    public TDOCHandsfreeAutostartService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("AutoService", "onDestroy");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);

        Intent autostartedIntent = new Intent(getBaseContext(), MainActivity.class);
        autostartedIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(autostartedIntent);
        Log.i("AutoService", "onStart");
    }
}
