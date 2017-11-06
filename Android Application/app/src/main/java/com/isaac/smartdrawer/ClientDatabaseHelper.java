package com.isaac.smartdrawer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.isaac.smartdrawer.ContentDbSchema.HistoryTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by admin on 2016/8/4.
 */
public class ClientDatabaseHelper extends SQLiteOpenHelper {

    public static final String TAG = "ClientDatabaseHelper";
    private static final String DB_NAME = "drawer_client.db";
    private static final int DB_VERSION = 1;
    private static final String DB_CREATE_TABLE_HISTORY = "create table " + HistoryTable.NAME
            + " (" + HistoryTable.Cols.ID + " text, "
            + HistoryTable.Cols.NAME + " text, "
            + HistoryTable.Cols.CATEGORY + " text, "
            + HistoryTable.Cols.EXIST + " integer, "
            + HistoryTable.Cols.DATE + " text)";

    private static final String DB_CREATE_TABLE_ALARMS = "create table " + ContentDbSchema.AlarmsTable.NAME
            + " (" + ContentDbSchema.AlarmsTable.Cols.ALARM_ID + " integer primary key, "
            + ContentDbSchema.AlarmsTable.Cols.DATE + " text, "
            + ContentDbSchema.AlarmsTable.Cols.IDS + " text, "
            + ContentDbSchema.AlarmsTable.Cols.INFO + " text)";

    private static ClientDatabaseHelper mInstance;

