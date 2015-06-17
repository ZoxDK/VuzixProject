package com.tdoc.vuzixproject;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("onCreate", "onCreate run");
        setContentView(R.layout.activity_main);

        // Add first fragment
        if (savedInstanceState == null) {
            Fragment fragment;

            // Always run setup first, to be able to access server
            if (ApplicationSingleton.sharedPreferences.getString("SERVER_IP", "").equals("") ||
                    ApplicationSingleton.sharedPreferences.getInt("SERVER_PORT", -1) == -1){
                fragment = new SetupFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.fragmentcontainer, fragment, "FRAG_SETUP")
                        .commit();
            }
            // Only do login screen if it has been more than 24 hours (in milliseconds)
            // or if there's been no prior login (default -1).
            // Using currTimeMil gives time since 1/1/1970, meaning we don't have to account for daylight savings.
            else if ((System.currentTimeMillis() - ApplicationSingleton.sharedPreferences.getLong("loginTime", -1)) > 86400000) {
                fragment = new LoginFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.fragmentcontainer, fragment, "FRAG_LOGIN")
                        .commit();
            } else {
                fragment = new MainFragment();
                getFragmentManager().beginTransaction()
                        .add(R.id.fragmentcontainer, fragment, "FRAG_MAIN")
                        .addToBackStack(null)
                        .commit();
            }
        }

        // Keep screen on (requires WAKE_LOCK permission in manifest)
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
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
            Fragment fragment = new SetupFragment();
            getFragmentManager().beginTransaction()
                    .add(R.id.fragmentcontainer, fragment, "FRAG_SETUP")
                    .commit();
        }

        return super.onOptionsItemSelected(item);
    }


    /***********************
     * Ensure that activity doesn't keep listening when not in focus, and clean up once destroyed.
     **********************/
    @Override
    protected void onResume(){
        super.onResume();
        if (ApplicationSingleton.isThereVoice) ApplicationSingleton.getVoiceCtrl().on();
        //gestSensor.register();
    }

    @Override
    protected void onPause(){
        super.onPause();
        if (ApplicationSingleton.isThereVoice && !ApplicationSingleton.scannerIntentRunning) ApplicationSingleton.getVoiceCtrl().off();
        //gestSensor.unregister();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (ApplicationSingleton.getVoiceCtrl() != null) ApplicationSingleton.getVoiceCtrl().destroy();
        //if (gestSensor != null) gestSensor = null;
    }
}
