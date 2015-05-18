package com.tdoc.vuzixproject;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.vuzix.speech.VoiceControl;

/**
 * Created by ZoxDK on 15-03-2015.
 */
public class VoiceController extends VoiceControl {
    private Context context;
    private Activity callingActivity;
    private Fragment callingFragment;

    public void setCallingFragment(Fragment callingFragment) {
        this.callingFragment = callingFragment;
    }

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

        // Check if supposed to listen for voice
        // ! Currently, the M100 turns off the mic and only listens for Voice On, if Voice Off is given
        // Hence, this check and setup is redundant !
        if (!ApplicationSingleton.voiceOff) {
            if (result.equals("bar code")) {
                Log.i("VoiceRecognition", "Bar code gotten");

                MainActivity.scannerIntentRunning = true;
                IntentIntegrator integrator = new IntentIntegrator(callingFragment);
                integrator.initiateScan();

            //} else if (result.equals("back") && MainActivity.scannerIntentRunning) {
            } else if (result.equals("back")) {
                Log.i("VoiceRecognition", "Back gotten");

                Intent intent = new Intent(context, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);

            } else if (result.equals("scan")) {
                Log.i("VoiceRecognition", "Scan gotten");

                MainActivity.scannerIntentRunning = true;
                IntentIntegrator integrator = new IntentIntegrator(callingFragment);
                integrator.initiateScan();

            } else if (result.equals("menu")){
                Log.i("VoiceRecognition", "Menu gotten");

                Fragment fragment = new MainFragment();
                callingFragment.getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragmentcontainer, fragment, "FRAG_MAIN")
                        .addToBackStack(null)
                        .commit();

            } else if (result.equals("packing list")) {
                Log.i("VoiceRecognition", "Packing list gotten");

                Fragment fragment = new PackingListFragment();
                callingFragment.getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragmentcontainer, fragment, "FRAG_PACK")
                        .addToBackStack(null)
                        .commit();

            } else if (result.equals("voice off")){
                Log.i("VoiceRecognition", "Voice off gotten");

                ApplicationSingleton.voiceOff = true;

            } else if (result.equals("perpetual inventory system")){
                android.os.Process.killProcess(android.os.Process.myPid());
                MainActivity.voiceCtrl.destroy();
            }

        } else if (result.equals("voice on")){
            Log.i("VoiceRecognition", "Voice on gotten");

            ApplicationSingleton.voiceOff = false;
        }
    }

}
