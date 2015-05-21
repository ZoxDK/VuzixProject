package com.tdoc.vuzixproject;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.parse.Parse;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by KET on 22-04-2015.
 */
public class ApplicationSingleton extends Application {
    private static ApplicationSingleton ourInstance = null;
    public static SharedPreferences sharedPreferences;
    public static boolean voiceOff = false;
    private static boolean isTDOCConnected = false;
    private static Queue<String> scanQueue = new LinkedList();

    @Override
    public void onCreate(){
        super.onCreate();
        ourInstance = this;

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ourInstance);
        // Enable Local Datastore. Currently not used and creates issues due to running before .initialize for some reason.
        //Parse.enableLocalDatastore(this);

        // Initialize Parse.com access, probably with user and project hashes.
        Parse.initialize(this, "sbnDtByNJrzgQXik8HRac2HyUVhqkigKUOcbQ52g", "oTAXhgq4M8qHcvAfAxJKRQ07DyP2zJz1phdeut8r");

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


    public static Queue getScanQueue() {
        return scanQueue;
    }

}