    protected ClientDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    public synchronized static ClientDatabaseHelper getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ClientDatabaseHelper(context);
        }
        return mInstance;
    }

    public synchronized static void destoryInstance() {
        if (mInstance != null) {
            mInstance.close();
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DB_CREATE_TABLE_HISTORY);
        db.execSQL(DB_CREATE_TABLE_ALARMS);
    }

    public void clear() {
        getWritableDatabase().execSQL("DELETE FROM " + HistoryTable.NAME);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    }

    public synchronized int getHistoryCount() {
        Log.d(TAG, "getHistoryCount");
        int count = -1;
        Cursor c = getReadableDatabase().query(HistoryTable.NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            count = c.getCount();
        }
        c.close();
        c = null;
        return count;
    }

    public synchronized int getAlarmsCount() {
        Log.d(TAG, "getAlarmsCount");
        int count = -1;
        Cursor c = getReadableDatabase().query(ContentDbSchema.AlarmsTable.NAME, null, null, null, null, null, null);
        if (c.moveToFirst()) {
            count = c.getCount();
        }
        c.close();
        c = null;
        return count;
    }

    public List<Goods> getHistories() {
        List<Goods> goods = new ArrayList<>();

        ContentCursorWrapper cursor = queryHistories(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                goods.add(cursor.getHistory());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return goods;
    }

    public List<Alarms> getAlarms() {
        List<Alarms> alarms = new ArrayList<>();

        ContentCursorWrapper cursor = queryAlarms(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                alarms.add(cursor.getAlarm());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return alarms;
    }

    private ContentCursorWrapper queryHistories(String whereClause, String[] whereArgs) {
        Cursor cursor = getWritableDatabase().query(
                HistoryTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new ContentCursorWrapper(cursor);
    }

    private ContentCursorWrapper queryAlarms(String whereClause, String[] whereArgs) {
        Cursor cursor = getWritableDatabase().query(
                ContentDbSchema.AlarmsTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null
        );
        return new ContentCursorWrapper(cursor);
    }

    public synchronized void insertHistory(Goods good) {
        Log.d(TAG, "insertHistory");
        ContentValues values = new ContentValues(5);
        values.put(HistoryTable.Cols.ID, good.getId());
        values.put(HistoryTable.Cols.NAME, good.getName());
        values.put(HistoryTable.Cols.CATEGORY, good.getCategory());
        values.put(HistoryTable.Cols.EXIST, good.getExist());
        values.put(HistoryTable.Cols.DATE, good.getDate());
        getWritableDatabase().insert(HistoryTable.NAME, null, values);
    }

    public synchronized void insertAlarms(Alarms alarm) {
        Log.d(TAG, "insertAlarms");
        ContentValues values = new ContentValues(4);
        values.put(ContentDbSchema.AlarmsTable.Cols.ALARM_ID, alarm.getAlarm_id());
        values.put(ContentDbSchema.AlarmsTable.Cols.DATE, alarm.getDate());
        values.put(ContentDbSchema.AlarmsTable.Cols.IDS, alarm.getId());
        values.put(ContentDbSchema.AlarmsTable.Cols.INFO, alarm.getInfo());
        getWritableDatabase().insert(ContentDbSchema.AlarmsTable.NAME, null, values);
    }

    public synchronized void updateHistoryCategory(String id, String categoty) {
        Log.d(TAG, "updateHistoryCategory");
        ContentValues value = new ContentValues();
        value.put(HistoryTable.Cols.CATEGORY, categoty);
        getWritableDatabase().update(HistoryTable.NAME, value, "id = ?", new String[]{id});
    }

    public synchronized void updateAlarmsDate(String alarm_id, String date) {
        Log.d(TAG, "updateAlarmsDate_out");
        ContentValues value = new ContentValues();
        value.put(ContentDbSchema.AlarmsTable.Cols.DATE, date);
        getWritableDatabase().update(ContentDbSchema.AlarmsTable.NAME, value, "alarm_id = ?", new String[]{alarm_id});
    }

    public synchronized void deleteAlarms(String alarm_id) {
        Log.d(TAG, "deleteAlarms");
        getWritableDatabase().delete(ContentDbSchema.AlarmsTable.NAME, "alarm_id = ?", new String[]{alarm_id});
    }

    public synchronized boolean isFindHistory(String name) {
        Log.d(TAG, "isFindHistory");
        String mName = null;
        Cursor cursor = getWritableDatabase().query(HistoryTable.NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            mName = cursor.getString(cursor.getColumnIndex(HistoryTable.Cols.NAME));
            if (name.equals(mName)) {
                return true;
            }
        }
        cursor.close();
        return false;
    }

    public synchronized int getHistoryNameCount(String name) {
        Log.d(TAG, "getHistoryNameCount");
        int i = 0;
        String mName = null;
        Cursor cursor = getWritableDatabase().query(HistoryTable.NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            mName = cursor.getString(cursor.getColumnIndex(HistoryTable.Cols.NAME));
            if (name.equals(mName)) {
                i++;
            }
        }
        cursor.close();
        return i;
    }

    public synchronized Alarms getAlarmByAlarmId(int alarm_id) {
        Log.d(TAG, "getAlarmByAlarmId");
        int mId;
        Cursor cursor = getWritableDatabase().query(ContentDbSchema.AlarmsTable.NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            mId = cursor.getInt(cursor.getColumnIndex(ContentDbSchema.AlarmsTable.Cols.ALARM_ID));
            if (mId == alarm_id) {
                return new Alarms(mId, cursor.getString(cursor.getColumnIndex(ContentDbSchema.AlarmsTable.Cols.DATE)),
                        cursor.getString(cursor.getColumnIndex(ContentDbSchema.AlarmsTable.Cols.IDS)),
                        cursor.getString(cursor.getColumnIndex(ContentDbSchema.AlarmsTable.Cols.INFO)));
            }
        }
        cursor.close();
        return null;
    }

    public synchronized List<MyDate> getHistoryNameDate(String name, MyDate myDate) {
        Log.d(TAG, "getHistoryNameDate");
        List<MyDate> strings = new ArrayList<>();
        String mName = null, date = null;
        Calendar calendar = Calendar.getInstance();
        Date dates = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Cursor cursor = getWritableDatabase().query(HistoryTable.NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Boolean isExist;
            mName = cursor.getString(cursor.getColumnIndex(HistoryTable.Cols.NAME));
            int exist = cursor.getInt(cursor.getColumnIndex(HistoryTable.Cols.EXIST));
            date = cursor.getString(cursor.getColumnIndex(HistoryTable.Cols.DATE));
            if (exist == 0) {
                isExist = false;
            } else {
                isExist = true;
            }
            try {
                dates = simpleDateFormat.parse(date);
                calendar.setTime(dates);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (name.equals(mName)&& calendar.get(Calendar.YEAR) == myDate.getYear() &&
                    (calendar.get(Calendar.MONTH) + 1) == myDate.getMonth()
                    && calendar.get(Calendar.DAY_OF_MONTH) == myDate.getDay()) {
                strings.add(new MyDate(isExist, calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)));
            }
        }
        cursor.close();
        return strings;
    }

    public synchronized List<MyDate> getHistoryNameDate(String name) {
        Log.d(TAG, "getHistoryNameDate");
        List<MyDate> strings = new ArrayList<>();
        String mName = null, date = null;
        Calendar calendar = Calendar.getInstance();
        Date dates = null;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        Cursor cursor = getWritableDatabase().query(HistoryTable.NAME, null, null, null, null, null, null);
        while (cursor.moveToNext()) {
            Boolean isExist;
            mName = cursor.getString(cursor.getColumnIndex(HistoryTable.Cols.NAME));
            int exist = cursor.getInt(cursor.getColumnIndex(HistoryTable.Cols.EXIST));
            date = cursor.getString(cursor.getColumnIndex(HistoryTable.Cols.DATE));
            if (exist == 0) {
                isExist = false;
            } else {
                isExist = true;
            }
            try {
                dates = simpleDateFormat.parse(date);
                calendar.setTime(dates);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (name.equals(mName)) {
                strings.add(new MyDate(isExist, calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH),
                        calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE)));
            }
        }
        cursor.close();
        return strings;
    }

}

