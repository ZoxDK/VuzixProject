package com.tdoc.vuzixproject;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


public class SetupFragment extends Fragment implements View.OnClickListener{

    private Button startScanButton, menuButton;
    private TextView tvData;
    private View rootView;
    private String currState = "NONE", serverIP = "", serverPort = "", returnData = "";
    private int port;
    private String[] wordList = {"back", "menu", "scan", "bar code", "perpetual inventory system"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_single_scan, container, false);

        startScanButton = (Button) rootView.findViewById(R.id.startScanButton);
        menuButton = (Button) rootView.findViewById(R.id.menuButton);
        startScanButton.setOnClickListener(this);
        menuButton.setOnClickListener(this);

        tvData = (TextView) rootView.findViewById(R.id.tvData);

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override // Currently just for testing physical buttons and gestures on the M100, and for use on phones
    public void onClick(View v) {
        if (v == startScanButton) {
            Log.i("Button pressed: ", "startScanButton");

            MainActivity.scannerIntentRunning = true;
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();

        } else if (v == menuButton) {
            Log.i("Button pressed: ", "menuButton");
            Fragment fragment = new MainFragment();
            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragmentcontainer, fragment, "FRAG_MAIN")
                    .addToBackStack(null)
                    .commit();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                String result = scanResult.getContents();
                Log.i("Scan result", "" + result);

                // Set server IP
                if (result.equals("StartSetServerIP")){
                    currState = "SET_SERVER_IP";
                    serverIP = "";
                } else if (currState.equals("SET_SERVER_IP")){
                    serverIP = serverIP.concat(result);
                } else if (result.equals("EndSetServerIP")){
                    ApplicationSingleton.sharedPreferences.edit().putString("SERVER_IP", serverIP).commit();
                    currState = "NONE";
                } // Server IP set

                // Set server port
                else if (result.equals("StartSetServerPort")){
                    currState = "SET_SERVER_PORT";
                    serverPort = "";
                } else if (currState.equals("SET_SERVER_IP")){
                    serverPort = serverPort.concat(result);
                } else if (result.equals("EndSetServerPort")){
                    port = -1;
                    try {
                        port = Integer.parseInt(serverPort);
                    } catch(NumberFormatException nfe) {
                        System.out.println("Could not parse " + nfe);
                    }
                    ApplicationSingleton.sharedPreferences.edit().putInt("SERVER_PORT", port).commit();
                    currState = "NONE";
                } // Server port set

                // Setup finished
                else if (result.equals("FinishSetup")){

                    if (ApplicationSingleton.sharedPreferences.getString("SERVER_IP", "").equals("")){
                        serverIP = "Server IP not set correctly.";
                    } else if (ApplicationSingleton.sharedPreferences.getInt("SERVER_PORT", -1) == -1){
                        serverPort = "Server port not set correctly.";
                    }
                    returnData = "Server IP: " + serverIP + "\n" +
                            "Server port: " + serverPort + "\n";
                    currState = "DONE";
                }
                tvData.setText(returnData);
                if (!currState.equals("DONE")) {
                    MainActivity.scannerIntentRunning = true;
                    IntentIntegrator integrator = new IntentIntegrator(this);
                    integrator.initiateScan();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        currState = "NONE";
    }
}
