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

    // Constructor to use if one has their own .lcf file with grammars
    public VoiceController(Context context, String[] grammars) {
        super(context, grammars);
    }

    // Constructor to use if one has their own .lcf file and only wants to use words from wordlist.
    public VoiceController(Context context, String[] grammars, String[] wordlist) {
        super(context, grammars, wordlist);
    }

    // If word is recognized in grammars and is on wordlist, if any, onRecognition is called.
    @Override
    protected void onRecognition(String result) {
        Log.i("VoiceRecognition", result);

        // Was trying to use "scan next", but cannot be combined with current grammars, so listen for "next"
        // then start scanner intent.
        if (result.equals("next")) {
            Log.i("VoiceRecognition", "Next gotten");
            MainActivity.scannerIntentRunning = true;
            IntentIntegrator integrator = new IntentIntegrator(callingActivity);
            integrator.initiateScan();
        } else if (result.equals("back") && MainActivity.scannerIntentRunning){
            Log.i("VoiceRecognition", "Back gotten");
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        }
    }

}
