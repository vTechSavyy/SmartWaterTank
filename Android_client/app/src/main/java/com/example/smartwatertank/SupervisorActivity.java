package com.example.smartwatertank;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import org.json.JSONException;
import org.json.JSONObject;


public class SupervisorActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SupervisorActivity";
    private final String SERVER_URL = "https://pereira-smart-water-tank.onrender.com";
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
        setupSharedPreferences();
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals("signature")) {
            Log.i(TAG, " Signature pref changed!");
            Log.i(TAG, "Signature value is: " + sharedPreferences.getAll().toString());
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(SupervisorActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public String getServerUrl() {
        return SERVER_URL;
    }

}
