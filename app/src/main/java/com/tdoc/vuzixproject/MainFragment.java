package com.tdoc.vuzixproject;

import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class MainFragment extends Fragment implements View.OnClickListener {
    private Button startScanButton, startPackingListButton, startMultiScanButton, startSettingsButton;
    private TextView tvUser;
    private View rootView;
    private String[] wordList = {"back", "order", "packing list", "perpetual inventory system"};


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_main, container, false);

        tvUser = (TextView) rootView.findViewById(R.id.tvUser);
        tvUser.setText("Welcome " + ApplicationSingleton.sharedPreferences.getString("currentUserName", "Unknown User"));

        startScanButton = (Button) rootView.findViewById(R.id.startScanButton);
        startPackingListButton = (Button) rootView.findViewById(R.id.startPackingListButton);
        startMultiScanButton = (Button) rootView.findViewById(R.id.startMultiScanButton);
        startSettingsButton = (Button) rootView.findViewById(R.id.startSettingsButton);

        startScanButton.setOnClickListener(this);
        startPackingListButton.setOnClickListener(this);
        startMultiScanButton.setOnClickListener(this);
        startSettingsButton.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }


    @Override // Currently just for testing physical buttons and gestures on the M100, and for use on phones
    public void onClick(View v) {
        if (v == startScanButton) {
            Log.i("Button pressed: ", "startScanButton");

            Fragment fragment = new SingleScanFragment();
            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragmentcontainer, fragment, "FRAG_SINGLE_SCAN")
                    .addToBackStack(null)
                    .commit();

        } else if (v == startPackingListButton) {
            Log.i("Button pressed: ", "startPackingListButton");

            Fragment fragment = new PackingListFragment();
            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragmentcontainer, fragment, "FRAG_PACK")
                    .addToBackStack(null)
                    .commit();
        } else if (v == startMultiScanButton) {
            Log.i("Button pressed: ", "startPackingListButton");

            Fragment fragment = new MultiScanFragment();
            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragmentcontainer, fragment, "FRAG_MULTI_SCAN")
                    .addToBackStack(null)
                    .commit();
        } else if (v == startSettingsButton) {
            Log.i("Button pressed: ", "startPackingListButton");

            Fragment fragment = new SetupFragment();
            this.getFragmentManager().beginTransaction()
                    .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out, android.R.animator.fade_in, android.R.animator.fade_out)
                    .replace(R.id.fragmentcontainer, fragment, "FRAG_SETUP")
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

}
