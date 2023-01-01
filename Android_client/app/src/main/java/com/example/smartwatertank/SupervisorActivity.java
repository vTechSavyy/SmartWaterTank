package com.example.smartwatertank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;


public class SupervisorActivity extends AppCompatActivity {

    private static final String TAG = "SupervisorActivity";

//    private final String SERVER_API_URL = "https://automatic-water-tank.herokuapp.com/api";
    private final String SERVER_API_URL = "http://192.168.1.11:3000/api";   // Local testing!
    private int mNumTanks = 2;
    public static final String CHANNEL_ID = "Event";

    // Main view for this activity: ViewPager with Tabbed Layout:
    private ViewPager mViewPager;
    private TankPagerAdapter mPagerAdapter;
    private TabLayout mTabLayout;


    public static Intent newIntent(Context context) {
        return new Intent(context, SupervisorActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_supervisor);

        // Setup the ViewPager and its adapter:
        mViewPager = findViewById(R.id.view_pager_supervisor);
        mPagerAdapter = new TankPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);

        createNotificationChannel();

    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private class TankPagerAdapter extends FragmentPagerAdapter {

        public TankPagerAdapter(FragmentManager fm){
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {

            Fragment sFragment = SupervisorFragment.newInstance();
            Bundle args = new Bundle();
            args.putInt(SupervisorFragment.FRAG_INDEX_KEY, position + 1);
            sFragment.setArguments(args);
            return sFragment;
        }

        @Override
        public int getCount() {
            return mNumTanks;
        }
    }

}


/*

// called when response HTTP status is "200 OK"
                    try {

                        String wl_tank_1 = response.getString("water_level_tank_1");
                        String wl_tank_2 = response.getString("water_level_tank_2");
                        String pump_1_command = response.getString("pump_1_command");
                        String pump_2_command = response.getString("pump_2_command");
                        int pump_1_status = response.getInt("pump_1_status");
                        int pump_2_status = response.getInt("pump_2_status");



                        //Now we can use the value in the mPriceTextView
                        tank_1_level.setText(wl_tank_1 +  " %");
                        tank_2_level.setText(wl_tank_2 +  " %");

                        GradientDrawable d1 =  (GradientDrawable) ContextCompat.getDrawable(SupervisorActivity.this, R.drawable.water_level_background_1);
                        GradientDrawable d2 =  (GradientDrawable) ContextCompat.getDrawable(SupervisorActivity.this, R.drawable.water_level_background_2);

                        d1.setGradientCenter(0, (100 - Float.parseFloat(wl_tank_1))/100);

                        tank_1_level.setBackgroundDrawable(d1);

                        d2.setGradientCenter(0, (100 - Float.parseFloat(wl_tank_2))/100);

                        tank_2_level.setBackgroundDrawable(d2);


                        // Set the pump status text:
                        if (pump_1_status == 1) {
                            tank_1_switch.setText(R.string.tank_switch_on_txt);
                        }
                        else {
                            tank_1_switch.setText(R.string.tank_switch_off_txt);
                        }

                        if (pump_2_status == 1) {
                            tank_2_switch.setText(R.string.tank_switch_on_txt);
                        }
                        else {
                            tank_2_switch.setText(R.string.tank_switch_off_txt);
                        }


                        // Set the pump command:
                        if (pump_1_command.equals("WT_ON")) {
                            tank_1_switch.setChecked(true);
                        }

                        if(pump_1_command.equals("WT_OFF")) {
                            tank_1_switch.setChecked(false);
                        }

                        if (pump_2_command.equals("WT_ON")) {
                            tank_2_switch.setChecked(true);
                        }

                        if(pump_2_command.equals("WT_OFF")) {
                            tank_2_switch.setChecked(false);
                        }

                        // Set a flag to indicate that data has been received
                        prog_bar.setVisibility(View.GONE);


                    } catch (Exception e) {

                        Log.e("GET req - Status", e.toString());

                    }

                }

 */
