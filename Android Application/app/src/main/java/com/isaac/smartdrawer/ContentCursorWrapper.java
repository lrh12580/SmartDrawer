package com.isaac.smartdrawer;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.isaac.smartdrawer.ContentDbSchema.ContentsTable;

import java.util.Date;

/**
 * Created by Isaac on 2016/7/30 0030.
 */
public class ContentCursorWrapper extends CursorWrapper{
    public ContentCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Content getContent() {
        String id = getString(getColumnIndex(ContentsTable.Cols.ID));
        String name = getString(getColumnIndex(ContentsTable.Cols.NAME));
        String category = getString(getColumnIndex(ContentsTable.Cols.CATEGORY));
        boolean isExist = getInt(getColumnIndex(ContentsTable.Cols.EXIST)) == 1;

        return new Content(id, name, category, isExist);
    }


    public Goods getHistory() {
        String id = getString(getColumnIndex(ContentDbSchema.HistoryTable.Cols.ID));
        String name = getString(getColumnIndex(ContentDbSchema.HistoryTable.Cols.NAME));
        String category = getString(getColumnIndex(ContentDbSchema.HistoryTable.Cols.CATEGORY));
        int exist = getInt(getColumnIndex(ContentDbSchema.HistoryTable.Cols.EXIST));
        String date = getString(getColumnIndex(ContentDbSchema.HistoryTable.Cols.DATE));
        return new Goods(id, name, category, exist, date);
    }

    public Alarms getAlarm() {
        int alarm_id = getInt(getColumnIndex(ContentDbSchema.AlarmsTable.Cols.ALARM_ID));
        String date = getString(getColumnIndex(ContentDbSchema.AlarmsTable.Cols.DATE));
        String ids = getString(getColumnIndex(ContentDbSchema.AlarmsTable.Cols.IDS));
        String info = getString(getColumnIndex(ContentDbSchema.AlarmsTable.Cols.INFO));

        return new Alarms(alarm_id, date, ids, info);
    }


    public String getId() {
        return getString(getColumnIndex(ContentsTable.Cols.ID));
    }
}
