package com.example.smartwatertank;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


/**
 * A simple {@link Fragment} subclass.
 */
public class SupervisorFragment extends Fragment {

    public static final String FRAG_INDEX_KEY = "FragmentIndex";
    public static final String FRAG_NAME_KEY = "FragmentName";
    private static final String TAG = "SupervisorFragment";

    private final String SERVER_URL = "http://192.168.29.224:3000";
//    private final String SERVER_URL = "https://plum-cockroach-gown.cyclic.app";
//    private final String SERVER_URL = "https://pereira-smart-water-tank.onrender.com";

    private final String STATUS_URL = SERVER_URL + "/api/status";
    private final String ACTUATION_URL = SERVER_URL + "/api/commands";

    private JsonObjectRequest tankStatusRequest;
    private JsonObjectRequest pumpActuationRequest;

    private Handler UIThreaHandler;
    private static int statusInterval = 20000;  // milliseconds

    private TextView mTitle;
    private TextView mTankLevel;
    private Switch mTankSwitch;
    private ProgressBar mSwitchRequestProgressBar;

    public SupervisorFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        return new SupervisorFragment();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UIThreaHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_supervisor, container, false);

        mTitle = rootView.findViewById(R.id.textview_title);

        mTankLevel = rootView.findViewById(R.id.tank_level);
        mSwitchRequestProgressBar = rootView.findViewById(R.id.progress_bar_switch);

        mTankSwitch = rootView.findViewById(R.id.tank_switch);
        mTankSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

                final String idxStr = String.valueOf(getArguments().getInt(FRAG_INDEX_KEY));
                JSONObject cmd_req = new JSONObject(new HashMap<String, String>() {
                    {
                        put("pump_" + idxStr, isChecked ? "ON" : "OFF");
                    }
                });

                pumpActuationRequest = new JsonObjectRequest(Request.Method.POST, ACTUATION_URL, cmd_req, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.i(TAG, "onResponse: Pump actuation. Response completed! ");
                        mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                        try {
                            String res_status = response.getString("status");
                            Log.i(TAG, "Msg is " + response.getString("msg"));
                            Log.i(TAG, " Status is: " + response.getString("status"));
                            Toast.makeText(getActivity(), response.getString("msg"), Toast.LENGTH_LONG).show();
                            if (res_status.equals("ON"))
                            {
                                mTankSwitch.setChecked(true);
                                mTankSwitch.setText(R.string.tank_switch_on_txt);
                                mTankSwitch.setBackgroundColor(Color.argb(50,0,255, 0));
                            }
                            else if (res_status.equals("OFF"))
                            {
                                mTankSwitch.setChecked(false);
                                mTankSwitch.setText(R.string.tank_switch_off_txt);
                                mTankSwitch.setBackgroundColor(Color.argb(50,255,0, 0));
                            }
                            else
                            {
                                Log.i(TAG, " Welp!");
                            }

                        }
                        catch (JSONException e){
                            Log.e(TAG, "onResponse: JSON Exception " + e.toString() );
                        }


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "onErrorResponse: " + error.toString() );
                        mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                    }
                });

                pumpActuationRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                       2,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                pumpActuationRequest.setTag(TAG);

                // Send http request to server:
                // Start progressbar:
                // Stop progressbar on response from HTTP req:
                if (buttonView.isPressed())
                {
                    mSwitchRequestProgressBar.setVisibility(View.VISIBLE);
                    VolleySingleton.getInstance(getActivity()).addToRequestQueue(pumpActuationRequest);
                }

            }
        });
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final String idxStr = String.valueOf(getArguments().getInt(FRAG_INDEX_KEY));
        String titleStr = "A-14" + idxStr;
        mTitle.setText(titleStr);

        tankStatusRequest = new JsonObjectRequest(Request.Method.GET, STATUS_URL, null,  new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
//                Log.i(TAG, " Received status update");
                try
                {
                    String level = response.getString("water_level_tank_" + idxStr);
                    mTankLevel.setText(level + " cm");
                    GradientDrawable d1 = (GradientDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.water_level_background_1);
                    d1.setGradientCenter(0.0f, (100.0f - Float.parseFloat(level)) / 100.0f);
                    SupervisorFragment.this.mTankLevel.setBackground(d1);

                    String pump_status = response.getString("pump_" + idxStr + "_status");

                    if (pump_status.equals("ON")) {
                        mTankSwitch.setChecked(true);
                        mTankSwitch.setText(R.string.tank_switch_on_txt);
                        mTankSwitch.setBackgroundColor(Color.argb(50, 0, 255, 0));
                        mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                    }

                    if (pump_status.equals("OFF")) {
                        mTankSwitch.setChecked(false);
                        mTankSwitch.setText(R.string.tank_switch_off_txt);
                        mTankSwitch.setBackgroundColor(Color.argb(50, 255, 0, 0));
                        mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                    }
                }
                catch (JSONException e){
                    Log.e(TAG, "onResponse: JSON Exception " + e.toString() );
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: " + error.toString() );
            }
        });

        tankStatusRequest.setTag(TAG);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: Called!");
        startStatusUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.i(TAG, "onPause: Called!");
        stopStatusUpdates();
    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
            VolleySingleton.getInstance(getActivity()).addToRequestQueue(tankStatusRequest);
            UIThreaHandler.postDelayed(mStatusChecker, statusInterval);
        }
    };

    private void startStatusUpdates() {
        mStatusChecker.run();
    }

    private void stopStatusUpdates() {
        UIThreaHandler.removeCallbacks(mStatusChecker);
        VolleySingleton.getInstance(getActivity()).cancelRequests(TAG);
    }


}
