package com.tdoc.vuzixproject;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;
import java.util.List;

public class PackingListFragment extends Fragment {

    private View rootView;
    private int currentItemPos = 0;
    private ArrayList<String> itemList = new ArrayList<>();
    private TableLayout tableLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_packing_list, container, false);

        tableLayout = (TableLayout) rootView.findViewById(R.id.packinglistTable);

        //buttonTest2 = (Button) rootView.findViewById(R.id.buttonTest2);
        //buttonTest.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        MainActivity.voiceCtrl.setCallingFragment(this);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", "" + scanResult.getContents());
                ParseQuery<ParseObject> query = ParseQuery.getQuery("PackingList");
                query.orderByAscending("Order");
                query.findInBackground(new FindCallback<ParseObject>() {
                    public void done(List<ParseObject> returnList, ParseException e) {
                        if (e == null) {
                            Log.d("List return: ", ""+returnList.toString());
                            for (ParseObject item : returnList){
                                itemList.add(item.getString("item"));
                                TableRow tableRow = new TableRow(getActivity());
                                tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

                                TextView tv = new TextView(getActivity());
                                tv.setText(""+item.getString("item"));
                                tv.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                CheckBox cb = new CheckBox(getActivity());
                                cb.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.FILL_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                cb.setChecked(item.getBoolean("ScannedYet"));
                                tableRow.addView(tv,0);
                                tableRow.addView(cb,1);

                                tableLayout.addView(tableRow, new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                            }
                            Log.d("itemList: ", itemList.toString());

                        } else {
                            Log.d("List return: ", "Error: " + e.getMessage());
                        }
                    }
                });

            }
        }
    }

}
