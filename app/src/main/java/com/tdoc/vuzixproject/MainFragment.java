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

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

public class MainFragment extends Fragment implements View.OnClickListener{

    private Button buttonTest, buttonTest2;
    private TextView tvData;
    private int clickedTimes = 0;
    private View rootView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        buttonTest = (Button) rootView.findViewById(R.id.buttonTest);
        buttonTest2 = (Button) rootView.findViewById(R.id.buttonTest2);
        buttonTest.setOnClickListener(this);
        buttonTest2.setOnClickListener(this);

        tvData = (TextView) rootView.findViewById(R.id.tvData);

        // Inflate the layout for this fragment
        return rootView;
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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", "" + scanResult.getContents());

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

}