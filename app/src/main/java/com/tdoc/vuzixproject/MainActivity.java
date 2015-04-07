package com.tdoc.vuzixproject;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.vuzix.speech.Constants;

import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{
    private GestureController gestSensor;
    private VoiceController voiceCtrl;
    private Button buttonTest;
    private TextView tvData;

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

        tvData = (TextView) findViewById(R.id.tvData);

        // Enable Local Datastore.
        //Parse.enableLocalDatastore(this);

        Parse.initialize(this, "sbnDtByNJrzgQXik8HRac2HyUVhqkigKUOcbQ52g", "oTAXhgq4M8qHcvAfAxJKRQ07DyP2zJz1phdeut8r");

        // Parse.com test
        //ParseObject testObject = new ParseObject("OnlineData");
        //testObject.put("barcode", "KR05WP5W7176929HA11AA00");
        //testObject.put("data1", "This is another test string.");
        //testObject.put("data2", 002);
        //testObject.saveInBackground();
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
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", scanResult.getContents());

                ParseQuery<ParseObject> query = ParseQuery.getQuery("OnlineData");
                query.whereEqualTo("barcode", scanResult.getContents());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            Log.d("data retrieved: ", object.getString("data1") + " and " + object.getInt("data2"));
                            tvData.setText("String data received: " + object.getString("data1") + "\n");
                            tvData.append("Integer data received: " + object.getInt("data2"));
                        } else {
                            Log.d("ParseException", "Error: " + e.getMessage() + " - code: " + e.getCode());

                            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                tvData.setText("Barcode not found in system.\n" +
                                        "Please try again...");
                            } else {
                                tvData.setText("And error occurred. Please try again...");
                            }
                        }
                    }
                });
            }
        }
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
