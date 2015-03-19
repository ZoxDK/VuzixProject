package com.tdoc.vuzixproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.vuzix.speech.Constants;

import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{
    private GestureController gestSensor;
    private VoiceController voiceCtrl;
    private Button buttonTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkVoiceRecognition()) {
            voiceCtrl = new VoiceController(this, MainActivity.this);
            voiceCtrl.addGrammar(Constants.GRAMMAR_MEDIA);
        }
        gestSensor = new GestureController(this);

        buttonTest = (Button) findViewById(R.id.buttonTest);
        buttonTest.setOnClickListener(this);
    }

    public boolean checkVoiceRecognition() {
        // Check if voice recognition is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
            if (activities.size() == 0) {
            Toast.makeText(this, "Voice recognizer not present",
            Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    @Override
    public void onClick(View v) {
        if (v == buttonTest) {
            Intent intent = new Intent(this, ButtonTestActivity.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            Log.i("Scan result", scanResult.getContents());
        } else return;
    }

    @Override
    protected void onResume(){
        super.onResume();
        voiceCtrl.on();
        gestSensor.register();
    }

    @Override
    protected void onPause(){
        super.onPause();
        voiceCtrl.off();
        gestSensor.unregister();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (voiceCtrl != null) voiceCtrl.destroy();
        if (gestSensor != null) gestSensor = null;
    }
}
