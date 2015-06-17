package com.tdoc.vuzixproject;

import android.app.Activity;
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
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.Queue;

public class MultiScanFragment extends Fragment implements View.OnClickListener{

    private Button startScanButton, menuButton;
    private TextView tvData;
    private View rootView;
    public static ScrollView scrollView;
    public static boolean finishedCalled = false;
    private ExternalCommunication extComm;
    private String[] wordList = {"back", "menu", "finished", "bar code", "perpetual inventory system"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_multi_scan, container, false);

        scrollView = (ScrollView) rootView.findViewById(R.id.scrollView);
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

            ApplicationSingleton.scannerIntentRunning = true;
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
        if (ApplicationSingleton.isThereVoice){
            ApplicationSingleton.getVoiceCtrl().setCallingFragment(this);
            //ApplicationSingleton.getVoiceCtrl().setWordlist(wordList);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            ApplicationSingleton.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                if (resultCode == Activity.RESULT_OK) {
                    Log.i("Scan result", "" + scanResult.getContents());
                    Log.d("scanResult Formatname", scanResult.getFormatName());
                    Queue scanQueue = ApplicationSingleton.getScanQueue();

                    // Check if there was backlogged scans saved - if so, ask to send
                    if (!ApplicationSingleton.sharedPreferences.getString("scanQueue", "").equals("")) {
                        // TODO: Add popup asking for "complete" or "ignore" backlog
                        Gson gson = new Gson();
                        scanQueue = gson.fromJson(ApplicationSingleton.sharedPreferences.getString("scanQueue", ""), LinkedList.class);
                    }

                    // Add scan to queue for no WiFi situations
                    if (!scanQueue.offer(scanResult.getContents())) {
                        Toast.makeText(this.getActivity(), "Error adding scan result to result queue...", Toast.LENGTH_LONG)
                                .show();
                    } else {
                        // Ensure WiFi is connected
                        ConnectivityManager connManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
                        NetworkInfo wifiInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                        if (wifiInfo.isConnected()) {
                            // T-DOC communications - try to connect to T-DOC server on WiFi
                            new connectTask().execute("");

                            // Send the messages in the queue to the T-DOC server
                            if (extComm != null) {
                                while (!scanQueue.isEmpty())
                                    extComm.sendMessage(scanQueue.poll().toString());
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
                } else {
                    Toast.makeText(this.getActivity(), "Scan cancelled or failed. Please try again...", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
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
            if (values[0].startsWith("A")){
                Log.d("T-DOC returns:", "Ack: "+values[0]);

                // TODO: Play some sound to indicate ACK
                tvData.append(values[0].substring(1)+"\n");
                if (!finishedCalled) {
                    ApplicationSingleton.scannerIntentRunning = true;
                    IntentIntegrator integrator = new IntentIntegrator(MultiScanFragment.this);
                    integrator.initiateScan();
                }
                // If return starts with N it's a Not acknowledged
            } else if (values[0].startsWith("N")){
                Log.d("T-DOC returns:", "Nack: "+values[0]);
                Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "Error: " + values[0]+ ".\n" +
                        "Please try again...", Toast.LENGTH_LONG)
                        .show();
                // TODO: Play some sound to indicate NACK.

                // Must have been an error since prefix is neither A nor N
            } else {
                Log.d("T-DOC returns:", "Error: "+values[0]);
                Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "Error: " + values[0]+ ".\n" +
                        "Please try again...", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

}
