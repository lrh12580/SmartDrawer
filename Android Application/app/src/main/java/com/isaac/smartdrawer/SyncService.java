package com.isaac.smartdrawer;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;

public class SyncService extends IntentService {
    private static final String EXTRA_TYPE = "com.isaac.smartdrawer.syncservice_type";
    private static final String TAG = "SyncService";
    private static final int ID_SERVICE_ALARM = 21567;
    private static final int ID_SMART_ALERT = 21568;
    private static final int ID_SMART_ALARM_AUTO = 21569;

    private static final int INTERVAL = 1000 * 60;

    public static void setServiceAlarm(Context context, boolean isOn) {
        Intent intent = SyncService.newIntent(context, ID_SERVICE_ALARM);
        PendingIntent pi = PendingIntent.getService(context, ID_SERVICE_ALARM, intent, 0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (isOn) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), INTERVAL, pi);
        } else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
    }

    public static void setSmartAlert(Context context, boolean isOn) {
        Intent intent = SyncService.newIntent(context, ID_SMART_ALERT);
        PendingIntent pi = PendingIntent.getService(context, ID_SMART_ALERT, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 7);
        c.set(Calendar.MINUTE, 40);
        if (isOn) {
            am.setInexactRepeating(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
        } else {
            am.cancel(pi);
            pi.cancel();
        }

        setAlarmAuto(context, isOn);
    }

    public static void setAlarmAuto(Context context, boolean isOn) {
        Intent intent = SyncService.newIntent(context, ID_SMART_ALARM_AUTO);
        PendingIntent pi = PendingIntent.getService(context, ID_SMART_ALARM_AUTO, intent, 0);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar c = Calendar.getInstance();
        if (isOn) {
            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), 5*AlarmManager.INTERVAL_DAY, pi);
        } else {
            am.cancel(pi);
            pi.cancel();
        }
    }

    public static Intent newIntent(Context context, int type) {
        Intent intent = new Intent(context, SyncService.class);
        intent.putExtra(EXTRA_TYPE, type);
        return intent;
    }

    public SyncService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (!isNetworkAvailableAndConnected()) return;

        switch (intent.getIntExtra(EXTRA_TYPE, 0)) {
            case ID_SERVICE_ALARM:
//                if (isAppForeground()) break;
                sync();
                break;

            case ID_SMART_ALERT:
                handleWeather();
                break;

//            case ID_SMART_ALARM_AUTO:
//                handleAlarmAuto();
//                break;

        }
    }

    private void handleWeather() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!(lm.isProviderEnabled(LocationManager.GPS_PROVIDER )&& sync())) return;
        String[] weather = Weather.getWeatherInfo(this);
        if (weather == null) return;
        boolean notify = false;
        StringBuilder sb = new StringBuilder();
        if (weather[0].contains("雨")) {
            List<Content> contents = ContentLab.instance(this).getContentsByName("伞");
            for (int i=0;i<contents.size();i++) {
                if (contents.get(i).isExist()) {
                    notify = true;
                    sb.append("今日有雨，别忘了带"+contents.get(i).getName()+"哦！");
                    break;
                }
            }
        }
        if (!(weather[1].contains("良") || weather[1].contains("优"))) {
            List<Content> contents = ContentLab.instance(this).getContentsByName("口罩");
            for (int i=0;i<contents.size();i++) {
                if (contents.get(i).isExist()) {
                    notify = true;
                    sb.append("今日空气"+weather[1]+"，别忘了带"+contents.get(i).getName()+"哦！");
                    break;
                }
            }
        }
        if (notify) {
            PendingIntent pi = PendingIntent.getActivity(this,2*ID_SERVICE_ALARM, HistoryActivity.newIntent(this), 0);
            Notification not = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.alarm_notify)
                    .setTicker(sb)
                    .setContentTitle("智能提示：")
                    .setContentText(sb)
                    .setContentIntent(pi)
                    .build();
            not.flags |= Notification.FLAG_AUTO_CANCEL;
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.notify(1, not);
        }
    }

    private boolean sync() {
        int[] out_new = ContentLab.instance(this).setExist(Client.instance().getIds());
        if (out_new == null) return true;
        StringBuilder sb = new StringBuilder();
        if (out_new[0] > 0) sb.append(out_new[0]+" 件物品被取出 ");
        if (out_new[1] > 0) sb.append(out_new[1]+" 件新物品被放入 ");

        PendingIntent pi = PendingIntent.getActivity(this,2*ID_SERVICE_ALARM,HistoryActivity.newIntent(this), 0);
        Notification not = new Notification.Builder(this)
                .setSmallIcon(R.drawable.alarm_notify)
                .setTicker("物品情况变化！")
                .setContentTitle("最近一次的物品情况变化")
                .setContentText(sb)
                .setContentIntent(pi)
                .build();

        not.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, not);
        return true;
    }

//    private void handleAlarmAuto() {
//        String info = null;
//        try {
//            info = new FileService(this).FileRead("output");
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        if (info == null) return;
//
//
//    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = cm.getActiveNetworkInfo().isConnected();
        return isNetworkAvailable && isNetworkConnected;
    }

    private boolean isAppForeground() {
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String currentPackageName = am.getRunningTasks(1).get(0).topActivity.getPackageName();
        return currentPackageName != null && currentPackageName.equals(getPackageName());
    }
}
