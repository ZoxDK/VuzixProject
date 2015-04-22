package com.tdoc.vuzixproject;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class LoginFragment extends Fragment implements View.OnClickListener {

    //private Button buttonTest, buttonTest2;
    public static TextView instructions_login;
    private EditText etLogin;
    private View rootView;
    public static boolean nameCorrect = false;
    private String currentName = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_login, container, false);

        //buttonTest = (Button) rootView.findViewById(R.id.buttonTest);
        //buttonTest2 = (Button) rootView.findViewById(R.id.buttonTest2);
        //buttonTest.setOnClickListener(this);
        //buttonTest2.setOnClickListener(this);

        instructions_login = (TextView) rootView.findViewById(R.id.instructions_login);
        etLogin = (EditText) rootView.findViewById(R.id.etLogin);
        etLogin.setText("Name");

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override // Currently just for testing physical buttons and gestures on the M100
    public void onClick(View v) {

    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", "" + scanResult.getContents());

                if (!nameCorrect) {
                    etLogin.setText("" + scanResult.getContents());
                    currentName = scanResult.getContents();
                    instructions_login.setText("If the name in the field is correct, please say \"scan next\".");
                } else {
                    // Query Parse.com, as testing in regards to sending and receiving data
                    ParseQuery<ParseObject> query = ParseQuery.getQuery("LoginCreds");
                    query.whereEqualTo("name", currentName);
                    query.getFirstInBackground(new GetCallback<ParseObject>() {
                        // done is run when background query task returns a result, hopefully with a result object
                        public void done(ParseObject object, ParseException e) {
                            if (e == null) {
                                Log.d("data retrieved: ", object.getString("name") + " and " + object.getString("pw"));
                                if (object.getString("pw").equals(scanResult.getContents())){
                                    Log.d("Login: ", "Success!");
                                    Fragment fragment = new MainFragment();
                                    getFragmentManager().beginTransaction()
                                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                                            .replace(R.id.fragmentcontainer, fragment, "FRAG_MAIN")
                                            .addToBackStack(null)
                                            .commit();
                                    MainActivity.voiceCtrl.setCallingFragment(fragment);
                                    ApplicationSingleton.sharedPreferences.edit()
                                            .putString("currentName", currentName)
                                            .putString("pw", scanResult.getContents())
                                            .commit();
                                } else {
                                    Log.d("Login: ", "Fail!");
                                    instructions_login.setText("Login failed, please try again...");
                                    nameCorrect = false;
                                    etLogin.setText("Name");
                                }
                            } else {
                                Log.d("ParseException", "Error: " + e.getMessage() + " - code: " + e.getCode());
                                // Let the user know if the object just couldn't be found, or if it's an actual error
                                if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                    instructions_login.setText("Name " + currentName + " not found in system.\n" +
                                            "Scanned data: " + scanResult.getContents() + ".\n" +
                                            "Please try again...");
                                    nameCorrect = false;
                                    currentName = "";
                                    etLogin.setText("Name");

                                } else {
                                    instructions_login.setText("And error occurred. Please try again...");
                                    nameCorrect = false;
                                    currentName = "";
                                    etLogin.setText("Name");
                                }
                            }
                        }
                    });
                }
            }
        }
    }

}
