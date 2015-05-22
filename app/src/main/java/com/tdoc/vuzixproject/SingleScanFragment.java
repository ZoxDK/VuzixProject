package com.tdoc.vuzixproject;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.LinkedList;
import java.util.Queue;

public class SingleScanFragment extends Fragment implements View.OnClickListener{

    private Button startScanButton, menuButton;
    private TextView tvData;
    private View rootView;
    private ExternalCommunication extComm;
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

    @Override
    public void onStart() {
        super.onStart();
        if (MainActivity.isThereVoice){
            MainActivity.voiceCtrl.setCallingFragment(this);
            MainActivity.voiceCtrl.setWordlist(wordList);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", "" + scanResult.getContents());
                Queue scanQueue = ApplicationSingleton.getScanQueue();

                // Check if there was backlogged scans saved - if so, ask to send
                if (!ApplicationSingleton.sharedPreferences.getString("scanQueue", "").equals("")){
                    // TODO: Add popup asking for "complete" or "ignore" backlog
                    Gson gson = new Gson();
                    scanQueue = gson.fromJson(ApplicationSingleton.sharedPreferences.getString("scanQueue", ""), LinkedList.class);
                }

                // Add scan to queue for no WiFi situations
                if (!scanQueue.offer(scanResult.getContents())){
                    Toast.makeText(this.getActivity(), "Error adding scan result to result queue...", Toast.LENGTH_SHORT)
                    .show();
                } else {
                    // Ensure WiFi is connected
                    ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    if (wifiInfo.isConnected()) {

                        // Only do Parse.com queries if we are not connected to T-DOC;
                        // This is for testing purposes only
                        if (!ApplicationSingleton.isTDOCConnected()) {
                            // Sends messages in the queue to Parse.com
                            while (!scanQueue.isEmpty())
                                parseCommunication(scanQueue.poll().toString());

                        } else {
                            // T-DOC communications - try to connect to T-DOC server on WiFi
                            new connectTask().execute("");
                            //sends the messages in the queue to the server
                            if (extComm != null) {
                                while (!scanQueue.isEmpty())
                                    extComm.sendMessage(scanQueue.poll().toString());
                            }
                        }

                    } else {
                        Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "No network connection. Saving following to queue: " + scanQueue.peek(), Toast.LENGTH_LONG)
                                .show();
                    }
                    // Save what is in the queue - if there was WiFi, it should be empty
                    // Otherwise, the result will have been added to the queue with the .offer()
                    Gson gson = new Gson();
                    String json;
                    if (!scanQueue.isEmpty()) {
                        json = gson.toJson(scanQueue);
                    } else {
                        json = "";
                    }
                    Log.d("SingScan Json", "Json to be put in preferences: " + json);
                    ApplicationSingleton.sharedPreferences.edit().putString("scanQueue", json).commit();

                }
            }
        }
    }
    private void parseCommunication(final String scanResults){
        ParseQuery<ParseObject> query = ParseQuery.getQuery("OnlineData");
        query.whereEqualTo("barcode", scanResults);
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
                                "Scanned data: " + scanResults + ".\n" +
                                "Please try again...");
                    } else {
                        tvData.setText("And error occurred. Please try again...");
                    }
                }
            }
        });
    }

    public class connectTask extends AsyncTask<String, String, ExternalCommunication> {

        @Override
        protected ExternalCommunication doInBackground(String... message) {

            //we create a TCPClient object and
            extComm = new ExternalCommunication(new ExternalCommunication.OnMessageReceived() {
                @Override
                //here the messageReceived method is implemented
                public void messageReceived(String message) {
                    //this method calls the onProgressUpdate
                    publishProgress(message);
                }
            });
            extComm.run();

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);

            tvData.setText(values[0]);
        }
    }

}
