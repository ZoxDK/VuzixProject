package com.tdoc.vuzixproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vuzix.speech.VoiceControl;

/**
 * Created by ZoxDK on 15-03-2015.
 */
public class VoiceController extends VoiceControl {
    Context context;
    Activity callingActivity;

    public VoiceController(Context context){
        super(context);
        this.context = context;

    }
    public VoiceController(Context context, Activity activity){
        super(context);
        this.context = context;
        this.callingActivity = activity;

    }

    //public VoiceController(Context context, String[] grammars) {
    //    super(context, grammars);
    //}

    public VoiceController(Context context, String[] grammars, String[] wordlist) {
        super(context, grammars, wordlist);
    }

    @Override
    protected void onRecognition(String result) {
        Log.i("VoiceRecognition", result);
        if (result.equals("next")) {
            Log.i("VoiceRecognition", "Next gotten");
            IntentIntegrator integrator = new IntentIntegrator(callingActivity);
            integrator.initiateScan();
        }
    }

}
