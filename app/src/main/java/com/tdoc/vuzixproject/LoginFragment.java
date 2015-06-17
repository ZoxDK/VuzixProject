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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class LoginFragment extends Fragment implements View.OnClickListener {

    private Button startScanButton;
    private TextView instructions_login;
    private EditText etLogin;
    private View rootView;
    private String currentUser = "";
    private String currentUserName = "";
    private ExternalCommunication extComm;
    private String[] wordList = {"bar code", "perpetual inventory system", "menu"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        startScanButton = (Button) rootView.findViewById(R.id.startScanButton);
        startScanButton.setOnClickListener(this);

        instructions_login = (TextView) rootView.findViewById(R.id.instructions_login);
        instructions_login.setText(R.string.instructions_login);

        etLogin = (EditText) rootView.findViewById(R.id.etLogin);

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override // Currently just for testing physical buttons and gestures on the M100, and for use on phones
    public void onClick(View v) {
        if (v == startScanButton){
            Log.i("Button pressed: ", "startScanButton");

            ApplicationSingleton.scannerIntentRunning = true;
            IntentIntegrator integrator = new IntentIntegrator(this);
            integrator.initiateScan();
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
                Log.i("Scan result", "" + scanResult.getContents());
                currentUser = scanResult.getContents();
                Log.d("scanResult Formatname", scanResult.getFormatName());
                etLogin.setText(currentUser);
                // T-DOC communications
                new connectTask().execute("");
                //sends the message to the server
                if (extComm != null) {
                    extComm.sendMessage(currentUser);
                }
            }
        }
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


            if (values[0].startsWith("A")){
                Log.d("T-DOC returns:", "Ack: "+values[0]);
                Log.d("Login: ", "Success!");
                // TODO: Play some sound to indicate ACK
                ApplicationSingleton.sharedPreferences.edit()
                        .putString("currentUserName", values[0])
                        .putString("currentUser", currentUser)
                        .putLong("loginTime", System.currentTimeMillis())
                        .commit();

                Fragment fragment = new MainFragment();
                getFragmentManager().beginTransaction()
                        .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                        .replace(R.id.fragmentcontainer, fragment, "FRAG_SINGLE_SCAN")
                        .addToBackStack(null)
                        .commit();

                // If return starts with N it's a Not acknowledged
            } else if (values[0].startsWith("N")){
                Log.d("T-DOC returns:", "Nack: "+values[0]);
                Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "Error: " + values[0] + ".\n" +
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
