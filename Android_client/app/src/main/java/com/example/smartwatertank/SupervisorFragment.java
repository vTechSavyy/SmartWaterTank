package com.example.smartwatertank;


import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
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

    private final String STATUS_URL = SERVER_URL + "/api/status";
    private final String ACTUATION_URL = SERVER_URL + "/api/commands";

    private JsonObjectRequest tankStatusRequest;
    private JsonObjectRequest pumpActuationRequest;

    private Handler UIThreaHandler;
    private static int statusInterval = 10000;  // milliseconds

    private TextView mTitle;
    private TextView mAutoStartTimeDisplay;
    private TextView mTankLevel;
    private CheckBox mAutoStartCheckBox;
    private ImageButton mAutoStartEditButton;
    private Switch mTankSwitch;
    private ProgressBar mSwitchRequestProgressBar;


    private TimePickerDialog.OnTimeSetListener mOnTimeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            String timeStr = hourOfDay + " : " + minute;
            mAutoStartTimeDisplay.setText(timeStr);
        }
    };

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
        mAutoStartEditButton = rootView.findViewById(R.id.auto_start_edit_button);
        mAutoStartEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment mTimePickerFragment = new AutoStartTimePicker(mOnTimeSetListener);
                mTimePickerFragment.show(getActivity().getSupportFragmentManager(), "timePicker");
            }
        });

        mAutoStartTimeDisplay = rootView.findViewById(R.id.auto_start_time_display);
        // TODO: 5/19/20 : Get initial value of AutoStartTimeDisplay either from Shared Preferences or from Server 

        mAutoStartCheckBox = rootView.findViewById(R.id.auto_start_checkbox);
        mAutoStartCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                
                if (isChecked) {
                    mAutoStartTimeDisplay.setVisibility(View.VISIBLE);
                    mAutoStartEditButton.setVisibility(View.VISIBLE);

                }
                else {
                    mAutoStartTimeDisplay.setVisibility(View.INVISIBLE);
                    mAutoStartEditButton.setVisibility(View.INVISIBLE);
                    mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                    mTankSwitch.setText(R.string.tank_switch_off_txt);
                    mTankSwitch.setBackgroundColor(Color.argb(50,255,0, 0));
                }
                
            }
        });

        mTankLevel = rootView.findViewById(R.id.tank_level);
        mSwitchRequestProgressBar = rootView.findViewById(R.id.progress_bar_switch);

        mTankSwitch = rootView.findViewById(R.id.tank_switch);
        mTankSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {

                final String idxStr = String.valueOf(getArguments().getInt(FRAG_INDEX_KEY));
                JSONObject cmd_req = new JSONObject(new HashMap<String, String>() {
                    {
                        put("pump_" + idxStr, isChecked ? "WT_ON" : "WT_OFF");
                    }
                });

                // Send http request to server:
                // Start progressbar:
                // Stop progressbar on response from HTTP req:
                if (isChecked) {
                    mSwitchRequestProgressBar.setVisibility(View.VISIBLE);
                    pumpActuationRequest = new JsonObjectRequest(Request.Method.POST, ACTUATION_URL, cmd_req, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                                Log.i(TAG, "onResponse: Pump actuation. Response completed! ");
                                mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                                mTankSwitch.setText(R.string.tank_switch_on_txt);
                                mTankSwitch.setBackgroundColor(Color.argb(50,0,255, 0));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse: " + error.toString() );
                        }
                    });

                    pumpActuationRequest.setTag(TAG);
                    VolleySingleton.getInstance(getActivity()).addToRequestQueue(pumpActuationRequest);

                }
                else {
                    mSwitchRequestProgressBar.setVisibility(View.VISIBLE);

                    pumpActuationRequest = new JsonObjectRequest(Request.Method.POST, ACTUATION_URL, cmd_req, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.i(TAG, "onResponse: Pump actuation. Response completed! ");
                            mSwitchRequestProgressBar.setVisibility(View.INVISIBLE);
                            mTankSwitch.setText(R.string.tank_switch_off_txt);
                            mTankSwitch.setBackgroundColor(Color.argb(50,255,0, 0));
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e(TAG, "onErrorResponse: " + error.toString() );
                        }
                    });

                    pumpActuationRequest.setTag(TAG);
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

        tankStatusRequest = new JsonObjectRequest(Request.Method.GET, STATUS_URL, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.i(TAG, " Received status update");
                try
                {
                    String level = response.getString("water_level_tank_" + idxStr);
                    mTankLevel.setText(level + " cm");
                    GradientDrawable d1 = (GradientDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.water_level_background_1);
                    d1.setGradientCenter(0.0f, (100.0f - Float.parseFloat(level)) / 100.0f);
                    SupervisorFragment.this.mTankLevel.setBackground(d1);
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


    public static class AutoStartTimePicker extends DialogFragment {

        private TimePickerDialog.OnTimeSetListener mListener;

        AutoStartTimePicker(TimePickerDialog.OnTimeSetListener listener) {
            mListener = listener;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            super.onCreateDialog(savedInstanceState);
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new android.app.TimePickerDialog(getActivity(), mListener, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));

        }

    }

    Runnable mStatusChecker = new Runnable() {
        @Override
        public void run() {
//            Log.i(TAG, " Adding volley request to queue");
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
