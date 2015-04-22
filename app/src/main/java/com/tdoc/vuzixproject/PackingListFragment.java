package com.tdoc.vuzixproject;

import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.List;

public class PackingListFragment extends ListFragment{

    private View rootView;
    private List<String> itemList;
    private int currentItemPos = 0;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_packing_list, container, false);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("PackingList");
        query.orderByDescending("Order");
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> returnList, ParseException e) {
                if (e == null) {
                    Log.d("List return: ", ""+returnList.toString());
                    for (ParseObject item : returnList){
                        itemList.add(item.getString("item"));
                    }

                } else {
                    Log.d("score", "Error: " + e.getMessage());
                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                R.layout.listelements, itemList);
        setListAdapter(adapter);
        // Inflate the layout for this fragment
        return rootView;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        // Currently only getting scan results, but check for request code to be sure
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            MainActivity.scannerIntentRunning = false;
            // Convert to preferred ZXing IntentResult
            final IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
            if (scanResult != null) {
                Log.i("Scan result", "" + scanResult.getContents());
                if (getListAdapter().getItem(currentItemPos).equals(""+scanResult.getContents())){

                    currentItemPos++;
                    CheckBox checkBox = (CheckBox) rootView.findViewById(R.id.list_elem_checkbox);
                    checkBox.setChecked(true);
                }

            }
        }
    }

}
