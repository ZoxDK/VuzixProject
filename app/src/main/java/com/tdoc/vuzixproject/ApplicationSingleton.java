package com.tdoc.vuzixproject;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.widget.Toast;

import com.vuzix.speech.Constants;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by KET on 22-04-2015.
 */
public class ApplicationSingleton extends Application {
    private static ApplicationSingleton ourInstance = null;
    public static SharedPreferences sharedPreferences;
    public static boolean scannerIntentRunning = false;
    private static VoiceController voiceCtrl;
    public String model = "";
    private String[] wordList = {"back", "bar code", "perpetual inventory system", "menu"};
    public static boolean isThereVoice = false;
    public static boolean voiceOff = false;
    private static boolean isTDOCConnected = false;
    private static Queue<String> scanQueue = new LinkedList();

    @Override
    public void onCreate(){
        super.onCreate();
        ourInstance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ourInstance);
        // Check for voice recognition ability of device, create object if there.
        // Add another grammar (for "scan next"). Best would be homemade grammar/.lcf file with wordlist.
        model = Build.MODEL;

        if (checkVoiceRecognition() && model.equals("M100")) {
            voiceCtrl = new VoiceController(getBaseContext());
            voiceCtrl.addGrammar(Constants.GRAMMAR_WAREHOUSE);
            //voiceCtrl.setWordlist(wordList);
        }
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

    public static ApplicationSingleton getInstance() {
        if (ourInstance == null) ourInstance = new ApplicationSingleton();
        return ourInstance;
    }

    public static boolean isTDOCConnected() {
        return isTDOCConnected;
    }

    public static void setIsTDOCConnected(boolean isTDOCConnected) {
        ApplicationSingleton.isTDOCConnected = isTDOCConnected;
    }

    public static VoiceController getVoiceCtrl(){return voiceCtrl;}

    public static Queue getScanQueue() {
        return scanQueue;
    }

}
