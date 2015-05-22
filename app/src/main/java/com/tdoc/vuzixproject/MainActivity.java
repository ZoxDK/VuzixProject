package com.tdoc.vuzixproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.vuzix.speech.Constants;

import java.util.List;


public class MainActivity extends Activity {
    public static VoiceController voiceCtrl;
    public static boolean isThereVoice = false, scannerIntentRunning = false;
    private String model = "";
    private String[] wordList = {"back", "bar code", "perpetual inventory system"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("onCreate", "onCreate run");
        setContentView(R.layout.activity_main);

        // Check for voice recognition ability of device, create object if there.
        // Add another grammar (for "scan next"). Best would be homemade grammar/.lcf file with wordlist.
        model = Build.MODEL;

        if (checkVoiceRecognition() && model.equals("M100")) {
            voiceCtrl = new VoiceController(this, MainActivity.this);
            voiceCtrl.addGrammar(Constants.GRAMMAR_WAREHOUSE);
            voiceCtrl.addGrammar(Constants.GRAMMAR_MEDIA);
            voiceCtrl.setWordlist(wordList);
        }
        //gestSensor = new GestureController(this);


        // Add first fragment
        if (savedInstanceState == null) {
            LoginFragment fragment = new LoginFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentcontainer, fragment, "FRAG_LOGIN")
                    .commit();
            if (isThereVoice) voiceCtrl.setCallingFragment(fragment);
        }

        // Parse.com test data push
        //ParseObject testObject = new ParseObject("LoginCreds");
        //testObject.put("name", "Ketil Kirchhof");
        //testObject.put("data2", 002);
        //testObject.saveInBackground();

        // Keep screen on (requires WAKE_LOCK permission in manifest)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Check if voice recognition is present
    public boolean checkVoiceRecognition() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() == 0 || !model.equals("M100")) {
                Toast.makeText(this, "Voice recognizer not present",
                Toast.LENGTH_SHORT).show();
            return false;
            }
        isThereVoice = true;
        return true;
    }

    @Override // Currently not used, might never be, due to restricted controls
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override // Currently not used, might never be, due to restricted controls
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /***********************
     * Ensure that activity doesn't keep listening when not in focus, and clean up once destroyed.
     **********************/
    @Override
    protected void onResume(){
        super.onResume();
        if (isThereVoice) voiceCtrl.on();
        //gestSensor.register();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (isThereVoice && !scannerIntentRunning) voiceCtrl.off();
        //gestSensor.unregister();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (voiceCtrl != null) voiceCtrl.destroy();
        //if (gestSensor != null) gestSensor = null;
    }
}
