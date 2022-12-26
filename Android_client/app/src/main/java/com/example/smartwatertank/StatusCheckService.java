package com.example.smartwatertank;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class StatusCheckService extends IntentService {

    private static final String TAG = "StatusCheckService";

    public StatusCheckService() {
        super("StatusCheckService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {

        if (intent != null) {
            final String frag_name = intent.getStringExtra(SupervisorFragment.FRAG_NAME_KEY);
            // TODO: 5/19/20 : Make a Volley Http request to the server:
            Log.i(TAG, "onHandleIntent: Fragment name is : " + frag_name);
            sendNotification(frag_name);
            }
    }

    public static void setStatusAlarm(Context ctx, boolean isOn, String frag_name) {

        // 1. Create the Pending Intent using the context of the invoking Activity/Fragment/Service
        Intent i = new Intent(ctx, StatusCheckService.class);
        i.putExtra(SupervisorFragment.FRAG_NAME_KEY, frag_name);
        PendingIntent pi = PendingIntent.getService(ctx, 0, i, 0);

        // 2. Create the AlarmManager using the context of the invoking Activity/Fragment/Service:
        AlarmManager alarmMgr = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);

        // 3. Set the repeating request that will fire up this Service each time:
        if (isOn) {
            Log.i(TAG, "setStatusAlarm: About to set Alarm!");
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), 10*1000, pi);
        }
        else {
            alarmMgr.cancel(pi);
            pi.cancel();
        }


    }

    private void sendNotification(String name){

        // Step 1: Create a pending Intent that will be used as a response when user taps notification:
        Intent i = SupervisorActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);

        // Step 2: Build the notification:
        Notification not = new NotificationCompat.Builder(this, SupervisorActivity.CHANNEL_ID)
                .setTicker("Tank App")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentTitle(name)
                .setContentText("Pump swicthed ON!")
                .setContentIntent(pi)
                .setAutoCancel(true).build();

        // Step 3: Send the notification through Notification Manager:
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(0, not);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy: Called!");
    }
}
