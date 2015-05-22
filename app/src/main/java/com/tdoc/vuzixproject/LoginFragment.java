package com.tdoc.vuzixproject;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private Button startScanButton;
    public static TextView instructions_login;
    private View rootView;
    private String currentUser = "";
    private String currentUserName = "";
    private ExternalCommunication extComm;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        startScanButton = (Button) rootView.findViewById(R.id.startScanButton);
        startScanButton.setOnClickListener(this);

        instructions_login = (TextView) rootView.findViewById(R.id.instructions_login);
        instructions_login.setText(R.string.instructions_login);

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override // Currently just for testing physical buttons and gestures on the M100, and for use on phones
    public void onClick(View v) {
        if (v == startScanButton){
            Log.i("Button pressed: ", "startScanButton");

            MainActivity.scannerIntentRunning = true;
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        if (MainActivity.isThereVoice) MainActivity.voiceCtrl.setCallingFragment(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", "" + scanResult.getContents());
                currentUser = scanResult.getContents();

                // Only do Parse.com queries if we are not connected to T-DOC;
                // This is for testing purposes only
                if (!ApplicationSingleton.isTDOCConnected()) {
                    parseCommunication(currentUser);
                } else {
                    // T-DOC communications
                    new connectTask().execute("");
                    //sends the message to the server
                    if (extComm != null) {
                        extComm.sendMessage(currentUser);
                    }
                }
            }
        }
    }

    private void parseCommunication(final String scanResults) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
        query.whereEqualTo("userBarcode", scanResults);
        query.getFirstInBackground(new GetCallback<ParseObject>() {
            // done is run when background query task returns a result, hopefully with a result object
            public void done(ParseObject object, ParseException e) {
                if (e == null) {
                    Log.d("Login: ", "Name: " + object.getString("userName") + " and code: " + object.getString("userBarcode"));
                    Log.d("Login: ", "Success!");
                    currentUserName = object.getString("userName");
                    Fragment fragment = new MainFragment();
                    getFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                            .replace(R.id.fragmentcontainer, fragment, "FRAG_MAIN")
                            .addToBackStack(null)
                            .commit();

                    ApplicationSingleton.sharedPreferences.edit()
                            .putString("currentUser", currentUser)
                            .putString("currentUserName", currentUserName)
                            .commit();
                } else {
                    Log.d("ParseException", "Error: " + e.getMessage() + " - code: " + e.getCode());
                    // Let the user know if the object just couldn't be found, or if it's an actual error
                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        instructions_login.setText("Name " + currentUser + " not found in system.\n" +
                                "Scanned data: " + scanResults + ".\n" +
                                "Please try again...");
                        currentUser = "";

                    } else {
                        instructions_login.setText("And error occurred. Please try again...");
                        currentUser = "";
                    }
                }
            }
        });
    }

    private class connectTask extends AsyncTask<String, String, ExternalCommunication> {

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

            Log.d("Login: ", "Success!");
            currentUserName = values[0];
            Fragment fragment = new MainFragment();
            getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragmentcontainer, fragment, "FRAG_SINGLE_SCAN")
                    .addToBackStack(null)
                    .commit();
        }
    }

}
