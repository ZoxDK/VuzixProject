package com.tdoc.vuzixproject;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class PackingListFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private Button backButton;
    private int currentItemPos = 0;
    private boolean hasActiveList = false;
    private String currentListBarcode, currentBarcode;
    private ArrayList<ParseObject> itemList = new ArrayList<>();
    private TableLayout tableLayout;
    private ExternalCommunication extComm;
    private String[] wordList = {"back", "menu", "scan", "bar code", "perpetual inventory system"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //super.onCreateView(inflater, container, savedInstanceState);

        rootView = inflater.inflate(R.layout.fragment_packing_list, container, false);

        tableLayout = (TableLayout) rootView.findViewById(R.id.packinglistTable);

        backButton = (Button) rootView.findViewById(R.id.menuButton);
        backButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onClick(View v) {
        if (v == backButton){
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
                currentBarcode = scanResult.getContents();

                // T-DOC communications
                new connectTask().execute("");
                //sends the message to the server
                if (extComm != null) {
                    extComm.sendMessage(scanResult.getContents());
                }

                // Only do Parse.com queries if we are not connected to T-DOC;
                // This is for testing purposes only
                if (!ApplicationSingleton.isTDOCConnected()) {
                    // If no list, get one; otherwise scans should compare to list
                    if (!hasActiveList) {
                        currentListBarcode = currentBarcode;
                        //currentListBarcode.toLowerCase();
                        //currentListBarcode.replace(" ", "_");
                        ApplicationSingleton.sharedPreferences.edit()
                                .putString("currentPacklistBarcode", currentListBarcode)
                                .commit();
                        System.out.println("No active list");
                        Log.i("Scan result", "" + currentBarcode);

                        ParseQuery<ParseObject> query = ParseQuery.getQuery(currentListBarcode);
                        query.orderByAscending("Order");
                        query.findInBackground(new FindCallback<ParseObject>() {
                            public void done(List<ParseObject> returnList, ParseException e) {
                                if (e == null) {
                                    Log.d("List return: ", "" + returnList.toString());
                                    for (ParseObject item : returnList) {

                                        itemList.add(item);
                                        TableRow tableRow = new TableRow(getActivity());
                                        tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                        TextView tv = new TextView(getActivity());
                                        tv.setText("" + item.getString("item"));
                                        tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                        CheckBox cb = new CheckBox(getActivity());
                                        cb.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                        cb.setChecked(item.getBoolean("ScannedYet"));
                                        cb.setId(item.getInt("Order") - 1);
                                        tableRow.addView(tv, 0);
                                        tableRow.addView(cb, 1);
                                        hasActiveList = true;

                                        tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                    }
                                    Log.d("itemList: ", itemList.toString());

                                } else {
                                    Log.d("List return: ", "Error: " + e.getMessage());
                                }
                            }
                        });
                    } else {
                        System.out.println("Active list");
                        Log.d("Scan result", "" + currentBarcode);

                        ParseQuery<ParseObject> query = ParseQuery.getQuery(currentListBarcode);
                        query.whereEqualTo("item", currentBarcode);
                        query.getFirstInBackground(new GetCallback<ParseObject>() {
                            public void done(ParseObject object, ParseException e) {
                                if (e == null) {
                                    Log.d("List item: ", "" + object.toString());

                                    CheckBox cb = (CheckBox) tableLayout.findViewById(object.getInt("Order") - 1);
                                    cb.setChecked(true);

                                } else {
                                    if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                                        Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "Item" + object.getString("item") + " not found in list.\n" +
                                                "Scanned data: " + scanResult.getContents() + ".\n" +
                                                "Please try again...", Toast.LENGTH_LONG)
                                                .show();

                                    } else Log.d("Item return: ", "Error: " + e.getMessage());
                                }
                            }
                        });
                    }
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

            //in the arrayList we add the messaged received from server
            if (hasActiveList && ApplicationSingleton.isTDOCConnected()){
                // If return starts with A it's an Acknowledge
                if (values[0].startsWith("A")){
                    Log.d("T-DOC returns:", "Ack: "+values[0]);
                    for (ParseObject item : itemList){
                        if (item.getString("item").equals(currentBarcode) && !item.getBoolean("ScannedYet")){
                            CheckBox cb = (CheckBox) tableLayout.findViewById(item.getInt("Order"));
                            cb.setChecked(true);
                        }
                    }
                    // TODO: Play some sound to indicate ACK
                    // Also, can there be several of the same item?

                // If return starts with N it's a Not acknowledged
                } else if (values[0].startsWith("N")){
                    Log.d("T-DOC returns:", "Nack: "+values[0]);
                    Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "Error: " + values[0]+ ".\n" +
                            "Scanned data: " + currentBarcode + ".\n" +
                            "Please try again...", Toast.LENGTH_LONG)
                            .show();
                    // TODO: Play some sound to indicate NACK.

                // Must have been an error since prefix is neither A nor N
                } else {
                    Log.d("T-DOC returns:", "Error: "+values[0]);
                    Toast.makeText(ApplicationSingleton.getInstance().getBaseContext(), "Error: " + values[0]+ ".\n" +
                            "Scanned data: " + currentBarcode + ".\n" +
                            "Please try again...", Toast.LENGTH_LONG)
                            .show();
                }
            }
        }
    }
}
