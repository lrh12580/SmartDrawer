package com.isaac.smartdrawer;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


/**
 * Created by admin on 2016/9/2.
 */
public class AlarmService extends IntentService {
    public static final String TAG = "AlarmService";
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mClientDatabaseHelper = ClientDatabaseHelper.getInstance(AlarmService.this);
//        Intent intent = getIntent();
//        int alarm_id = intent.getIntExtra("alarm_id", 0);
//        Log.d("AlarmService", alarm_id + "");
//        Alarms alarms = mClientDatabaseHelper.getAlarmByAlarmId(alarm_id);
//        Log.d(TAG, alarms.getDate_year()+"");
//        Log.d(TAG, alarms.getDate_month()+"");
//        Log.d(TAG, alarms.getDate_day()+"");
//        Log.d(TAG, alarms.getDate_hour()+"");
//        Log.d(TAG, alarms.getDate_minute()+"");
//        Log.d(TAG, alarms.getIds().get(0));
//        Log.d(TAG, alarms.getIds().get(1));
//        mClientDatabaseHelper.deleteAlarms(alarm_id + "");
//        Log.d("AlarmService", mClientDatabaseHelper.getAlarmsCount() + " ");
//    }

    public AlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        int alarm_id = intent.getIntExtra(Alarms.ALARM_ID, -1);
        if (alarm_id < 0) return;
        ClientDatabaseHelper helper = ClientDatabaseHelper.getInstance(this);
        Alarms alarms = helper.getAlarmByAlarmId(alarm_id);
        helper.deleteAlarms(alarm_id+"");
        Notification not = new Notification.Builder(this)
                .setSmallIcon(R.drawable.alarm_notify)
                .setTicker(alarms.getInfo())
                .setContentTitle("注意啦！")
                .setContentText(alarms.getInfo())
                .build();
        not.flags |= Notification.FLAG_AUTO_CANCEL;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, not);
    }
}
