package com.isaac.smartdrawer;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by admin on 2016/8/1.
 */
public class Alarms {
    private int alarm_id;
    private String date, ids, info;
    private int date_year, date_month, date_day, date_hour, date_minute, dayofWeek, dayofYear;
    private Calendar calendar;
    private Date myDate;
    public static final String ALARM_ID = "alarm_id";

    public static void setAlarm(Context context, String date, String ids, String info) {
        ClientDatabaseHelper mClientDatabaseHelper;

        SharedPreferences preferences = context.getSharedPreferences(ALARM_ID, Context.MODE_PRIVATE);
        int alarm_id = preferences.getInt(ALARM_ID, 0) + 1;
        SharedPreferences.Editor editor = context.getSharedPreferences(ALARM_ID, Context.MODE_PRIVATE).edit();
        editor.putInt(ALARM_ID, alarm_id);
        editor.apply();
        Alarms alarms = new Alarms(alarm_id, date, ids, info);
        Calendar calendar = Calendar.getInstance();
        int add = calculateDaysNow(date);
        if (add < 0) {//设置在今天之前的闹钟
            Toast.makeText(context, "请确保您设定的日期正确"
                    , Toast.LENGTH_SHORT).show();
        } else {
            //闹钟设定成功的情况
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.SECOND, add);
            AlarmManager alarmManager =
                    (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent i1 = new Intent(context, AlarmService.class);
            i1.putExtra(ALARM_ID, alarm_id);
            // 创建PendingIntent对象
            PendingIntent pi = PendingIntent.getService(context, alarm_id, i1, 0);
//            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),pi);

            Intent i2 = new Intent(context, AlarmManagerActivity.class);
            i2.putExtra(ALARM_ID, alarm_id);
            PendingIntent pi2 = PendingIntent.getActivity(context, alarm_id, i2, 0);

            AlarmManager.AlarmClockInfo alarmClockInfo =
                    new AlarmManager.AlarmClockInfo(calendar.getTimeInMillis(),
                            pi2);
            alarmManager.setAlarmClock(alarmClockInfo, pi);
            Toast.makeText(context, "闹铃设置成功啦"
                    , Toast.LENGTH_SHORT).show();
            mClientDatabaseHelper = ClientDatabaseHelper.getInstance(context);
            mClientDatabaseHelper.insertAlarms(alarms);
        }
    }

    public Alarms(int alarm_id, String date, String ids, String info) {
        this.alarm_id = alarm_id;
        this.date = date;
        this.ids = ids;
        this.info = info;
        calendar = Calendar.getInstance();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        try {
            myDate = simpleDateFormat.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        calendar.setTime(myDate);
        date_year = calendar.get(Calendar.YEAR);
        date_month = calendar.get(Calendar.MONTH);
        date_day = calendar.get(Calendar.DAY_OF_MONTH);
        date_hour = calendar.get(Calendar.HOUR_OF_DAY);
        date_minute = calendar.get(Calendar.MINUTE);
        dayofWeek = calendar.get(Calendar.DAY_OF_WEEK);
        dayofYear = calendar.get(Calendar.DAY_OF_YEAR);
    }

    public int getAlarm_id(){
        return alarm_id;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<String> getIds() {
        ArrayList<String> mIds = new ArrayList<>();
        JSONObject jsonObj;
        JSONArray array = null;
        try {
            jsonObj = new JSONObject(ids);
            array = jsonObj.getJSONArray("ids");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < array.length(); i++) {
            try {
                mIds.add((String) array.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return mIds;
    }

    public String getId() {
        return ids;
    }

    public String getInfo() {
        return info;
    }

    public int getDate_year() {
        return date_year;
    }

    public int getDate_month() {
        return date_month+1;
    }

    public int getDate_day() {
        return date_day;
    }

    public int getDate_hour() {
        return date_hour;
    }

    public int getDate_minute() {
        return date_minute;
    }

    public int getDayofYear() {
        return dayofYear;
    }

    public int getDate_dayofWeek() {
        return dayofWeek;
    }

    public static int calculateDaysNow(String str) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Date now = null;
        Date date= null;
        try {
            date = simpleDateFormat.parse(str);
            Calendar calendar = Calendar.getInstance();
            String dates = calendar.get(Calendar.YEAR) + "-"
                    + (int)(calendar.get(Calendar.MONTH)+1) + "-" + calendar.get(Calendar.DAY_OF_MONTH)
                    +" " + calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"
                    +calendar.get(Calendar.SECOND);
            now = simpleDateFormat.parse(dates);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        int l= (int) ((date.getTime()-now.getTime())/1000);
        return l;
    }
}
