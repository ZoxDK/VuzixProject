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
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.vuzix.speech.Constants;

import java.util.List;


public class MainActivity extends Activity implements View.OnClickListener{
    private GestureController gestSensor;
    private VoiceController voiceCtrl;
    private Button buttonTest, buttonTest2;
    private TextView tvData;
    private int clickedTimes = 0;
    public static boolean isThereVoice = false, scannerIntentRunning = false;
    String model = "";
    String[] wordList = {"back", "next"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for voice recognition ability of device, create object if there.
        // Add another grammar (for "scan next"). Best would be homemade grammar/.lcf file with wordlist.
        model = Build.MODEL;

        if (checkVoiceRecognition() && model.equals("M100")) {
            voiceCtrl = new VoiceController(this, MainActivity.this);
            voiceCtrl.addGrammar(Constants.GRAMMAR_MEDIA);
            voiceCtrl.setWordlist(wordList);
        }
        //gestSensor = new GestureController(this);

        buttonTest = (Button) findViewById(R.id.buttonTest);
        buttonTest2 = (Button) findViewById(R.id.buttonTest2);
        buttonTest.setOnClickListener(this);
        buttonTest2.setOnClickListener(this);

        tvData = (TextView) findViewById(R.id.tvData);

        // Parse.com test data push
        //ParseObject testObject = new ParseObject("OnlineData");
        //testObject.put("barcode", "5060335631558");
        //testObject.put("data1", "This is a test string.");
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

    @Override // Currently just for testing physical buttons and gestures on the M100
    public void onClick(View v) {
        if (v == buttonTest) {
            //Intent intent = new Intent(this, ButtonTestActivity.class);
            //startActivity(intent);

            // For testing scanner without voice controller useable (not using M100)
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        } if (v == buttonTest2) {
            clickedTimes++;
            buttonTest2.setText("Button 2 clicked "+ clickedTimes + " times");
        }
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", ""+scanResult.getContents());

                // Query Parse.com, as testing in regards to sending and receiving data, for data to the result
                ParseQuery<ParseObject> query = ParseQuery.getQuery("OnlineData");
                query.whereEqualTo("barcode", scanResult.getContents());
                query.getFirstInBackground(new GetCallback<ParseObject>() {
                    // done is run when background query task returns a result, hopefully with a result object
                    public void done(ParseObject object, ParseException e) {
                        if (e == null) {
                            Log.d("data retrieved: ", object.getString("data1") + " and " + object.getInt("data2"));
                            tvData.setText("String data received: " + object.getString("data1") + "\n");
                            tvData.append("Integer data received: " + object.getInt("data2"));
                        } else {
                            Log.d("ParseException", "Error: " + e.getMessage() + " - code: " + e.getCode());
                            // Let the user know if the object just couldn't be found, or if it's an actual error
                            if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                tvData.setText("Barcode not found in system.\n" +
                                        "Scanned data: " + scanResult.getContents() + ".\n" +
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
